package ru.impression.syncable_entity

import kotlinx.coroutines.CoroutineScope
import ru.impression.kotlin_delegate_concatenator.DelegateSum
import ru.impression.kotlin_delegate_concatenator.getDelegateFromSum
import ru.impression.kotlin_delegate_concatenator.plus
import ru.impression.ui_generator_base.StateDelegate
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.*

internal fun <T> List<T>.replace(oldValueIndex: Int, newValue: T) = ArrayList<T>().apply {
    this@replace.forEachIndexed { index, value ->
        if (index == oldValueIndex) add(newValue) else add(value)
    }
}

fun <T> KCallable<T>.getSyncableDelegate(receiver: CoroutineScope? = null): SyncablePropertyDelegate<T>? {
    return when (this) {
        is KProperty0<*> -> getDelegateFromSum<SyncablePropertyDelegate<T>>()
        is KProperty1<*, *> -> {
            if (receiver == null) return null
            (this as KProperty1<Any?, *>)
                .getDelegateFromSum<Any?, SyncablePropertyDelegate<T>>(receiver)
        }
        else -> null
    }
}

val <T> KProperty0<T>.isSynced get() = getSyncableDelegate()?.isSynced != false

val <T> KProperty0<T>.isSyncing get() = getSyncableDelegate()?.isSyncing == true

suspend fun <T> KProperty0<T>.sync() {
    getSyncableDelegate()?.apply { doSync(value) }
}

suspend fun <T> KMutableProperty0<T>.setAndSync(value: T) {
    getSyncableDelegate()?.apply {
        set(value)
        doSync(value)
    }
}

suspend fun <T> KMutableProperty0<T>.syncAndSet(value: T) {
    getSyncableDelegate()?.apply {
        doSync(value)
        set(value)
    }
}

fun <R : Any, T : SyncableEntity> StateDelegate<R, List<T>?>.andSyncableEntities(
    observeState: Boolean = true
) = this + ((parent as SyncableEntityParent).syncableEntities(
    value,
    observeState
) as SyncableEntityParentDelegate<R, List<T>?>)

fun <R : Any, T : SyncableEntity?> StateDelegate<R, T>.andSyncableEntity(
    observeState: Boolean = true
) = this + ((parent as SyncableEntityParent).syncableEntity(
    value,
    observeState
) as SyncableEntityParentDelegate<R, T>)

fun <T> SyncableEntityStateDelegate<T>.andSyncableProperty(sync: (suspend (T) -> Unit)? = null) =
    this + SyncablePropertyDelegate(parent, value, sync, true)