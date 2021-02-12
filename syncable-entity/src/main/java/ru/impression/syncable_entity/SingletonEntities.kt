package ru.impression.syncable_entity

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

internal object SingletonEntities {

    private val entities =
        ConcurrentHashMap<KClass<out SyncableEntity>, ConcurrentHashMap<Any, SyncableEntity>>()

    @Synchronized
    fun get(entityClass: KClass<out SyncableEntity>, id: Any) = entities[entityClass]?.get(id)

    @Synchronized
    fun add(entity: SyncableEntity) {
        val primaryProperty = entity.id ?: return
        val map = entities[entity::class]
            ?: ConcurrentHashMap<Any, SyncableEntity>().also { entities[entity::class] = it }
        val oldEntity = map[primaryProperty]
        if (oldEntity === entity) return
        map[primaryProperty] = entity
        oldEntity?.replaceWith(entity)
    }

    @Synchronized
    fun remove(entity: SyncableEntity) {
        val primaryProperty = entity.id ?: return
        val map = entities[entity::class] ?: return
        if (map[primaryProperty] === entity) {
            map.remove(primaryProperty)
            if (map.isEmpty()) entities.remove(entity::class)
        }
    }

    @Synchronized
    fun contains(entity: SyncableEntity) =
        entity.id?.let { entities[entity::class]?.get(it) } === entity
}