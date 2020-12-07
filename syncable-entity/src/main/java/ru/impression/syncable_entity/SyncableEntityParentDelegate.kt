package ru.impression.syncable_entity

import ru.impression.kotlin_delegate_concatenator.getDelegateFromSum
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

class SyncableEntityParentDelegate<T>(
    private val parent: SyncableEntityParent,
    private var value: T,
    private val observeState: Boolean
) : ReadWriteProperty<SyncableEntityParent, T> {

    init {
        bind()
    }

    @Synchronized
    override fun getValue(thisRef: SyncableEntityParent, property: KProperty<*>): T = value

    @Synchronized
    override fun setValue(thisRef: SyncableEntityParent, property: KProperty<*>, value: T) {
        unbind()
        this.value = value
        bind()
    }

    @Synchronized
    private fun setValueToProperty(value: T) {
        (parent::class.declaredMemberProperties.firstOrNull {
            (it as? KProperty1<SyncableEntityParent, *>)
                ?.getDelegateFromSum<SyncableEntityParent, SyncableEntityParentDelegate<*>>(parent) == this
        } as KMutableProperty1<SyncableEntityParent, T>?)?.set(parent, value)
    }

    @Synchronized
    fun bind() {
        when (val currentValue = value) {
            is SyncableEntity -> currentValue.bind(parent, observeState)
            is List<*> ->
                currentValue.forEach { (it as? SyncableEntity)?.bind(parent, observeState) }
        }
    }

    @Synchronized
    fun unbind() {
        when (val currentValue = value) {
            is SyncableEntity -> currentValue.unbind(parent)
            is List<*> -> currentValue.forEach { (it as? SyncableEntity)?.unbind(parent) }
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