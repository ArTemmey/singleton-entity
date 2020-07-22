package ru.impression.observable_entity

import kotlin.reflect.KClass

internal object GlobalStore {

    private val entities = HashMap<KClass<out ObservableEntity>, HashMap<Any, ObservableEntity>>()

    fun add(observableEntity: ObservableEntity) {
        val primaryProperty = observableEntity.primaryProperty ?: return
        val map = entities[observableEntity::class]
            ?: HashMap<Any, ObservableEntity>().also { entities[observableEntity::class] = it }
        map[primaryProperty]?.replaceWith(observableEntity)
        map[primaryProperty] = observableEntity
    }

    fun remove(observableEntity: ObservableEntity) {
        val primaryProperty = observableEntity.primaryProperty ?: return
        val map = entities[observableEntity::class] ?: return
        if (map[primaryProperty] === observableEntity) map.remove(primaryProperty)
        if (map.isEmpty()) entities.remove(observableEntity::class)
    }

    fun contains(observableEntity: ObservableEntity) =
        observableEntity.primaryProperty?.let { entities[observableEntity::class]?.get(it) } === observableEntity
}