package ru.impression.syncable_entity

import androidx.annotation.CallSuper
import ru.impression.ui_generator_base.CoroutineViewModel

abstract class SyncableEntityParentViewModel(attrs: IntArray? = null) : CoroutineViewModel(attrs),
    SyncableEntityParent {

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

    override fun replace(oldEntity: SyncableEntity, newEntity: SyncableEntity) {
        delegates.forEach { it.replace(oldEntity, newEntity) }
    }

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        delegates.forEach { it.unbind() }
    }
}