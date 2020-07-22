package ru.impression.observable_entity

import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible

open class ObservableEntityParentDelegate<R : ObservableEntityParent, T>(
    parent: R,
    initialValue: T,
    getInitialValue: (suspend () -> T)?,
    immediatelyBindChanges: Boolean?,
    onChanged: ((T) -> Unit)?
) : ObservableEntityStateDelegate<R, T>(
    parent,
    initialValue,
    getInitialValue,
    immediatelyBindChanges,
    onChanged
) {


    init {
        attachCurrentValue()
    }

    @Synchronized
    fun attachCurrentValue() {
        attachValue(value)
    }

    @Synchronized
    fun detachCurrentValue() {
        detachValue(value)
    }

    @Synchronized
    private fun attachValue(value: T) {
        when (value) {
            is ObservableEntity -> value.attach(parent)
            is List<*> -> value.forEach { (it as? ObservableEntity)?.attach(parent) }
        }
    }

    @Synchronized
    private fun detachValue(value: T) {
        when (value) {
            is ObservableEntity -> value.detach(parent)
            is List<*> -> value.forEach { (it as? ObservableEntity)?.detach(parent) }
        }
    }

    @Synchronized
    override fun setValue(thisRef: R, property: KProperty<*>, value: T) {
        detachValue(this.value)
        attachValue(value)
        super.setValue(thisRef, property, value)
    }

    @Synchronized
    private fun setValueToProperty(value: T) {
        (parent::class.members.firstOrNull {
            it.isAccessible = true
            it is KMutableProperty1<*, *> && (it as KMutableProperty1<R, *>)
                .getDelegate(parent) === this
        } as KMutableProperty1<R, T>?)?.set(parent, value)
    }

    @Synchronized
    fun replace(oldEntity: ObservableEntity, newEntity: ObservableEntity) {
        when (val currentValue = value) {
            is ObservableEntity -> if (currentValue === oldEntity) setValueToProperty(newEntity as T)

            is List<*> -> currentValue.indexOf(oldEntity).takeIf { it != -1 }
                ?.let { setValueToProperty(currentValue.replace(it, newEntity) as T) }
        }
    }
}