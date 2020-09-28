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
                ?.getDelegateFromSum<R, SyncableEntityParentDelegate<*, *>>(parent) == this
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
        value.entities?.forEach { it.bind(parent as SyncableEntityParent, observeState) }
    }

    @Synchronized
    private fun unbindParent() {
        value.entities?.forEach { it.unbind(parent as SyncableEntityParent) }
    }

    @Synchronized
    fun replace(oldEntity: SyncableEntity, newEntity: SyncableEntity) {
        when (val currentValue = value) {
            is SyncableEntity -> if (currentValue === oldEntity) setValueToProperty(newEntity as T)

            is List<*> -> currentValue.indexOf(oldEntity).takeIf { it != -1 }
                ?.let { setValueToProperty(currentValue.replace(it, newEntity) as T) }
        }
    }

    private val T.entities: List<SyncableEntity>?
        get() = when (this) {
            is SyncableEntity -> listOf(this)
            is List<*> -> filterIsInstance<SyncableEntity>().takeIf { it.isNotEmpty() }
            else -> null
        }
}