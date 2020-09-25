package ru.impression.syncable_entity

import kotlinx.coroutines.CoroutineScope
import ru.impression.kotlin_delegate_concatenator.getDelegateFromSum
import kotlin.reflect.*

internal fun <T> List<T>.replace(oldValueIndex: Int, newValue: T) = ArrayList<T>().apply {
    this@replace.forEachIndexed { index, value ->
        if (index == oldValueIndex) add(newValue) else add(value)
    }
}

fun <T> KCallable<T>.getSyncableDelegate(receiver: CoroutineScope? = null): SyncableDelegate<CoroutineScope, T>? {
    return when (this) {
        is KProperty0<*> -> getDelegateFromSum<SyncableDelegate<CoroutineScope, T>>()
        is KProperty1<*, *> -> {
            if (receiver == null) return null
            (this as KProperty1<Any?, *>)
                .getDelegateFromSum<Any?, SyncableDelegate<CoroutineScope, T>>(receiver)
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