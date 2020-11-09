package ru.impression.syncable_entity

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SyncablePropertyDelegate<T>(
    private val parent: SyncableEntity,
    @Volatile
    private var sourceValue: T,
    private val sync: (suspend (T) -> Unit)?
) : ReadWriteProperty<SyncableEntity, T> {

    internal var value = sourceValue

    @Volatile
    var isSyncing = false
        internal set(value) {
            field = value
            parent.onStateChanged()
        }

    val isSynced
        @Synchronized
        get() = value == sourceValue

    @Synchronized
    fun isSynced(value: T) = value == sourceValue

    @Synchronized
    override fun getValue(thisRef: SyncableEntity, property: KProperty<*>): T = value

    @Synchronized
    override fun setValue(thisRef: SyncableEntity, property: KProperty<*>, value: T) {
        this.value = value
    }

    suspend fun doSync(value: T) {
        if (isSynced(value)) return
        isSyncing = true
        sync?.invoke(value)
        onSynced(value)
        isSyncing = false
    }

    @Synchronized
    fun onCurrentValueSynced() {
        onSynced(value)
    }

    @Synchronized
    fun onSynced(value: T) {
        sourceValue = value
    }
}