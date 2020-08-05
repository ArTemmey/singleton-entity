package ru.impression.observable_entity

import java.util.concurrent.ConcurrentLinkedQueue
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

    protected val delegates = ArrayList<ObservableEntityParentStateImpl<out ObservableEntity, *>>()

    private val parents = ConcurrentLinkedQueue<ObservableEntityParent>()

    protected fun <T> state(
        initialValue: T,
        immediatelyBindChanges: Boolean = false,
        onChanged: ((T) -> Unit)? = null
    ): ReadWriteProperty<ObservableEntity, T> =
        ObservableEntityStateImpl(this, initialValue, null, immediatelyBindChanges, onChanged)

    protected fun <T : ObservableEntity> observableEntity(
        initialValue: T,
        immediatelyBindChanges: Boolean = false,
        onChanged: ((T) -> Unit)? = null
    ): ReadWriteProperty<ObservableEntity, T> = ObservableEntityParentStateImpl(
        this,
        initialValue,
        null,
        immediatelyBindChanges,
        onChanged
    ).also { delegates.add(it) }

    protected fun <T : ObservableEntity> observableEntities(
        initialValue: Iterable<T>,
        immediatelyBindChanges: Boolean = false,
        onChanged: ((Iterable<T>) -> Unit)? = null
    ): ReadWriteProperty<ObservableEntity, Iterable<T>> = ObservableEntityParentStateImpl(
        this,
        initialValue,
        null,
        immediatelyBindChanges,
        onChanged
    ).also { delegates.add(it) }

    @Synchronized
    internal fun attach(parent: ObservableEntityParent) {
        if (!parents.contains(parent)) parents.add(parent)
        if (wasGlobal) {
            isGlobal = true
            wasGlobal = false
        }
    }

    @Synchronized
    internal fun detach(parent: ObservableEntityParent) {
        parents.remove(parent)
        if (parents.isEmpty()) {
            wasGlobal = isGlobal
            isGlobal = false
        }
    }

    @Synchronized
    override fun onStateChanged(immediatelyBindChanges: Boolean) {
        parents.forEach { it.onStateChanged(immediatelyBindChanges) }
    }

    @Synchronized
    internal fun replaceWith(newEntity: ObservableEntity) {
        parents.forEach { it.replace(this, newEntity) }
    }

    @Synchronized
    override fun replace(oldEntity: ObservableEntity, newEntity: ObservableEntity) {
        delegates.forEach { it.replace(oldEntity, newEntity) }
    }
}