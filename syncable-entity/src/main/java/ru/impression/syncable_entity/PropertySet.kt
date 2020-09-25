package ru.impression.syncable_entity

import kotlin.reflect.KMutableProperty0
import kotlin.reflect.jvm.reflect

abstract class PropertySet(val properties: Set<KMutableProperty0<*>>) {

    suspend fun sync() {
        sync(*properties.map { it.getSyncableDelegate()?.value }.toTypedArray())
    }

    internal suspend fun sync(vararg values: Any?) {
        var needSync = false
        properties.forEachIndexed { index, property ->
            if (property.getSyncableDelegate()?.isSynced(values[index]) == false) {
                needSync = true
                return@forEachIndexed
            }
        }
        if (!needSync) return
        with(properties.firstOrNull()?.getSyncableDelegate()?.parent as? SyncableEntity ?: return) {
            val syncFun = getSyncFun()
            when (syncFun?.reflect()?.parameters?.size) {
                2 -> (syncFun as suspend (Any?, Any?) -> Unit).invoke(values[0], values[1])
            }
        }
    }
}

class TwoPropertySet<T0, T1> internal constructor(
    private val property0: KMutableProperty0<T0>,
    private val property1: KMutableProperty0<T1>
) : PropertySet(setOf(property0, property1)) {

    suspend fun setAndSync(value0: T0, value1: T1) {
        property0.set(value0)
        property1.set(value1)
        sync(value0, value1)
    }

    suspend fun syncAndSet(value0: T0, value1: T1) {
        sync(value0, value1)
        property0.set(value0)
        property1.set(value1)
    }
}

fun <T1, T2> propertySetOf(property0: KMutableProperty0<T1>, property1: KMutableProperty0<T2>) =
    TwoPropertySet(property0, property1)