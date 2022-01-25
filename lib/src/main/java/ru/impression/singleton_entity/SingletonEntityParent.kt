package ru.impression.singleton_entity

import java.util.concurrent.CopyOnWriteArrayList

interface SingletonEntityParent {
    fun replace(oldEntity: SingletonEntity, newEntity: SingletonEntity)
    fun <T : SingletonEntity?> singletonEntity(initialValue: T): SingletonEntityDelegate<T>
    fun detachFromEntities()
}

class SingletonEntityParentImpl : SingletonEntityParent {

    private val delegates = CopyOnWriteArrayList<SingletonEntityDelegate<*>>()

    override fun <T : SingletonEntity?> singletonEntity(initialValue: T) =
        SingletonEntityDelegate(this, initialValue).also { delegates.add(it) }

    @Synchronized
    override fun replace(oldEntity: SingletonEntity, newEntity: SingletonEntity) {
        delegates.forEach {
            if (it.value === oldEntity)
                (it as SingletonEntityDelegate<SingletonEntity>).setValue(newEntity)
        }
    }

    @Synchronized
    override fun detachFromEntities() {
        delegates.forEach { it.value?.removeParent(this) }
    }
}