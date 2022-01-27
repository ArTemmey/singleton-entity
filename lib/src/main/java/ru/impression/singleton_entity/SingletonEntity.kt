package ru.impression.singleton_entity

import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList

interface SingletonEntity : SingletonEntityParent {
    val id: Any

    var instance: SingletonEntity

    val parents: List<WeakReference<SingletonEntityParent>>

    fun replaceWith(newEntity: SingletonEntity)

    fun addParent(parent: SingletonEntityParent)

    fun removeParent(parent: SingletonEntityParent)
}

class SingletonEntityImpl(override val id: Any) : SingletonEntity,
    DefaultSingletonEntityParent by DefaultSingletonEntityParentImpl() {

    @Volatile
    private var _instance: SingletonEntity? = null

    override var instance: SingletonEntity
        get() = _instance!!
        set(value) {
            _instance = value
            SingletonEntityStore.put(value)
        }

    override val parents = CopyOnWriteArrayList<WeakReference<SingletonEntityParent>>()

    @Synchronized
    override fun addParent(parent: SingletonEntityParent) {
        parents.removeAll { it.get() == null }
        if (parents.firstOrNull { it.get() === parent } == null) parents.add(WeakReference(parent))
    }

    @Synchronized
    override fun removeParent(parent: SingletonEntityParent) {
        parents.removeAll { it.get() == null }
        parents.forEach {
            if (it.get() === parent) {
                parents.remove(it)
                return@forEach
            }
        }
        if (parents.isEmpty() && SingletonEntityStore.contains(this))
            SingletonEntityStore.remove(this)
    }

    override fun replaceWith(newEntity: SingletonEntity) {
        parents.forEach { it.get()?.replace(instance, newEntity) }
    }
}