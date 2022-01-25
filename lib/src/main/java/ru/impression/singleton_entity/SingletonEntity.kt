package ru.impression.singleton_entity

interface SingletonEntity : SingletonEntityParent {
    val id: Any

    fun replaceWith(newEntity: SingletonEntity)

    fun addParent(parent: SingletonEntityParent)

    fun removeParent(parent: SingletonEntityParent)
}

class SingletonEntityImpl(override val id: Any) : SingletonEntity,
    SingletonEntityParent by SingletonEntityParentImpl() {

    private val parents = ArrayList<SingletonEntityParent>()

    init {
        EntityStore.replaceIfExists(this)
    }

    override fun addParent(parent: SingletonEntityParent) {
        if (!parents.contains(parent)) {
            parents.add(parent)
            EntityStore.put(this)
        }
    }

    override fun removeParent(parent: SingletonEntityParent) {
        parents.remove(parent)
        if (parents.isEmpty()) {
            EntityStore.remove(this)
            detachFromEntities()
        }
    }

    override fun replaceWith(newEntity: SingletonEntity) {
        parents.forEach { it.replace(this, newEntity) }
    }
}