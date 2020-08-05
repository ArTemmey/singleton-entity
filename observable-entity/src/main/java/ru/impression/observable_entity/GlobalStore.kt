package ru.impression.observable_entity

import kotlin.reflect.KClass

internal object GlobalStore {

    private val entities = HashMap<KClass<out ObservableEntity>, HashMap<Any, ObservableEntity>>()

    fun add(entity: ObservableEntity) {
        val primaryProperty = entity.primaryProperty ?: return
        val map = entities[entity::class]
            ?: HashMap<Any, ObservableEntity>().also { entities[entity::class] = it }
        val oldEntity = map[primaryProperty]
        map[primaryProperty] = entity
        oldEntity?.replaceWith(entity)
    }

    fun remove(entity: ObservableEntity) {
        val primaryProperty = entity.primaryProperty ?: return
        val map = entities[entity::class] ?: return
        if (map[primaryProperty] === entity) {
            map.remove(primaryProperty)
            if (map.isEmpty()) entities.remove(entity::class)
        }
    }

    fun contains(entity: ObservableEntity) =
        entity.primaryProperty?.let { entities[entity::class]?.get(it) } === entity
}