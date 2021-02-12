package ru.impression.syncable_entity

import kotlinx.coroutines.CoroutineScope
import ru.impression.kotlin_delegate_concatenator.getDelegateFromSum
import kotlin.reflect.KCallable
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

fun <T : SyncableEntity?> Any.syncableEntity(initialValue: T) =
    if (this is SyncableEntityParent)
        createDelegate(initialValue)
    else
        SyncableEntityDelegate(this, initialValue)

fun <T : SyncableEntity> Any.syncableEntities(initialValue: List<T>?) =
    if (this is SyncableEntityParent)
        createDelegate(initialValue)
    else
        SyncableEntityDelegate(this, initialValue)

fun <T> KCallable<T>.getSyncablePropertyDelegate(receiver: CoroutineScope? = null): SyncablePropertyDelegate<T>? {
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

val <T> KProperty0<T>.isSynced get() = getSyncablePropertyDelegate()?.isSynced != false

val <T> KProperty0<T>.isSyncing get() = getSyncablePropertyDelegate()?.isSyncing == true

suspend fun <T> KProperty0<T>.sync() {
    getSyncablePropertyDelegate()?.apply { doSync(value) }
}

suspend fun <T> KMutableProperty0<T>.setAndSync(value: T) {
    getSyncablePropertyDelegate()?.apply {
        set(value)
        doSync(value)
    }
}

suspend fun <T> KMutableProperty0<T>.syncAndSet(value: T) {
    getSyncablePropertyDelegate()?.apply {
        doSync(value)
        set(value)
    }
}