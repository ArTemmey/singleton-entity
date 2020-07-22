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

    protected val delegates = ArrayList<ObservableEntityParentDelegate<out ObservableEntity, *>>()

    private val parents = ConcurrentLinkedQueue<ObservableEntityParent>()

    protected fun <T> state(
        initialValue: T,
        immediatelyBindChanges: Boolean = false,
        onChanged: ((T) -> Unit)? = null
    ): ReadWriteProperty<ObservableEntity, T> =
        ObservableEntityStateDelegate(this, initialValue, null, immediatelyBindChanges, onChanged)

    protected fun <T : ObservableEntity> observableEntity(
        initialValue: T,
        immediatelyBindChanges: Boolean = false,
        onChanged: ((T) -> Unit)? = null
    ): ReadWriteProperty<ObservableEntity, T> = ObservableEntityParentDelegate(
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
    ): ReadWriteProperty<ObservableEntity, Iterable<T>> = ObservableEntityParentDelegate(
        this,
        initialValue,
        null,
        immediatelyBindChanges,
        onChanged
    ).also { delegates.add(it) }

    @Synchronized
    internal fun attach(parent: ObservableEntityParent) {
        if (!parents.contains(parent)) parents.add(parent)
    }

    @Synchronized
    internal fun detach(parent: ObservableEntityParent) {
        parents.remove(parent)
        if (parents.isEmpty()) isGlobal = false
    }

    @Synchronized
    internal fun replaceWith(other: ObservableEntity) {
        parents.forEach { it.replace(this, other) }
    }

    @Synchronized
    override fun onStateChanged(immediatelyBindChanges: Boolean) {
        parents.forEach { it.onStateChanged(immediatelyBindChanges) }
    }

    @Synchronized
    override fun replace(oldEntity: ObservableEntity, newEntity: ObservableEntity) {
        delegates.forEach { it.replace(oldEntity, newEntity) }
    }
}