package ru.impression.singleton_entity

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

object EntityStore {

    private val entities =
        ConcurrentHashMap<KClass<out SingletonEntity>, WeakHashMap<SingletonEntity, Any>>()

    fun put(entity: SingletonEntity) {
        put(entity, abortIfAbsent = false)
    }

    fun replaceIfExists(entity: SingletonEntity) {
        put(entity, abortIfAbsent = true)
    }

    private fun put(entity: SingletonEntity, abortIfAbsent: Boolean) {
        val oldEntity = synchronized(this) {
            val entityId = entity.id
            val map = entities[entity::class]
                ?: WeakHashMap<SingletonEntity, Any>().also { entities[entity::class] = it }
            val oldEntity = map.keys.firstOrNull { it.id == entityId }
            if (oldEntity == null && abortIfAbsent) return
            if (oldEntity === entity) return
            map[entity] = Any()
            oldEntity
        }
        oldEntity?.replaceWith(entity)
    }
}