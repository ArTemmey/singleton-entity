package ru.impression.observable_entity

import java.util.concurrent.ConcurrentHashMap
import kotlin.properties.ReadWriteProperty

abstract class ObservableEntity : ObservableEntityParent {

    open val primaryProperty: Any? = null

    var isGlobal: Boolean
        get() = GlobalStore.contains(this)
        set(value) {
            if (value) GlobalStore.add(this) else GlobalStore.remove(this)
        }

    @Volatile
    private var wasGlobal = false

    private val delegates = ArrayList<ObservableEntityParentDelegate<ObservableEntity, *>>()

    private val parents = ConcurrentHashMap<ObservableEntityParent, Boolean>()

    protected fun <T> state(
        initialValue: T,
        immediatelyBindChanges: Boolean = false,
        onChanged: ((T) -> Unit)? = null
    ): ReadWriteProperty<ObservableEntity, T> =
        ObservableEntityStateDelegate(this, initialValue, null, immediatelyBindChanges, onChanged)

    protected fun <T : ObservableEntity?> observableEntity(
        initialValue: T,
        observeChanges: Boolean = true
    ) = ObservableEntityParentDelegate(this, initialValue, observeChanges)
        .also { delegates.add(it) }


    protected fun <T : ObservableEntity> observableEntities(
        initialValue: List<T>?,
        observeChanges: Boolean = true
    ) = ObservableEntityParentDelegate(this, initialValue, observeChanges)
        .also { delegates.add(it) }

    @Synchronized
    fun bind(parent: ObservableEntityParent, observeChanges: Boolean) {
        if (!parents.contains(parent)) parents[parent] = observeChanges
        if (wasGlobal) {
            isGlobal = true
            wasGlobal = false
        }
    }

    @Synchronized
    internal fun unbind(parent: ObservableEntityParent) {
        parents.remove(parent)
        if (parents.isEmpty()) {
            wasGlobal = isGlobal
            isGlobal = false
        }
    }

    @Synchronized
    override fun onStateChanged(immediatelyBindChanges: Boolean) {
        parents.forEach { if (it.value) it.key.onStateChanged(immediatelyBindChanges) }
    }

    @Synchronized
    internal fun replaceWith(newEntity: ObservableEntity) {
        parents.keys.forEach { it.replace(this, newEntity) }
    }

    @Synchronized
    override fun replace(oldEntity: ObservableEntity, newEntity: ObservableEntity) {
        delegates.forEach { it.replace(oldEntity, newEntity) }
    }
}