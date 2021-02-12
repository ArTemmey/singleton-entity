package ru.impression.syncable_entity

import ru.impression.kotlin_delegate_concatenator.getDelegateFromSum
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

open class SyncableEntityDelegate<R : Any, T>(
    protected val parent: R,
    protected var value: T
) : ReadWriteProperty<R, T> {

    init {
        bindToValue()
    }

    @Synchronized
    override fun getValue(thisRef: R, property: KProperty<*>): T = value

    @Synchronized
    override fun setValue(thisRef: R, property: KProperty<*>, value: T) {
        unbindFromValue()
        this.value = value
        bindToValue()
    }

    @Synchronized
    private fun setValueToProperty(value: T) {
        (parent::class.declaredMemberProperties.firstOrNull {
            (it as? KProperty1<R, T>)
                ?.getDelegateFromSum<R, SyncableEntityDelegate<R, T>>(parent) == this
        } as? KMutableProperty1<R, T>?)?.set(parent, value)
    }

    @Synchronized
    private fun bindToValue() {
        forEach { it.addParentDelegate(this) }
    }

    @Synchronized
    private fun unbindFromValue() {
        forEach { it.removeParentDelegate(this) }
    }

    @Synchronized
    private fun forEach(block: (SyncableEntity) -> Unit) {
        when (val currentValue = value) {
            is SyncableEntity -> block(currentValue)
            is List<*> -> currentValue.forEach { (it as? SyncableEntity)?.let(block) }
        }
    }

    @Synchronized
    fun replace(oldEntity: SyncableEntity, newEntity: SyncableEntity) {
        when (val currentValue = value) {
            is SyncableEntity -> if (currentValue === oldEntity) setValueToProperty(newEntity as T)
            is MutableList<*> -> (currentValue as MutableList<SyncableEntity>).indexOf(oldEntity)
                .takeIf { it != -1 }?.let { currentValue.set(it, newEntity) }
        }
    }
}