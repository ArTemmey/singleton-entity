package ru.impression.syncable_entity

import kotlinx.coroutines.CoroutineScope
import ru.impression.ui_generator_base.ComponentViewModel
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SyncableDelegate<R : CoroutineScope, T>(
    val parent: R,
    @Volatile
    private var sourceValue: T,
    private val sync: (suspend (T) -> Unit)?
) : ReadWriteProperty<R, T> {

    internal var value = sourceValue

    @Volatile
    var isSyncing = false
        set(value) {
            field = value
            onStateChanged()
        }

    val isSynced
        @Synchronized
        get() = value == sourceValue

    @Synchronized
    fun isSynced(value: T) = value == sourceValue

    @Synchronized
    override fun getValue(thisRef: R, property: KProperty<*>) = value

    @Synchronized
    override fun setValue(thisRef: R, property: KProperty<*>, value: T) {
        this.value = value
        if(!isSynced(value)) (parent as? SyncableEntity)?.isFullySynced = false
    }

    suspend fun doSync(value: T) {
        if (isSynced(value)) return
        isSyncing = true
        sync?.invoke(value)
        isSyncing = false
        onSyncCompleted(value)
    }

    @Synchronized
    fun onSyncCompleted() {
        onSyncCompleted(value)
    }

    @Synchronized
    fun onSyncCompleted(value: T) {
        sourceValue = value
    }

    private fun onStateChanged() {
        (parent as? ComponentViewModel)?.onStateChanged()
            ?: (parent as? SyncableEntity)?.onStateChanged(false)
    }
}