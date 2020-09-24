package ru.impression.observable_entity

import ru.impression.kotlin_delegate_concatenator.getDelegateFromSum
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

class ObservableEntityParentDelegate<R : ObservableEntityParent, T>(
    private val parent: R,
    private var value: T,
    private val observeChanges: Boolean
) : ReadWriteProperty<R, T> {

    @Synchronized
    override fun getValue(thisRef: R, property: KProperty<*>) = value

    @Synchronized
    override fun setValue(thisRef: R, property: KProperty<*>, value: T) {
        unbindParent()
        this.value = value
        bindParent()
    }

    @Synchronized
    private fun setValueToProperty(value: T) {
        (parent::class.members.firstOrNull {
            (it as? KProperty1<R, *>)
                ?.getDelegateFromSum<R, ObservableEntityParentDelegate<*, *>>(parent) == this
        } as KMutableProperty1<R, T>?)?.set(parent, value)
    }

    @Synchronized
    fun onCreate() {
        bindParent()
    }

    @Synchronized
    fun onDestroy() {
        unbindParent()
    }

    @Synchronized
    private fun bindParent() {
        when (val value = value) {
            is ObservableEntity -> value.bind(parent, observeChanges)
            is List<*> -> value.forEach { (it as? ObservableEntity)?.bind(parent, observeChanges) }
        }
    }

    @Synchronized
    private fun unbindParent() {
        when (val value = value) {
            is ObservableEntity -> value.unbind(parent)
            is List<*> -> value.forEach { (it as? ObservableEntity)?.unbind(parent) }
        }
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