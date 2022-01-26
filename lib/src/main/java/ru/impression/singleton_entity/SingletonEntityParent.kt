package ru.impression.singleton_entity

interface SingletonEntityParent {
    fun replace(oldEntity: SingletonEntity, newEntity: SingletonEntity)
}

interface DefaultSingletonEntityParent : SingletonEntityParent {
    fun <T : SingletonEntity?> singletonEntity(initialValue: T): SingletonEntityDelegate<T>
}

class DefaultSingletonEntityParentImpl : DefaultSingletonEntityParent {

    private val delegates = ArrayList<SingletonEntityDelegate<*>>()

    override fun <T : SingletonEntity?> singletonEntity(initialValue: T) =
        SingletonEntityDelegate(this, initialValue).also { delegates.add(it) }

    override fun replace(oldEntity: SingletonEntity, newEntity: SingletonEntity) {
        delegates.forEach {
            if (it.value === oldEntity)
                (it as SingletonEntityDelegate<SingletonEntity>).setValue(newEntity)
        }
    }
}

class SingletonEntityList<T : SingletonEntity> : ArrayList<T>(), SingletonEntityParent {

    init {
        forEach { it.addParent(this) }
    }

    override fun add(element: T): Boolean {
        return super.add(element).also { element.addParent(this) }
    }

    override fun add(index: Int, element: T) {
        super.add(index, element).also { element.addParent(this) }
    }

    override fun addAll(elements: Collection<T>): Boolean {
        return super.addAll(elements).also { elements.forEach { it.addParent(this) } }
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        return super.addAll(index, elements).also { elements.forEach { it.addParent(this) } }
    }

    override fun clear() {
        forEach { it.removeParent(this) }
        super.clear()
    }

    override fun remove(element: T): Boolean {
        element.removeParent(this)
        return super.remove(element)
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        elements.forEach { it.removeParent(this) }
        return super.removeAll(elements.toSet())
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        forEach { if (!elements.contains(it)) it.removeParent(this) }
        return super.retainAll(elements.toSet())
    }

    override fun removeAt(index: Int): T {
        getOrNull(index)?.removeParent(this)
        return super.removeAt(index)
    }

    override fun set(index: Int, element: T): T {
        getOrNull(index)?.removeParent(this)
        return super.set(index, element).also { element.addParent(this) }
    }

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        subList(fromIndex, toIndex).forEach { it.removeParent(this) }
        super.removeRange(fromIndex, toIndex)
    }

    override fun replace(oldEntity: SingletonEntity, newEntity: SingletonEntity) {
        val index = indexOf(oldEntity).takeIf { it != -1 } ?: return
        get(index).removeParent(this)
        set(index, newEntity as T)
        newEntity.addParent(this)
    }
}