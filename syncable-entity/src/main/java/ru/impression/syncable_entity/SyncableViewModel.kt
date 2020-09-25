package ru.impression.syncable_entity

import androidx.lifecycle.Lifecycle
import ru.impression.ui_generator_base.CoroutineViewModel

abstract class SyncableViewModel : CoroutineViewModel(), SyncableEntityParent {

    private val delegates =
        ArrayList<SyncableEntityParentDelegate<SyncableViewModel, *>>()

    protected fun <T : SyncableEntity?> syncableEntity(
        initialValue: T,
        observeChanges: Boolean = true
    ) = SyncableEntityParentDelegate(this, initialValue, observeChanges)
        .also { delegates.add(it) }

    protected fun <T : SyncableEntity> syncableEntities(
        initialValue: List<T>?,
        observeChanges: Boolean = true
    ) = SyncableEntityParentDelegate(this, initialValue, observeChanges)
        .also { delegates.add(it) }

    override fun onLifecycleEvent(event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> delegates.forEach { it.onCreate() }
            Lifecycle.Event.ON_DESTROY -> delegates.forEach { it.onDestroy() }
        }
    }

    override fun replace(oldEntity: SyncableEntity, newEntity: SyncableEntity) {
        delegates.forEach { it.replace(oldEntity, newEntity) }
    }
}