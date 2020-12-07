package ru.impression.syncable_entity

import ru.impression.ui_generator_base.StateOwner


interface SyncableEntityParent: StateOwner {

    fun <T : SyncableEntity?> syncableEntity(
        sourceValue: T,
        observeState: Boolean = true
    ): SyncableEntityParentDelegate<T>

    fun <T : SyncableEntity> syncableEntities(
        sourceValue: List<T>?,
        observeState: Boolean = true
    ): SyncableEntityParentDelegate<List<T>?>

    fun replace(oldEntity: SyncableEntity, newEntity: SyncableEntity)
}