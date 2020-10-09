package ru.impression.syncable_entity

import androidx.lifecycle.Lifecycle
import ru.impression.ui_generator_base.CoroutineViewModel

abstract class SyncableEntityParentViewModel : CoroutineViewModel(), SyncableEntityParent {

    private val delegates =
        ArrayList<SyncableEntityParentDelegate<SyncableEntityParentViewModel, *>>()

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