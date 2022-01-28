package ru.impression.singleton_entity

import java.lang.ref.WeakReference
import kotlin.reflect.KClass

object SingletonEntityStore {

    private val entities =
        HashMap<KClass<out SingletonEntity>, MutableList<WeakReference<SingletonEntity>>>()

    var maxSize = 500

    fun get(entityClass: KClass<out SingletonEntity>, id: Any): SingletonEntity? =
        entities[entityClass]?.firstOrNull { it.get()?.id == id }?.get()

    fun contains(entity: SingletonEntity): Boolean =
        entities[entity::class]?.firstOrNull { it === entity } != null

    fun put(entity: SingletonEntity) {
        val entityId = entity.id
        synchronized(this) {
            val list = entities[entity::class]
                ?: ArrayList<WeakReference<SingletonEntity>>().also { entities[entity::class] = it }
            list.removeAll { it.get() == null }
            val oldEntityIndex = list.indexOfFirst { it.get()?.id == entityId }.takeIf { it != -1 }
            if (oldEntityIndex != null) {
                val oldEntity = list[oldEntityIndex]
                if (oldEntity === entity) return
                list[oldEntityIndex] = WeakReference(entity)
                oldEntity.get()?.replaceWith(entity)
            } else {
                if (list.size == maxSize) list.removeFirst()
                list.add(WeakReference(entity))
            }
        }
    }

    fun remove(entity: SingletonEntity) {
        val entityId = entity.id
        synchronized(this) {
            val list = entities[entity::class] ?: return
            list.removeAll { it.get() == null }
            if (list.firstOrNull { it.get()?.id == entityId } === entity) {
                list.remove(entity)
                if (list.isEmpty()) entities.remove(entity::class)
            }
        }
    }
}