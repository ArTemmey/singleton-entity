package ru.impression.singleton_entity

interface SingletonEntity : SingletonEntityParent {
    val id: Any

    var instance: SingletonEntity

    fun replaceWith(newEntity: SingletonEntity)

    fun addParent(parent: SingletonEntityParent)

    fun removeParent(parent: SingletonEntityParent)
}

class SingletonEntityImpl(override val id: Any) : SingletonEntity,
    SingletonEntityParent by SingletonEntityParentImpl() {

    private var _instance: SingletonEntity? = null

    override var instance: SingletonEntity
        get() = _instance!!
        set(value) {
            _instance = value
            EntityStore.replaceIfExists(value)
        }

    private val parents = ArrayList<SingletonEntityParent>()

    override fun addParent(parent: SingletonEntityParent) {
        if (!parents.contains(parent)) {
            parents.add(parent)
            EntityStore.put(instance)
        }
    }

    override fun removeParent(parent: SingletonEntityParent) {
        parents.remove(parent)
        if (parents.isEmpty()) {
            EntityStore.remove(instance)
            detachFromEntities()
        }
    }

    override fun replaceWith(newEntity: SingletonEntity) {
        parents.forEach { it.replace(instance, newEntity) }
    }
}