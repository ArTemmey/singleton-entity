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

class SingletonEntityList<T> @JvmOverloads constructor(c: Collection<T>? = null) :
    ArrayList<T>(c ?: emptyList()),
    SingletonEntityParent {

    init {
        forEach { (it as? SingletonEntity)?.addParent(this) }
    }

    override fun add(element: T): Boolean {
        val resultElement = ((element as? SingletonEntity)?.actualInstance ?: element) as T
        return super.add(resultElement)
            .also { (resultElement as? SingletonEntity)?.addParent(this) }
    }

    override fun add(index: Int, element: T) {
        val resultElement = ((element as? SingletonEntity)?.actualInstance ?: element) as T
        super.add(index, resultElement)
            .also { (resultElement as? SingletonEntity)?.addParent(this) }
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val resultElements = elements.map { ((it as? SingletonEntity)?.actualInstance ?: it) as T }
        return super.addAll(resultElements)
            .also { resultElements.forEach { (it as? SingletonEntity)?.addParent(this) } }
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        val resultElements = elements.map { ((it as? SingletonEntity)?.actualInstance ?: it) as T }
        return super.addAll(index, resultElements)
            .also { resultElements.forEach { (it as? SingletonEntity)?.addParent(this) } }
    }

    override fun clear() {
        forEach { (it as? SingletonEntity)?.removeParent(this) }
        super.clear()
    }

    override fun remove(element: T): Boolean {
        (element as? SingletonEntity)?.removeParent(this)
        return super.remove(element)
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        elements.forEach { (it as? SingletonEntity)?.removeParent(this) }
        return super.removeAll(elements.toSet())
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        forEach { if (!elements.contains(it)) (it as? SingletonEntity)?.removeParent(this) }
        return super.retainAll(elements.toSet())
    }

    override fun removeAt(index: Int): T {
        (getOrNull(index) as? SingletonEntity)?.removeParent(this)
        return super.removeAt(index)
    }

    override fun set(index: Int, element: T): T {
        (getOrNull(index) as? SingletonEntity)?.removeParent(this)
        val resultElement = ((element as? SingletonEntity)?.actualInstance ?: element) as T
        return super.set(index, resultElement)
            .also { (resultElement as? SingletonEntity)?.addParent(this) }
    }

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        subList(fromIndex, toIndex).forEach { (it as? SingletonEntity)?.removeParent(this) }
        super.removeRange(fromIndex, toIndex)
    }

    override fun replace(oldEntity: SingletonEntity, newEntity: SingletonEntity) {
        val index = indexOf(oldEntity as T).takeIf { it != -1 } ?: return
        (get(index) as? SingletonEntity)?.removeParent(this)
        set(index, newEntity as T)
        newEntity.addParent(this)
    }
}

fun <T> Collection<T>.toSingletonEntityList() = SingletonEntityList(this)

class SingletonEntityWrapper<T : SingletonEntity?>(value: T) : SingletonEntityParent {

    @Volatile
    var value: T = value
        set(value) {
            field?.removeParent(this)
            val resultValue = value?.actualInstance as T
            field = resultValue
            resultValue?.addParent(this)
        }

    @Synchronized
    override fun replace(oldEntity: SingletonEntity, newEntity: SingletonEntity) {
        if (oldEntity !== value) return
        oldEntity.removeParent(this)
        value = newEntity as T
        newEntity.addParent(this)
    }
}