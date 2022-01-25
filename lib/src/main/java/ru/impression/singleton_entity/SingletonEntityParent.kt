package ru.impression.singleton_entity

interface SingletonEntityParent {
    fun replace(oldEntity: SingletonEntity, newEntity: SingletonEntity)
    fun <T : SingletonEntity?> singletonEntity(initialValue: T): SingletonEntityDelegate<T>
    fun detachFromEntities()
}

class SingletonEntityParentImpl : SingletonEntityParent {

    private val delegates = ArrayList<SingletonEntityDelegate<*>>()

    override fun <T : SingletonEntity?> singletonEntity(initialValue: T) =
        SingletonEntityDelegate(this, initialValue).also { delegates.add(it) }

    override fun replace(oldEntity: SingletonEntity, newEntity: SingletonEntity) {
        delegates.forEach {
            if (it.value === oldEntity)
                (it as SingletonEntityDelegate<SingletonEntity>).setValue(newEntity)
        }
    }

    override fun detachFromEntities() {
        delegates.forEach { it.value?.removeParent(this) }
    }
}