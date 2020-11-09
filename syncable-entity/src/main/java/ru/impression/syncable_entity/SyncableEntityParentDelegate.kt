package ru.impression.syncable_entity

import ru.impression.kotlin_delegate_concatenator.getDelegateFromSum
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

class SyncableEntityParentDelegate<R : Any, T>(
    private val parent: R,
    private var value: T,
    private val observeState: Boolean
) : ReadWriteProperty<R, T> {

    init {
        bind()
    }

    @Synchronized
    override fun getValue(thisRef: R, property: KProperty<*>) = value

    @Synchronized
    override fun setValue(thisRef: R, property: KProperty<*>, value: T) {
        unbind()
        this.value = value
        bind()
    }

    @Synchronized
    private fun setValueToProperty(value: T) {
        (parent::class.members.firstOrNull {
            (it as? KProperty1<R, *>)
                ?.getDelegateFromSum<R, SyncableEntityParentDelegate<*, *>>(parent) == this
        } as KMutableProperty1<R, T>?)?.set(parent, value)
    }

    @Synchronized
    private fun bind() {
        when (val currentValue = value) {
            is SyncableEntity -> currentValue.bind(parent as SyncableEntityParent, observeState)
            is List<*> -> currentValue.forEach {
                (it as? SyncableEntity)?.bind(parent as SyncableEntityParent, observeState)
            }
        }
    }

    @Synchronized
    fun unbind() {
        when (val currentValue = value) {
            is SyncableEntity -> currentValue.unbind(parent as SyncableEntityParent)
            is List<*> -> currentValue.forEach {
                (it as? SyncableEntity)?.unbind(parent as SyncableEntityParent)
            }
        }
    }

    @Synchronized
    fun replace(oldEntity: SyncableEntity, newEntity: SyncableEntity) {
        when (val currentValue = value) {
            is SyncableEntity -> if (currentValue === oldEntity) setValueToProperty(newEntity as T)
            is List<*> -> currentValue.indexOf(oldEntity).takeIf { it != -1 }
                ?.let { setValueToProperty(currentValue.replace(it, newEntity) as T) }
        }
    }
}