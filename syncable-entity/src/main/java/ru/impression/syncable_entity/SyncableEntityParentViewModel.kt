package ru.impression.syncable_entity

import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import ru.impression.kotlin_delegate_concatenator.plus
import ru.impression.ui_generator_base.ComponentViewModel
import ru.impression.ui_generator_base.CoroutineViewModel
import ru.impression.ui_generator_base.StateDelegate
import kotlin.properties.ReadWriteProperty

abstract class SyncableEntityParentViewModel(attrs: IntArray? = null) : CoroutineViewModel(attrs),
    SyncableEntityParent {

    private val delegates =
        ArrayList<SyncableEntityParentDelegate<*>>()

    private var delegatesWereUnbound = false

    override fun <T : SyncableEntity?> syncableEntity(
        sourceValue: T,
        observeState: Boolean
    ) = SyncableEntityParentDelegate(this, sourceValue, observeState)
        .also { delegates.add(it) }

    override fun <T : SyncableEntity> syncableEntities(
        sourceValue: List<T>?,
        observeState: Boolean
    ) = SyncableEntityParentDelegate(this, sourceValue, observeState)
        .also { delegates.add(it) }

    fun <T : SyncableEntity?> StateDelegate<ComponentViewModel, T>.andSyncableEntity(observeState: Boolean = true) =
        (this + syncableEntity(value, observeState)) as ReadWriteProperty<ComponentViewModel, T>

    fun <T : List<SyncableEntity>?> StateDelegate<CoroutineViewModel, T>.andSyncableEntities(
        observeState: Boolean = false
    ) = (this + (syncableEntities(value, observeState) as ReadWriteProperty<CoroutineViewModel, T>))
            as ReadWriteProperty<CoroutineViewModel, T>

    override fun replace(oldEntity: SyncableEntity, newEntity: SyncableEntity) {
        delegates.forEach { it.replace(oldEntity, newEntity) }
    }

    @CallSuper
    override fun onLifecycleEvent(event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_CREATE && delegatesWereUnbound)
            delegates.forEach { it.bind() }
    }

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        delegates.forEach { it.unbind() }
        delegatesWereUnbound = true
    }
}