package ru.impression.singleton_entity

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

object EntityStore {

    private val entities =
        ConcurrentHashMap<KClass<out SingletonEntity>, ConcurrentHashMap<Any, SingletonEntity>>()

    @Synchronized
    fun get(entityClass: KClass<out SingletonEntity>, id: Any) = entities[entityClass]?.get(id)

    @Synchronized
    fun put(entity: SingletonEntity) {
        put(entity, abortIfAbsent = false)
    }

    @Synchronized
    fun replaceIfExists(entity: SingletonEntity) {
        put(entity, abortIfAbsent = true)
    }

    @Synchronized
    private fun put(entity: SingletonEntity, abortIfAbsent: Boolean) {
        val id = entity.id
        val map = entities[entity::class]
            ?: ConcurrentHashMap<Any, SingletonEntity>().also { entities[entity::class] = it }
        val oldEntity = map[id]
        if (abortIfAbsent && oldEntity == null) return
        if (oldEntity === entity) return
        map[id] = entity
        oldEntity?.replaceWith(entity)
    }


    @Synchronized
    fun remove(entity: SingletonEntity) {
        val id = entity.id
        val map = entities[entity::class] ?: return
        if (map[id] === entity) {
            map.remove(id)
            if (map.isEmpty()) entities.remove(entity::class)
        }
    }

    @Synchronized
    fun contains(entity: SingletonEntity) = entities[entity::class]?.get(entity.id) === entity

    @Synchronized
    fun contains(entityClass: KClass<out SingletonEntity>, entityId: Any) =
        entities[entityClass]?.get(entityId) != null
}