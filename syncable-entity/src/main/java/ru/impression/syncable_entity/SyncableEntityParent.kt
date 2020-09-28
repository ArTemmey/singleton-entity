package ru.impression.syncable_entity

interface SyncableEntityParent {
    fun <T : SyncableEntity?> syncableEntity(
        sourceValue: T,
        observeState: Boolean = true
    ): SyncableEntityParentDelegate<out Any, T>

    fun <T : SyncableEntity> syncableEntities(
        sourceValue: List<T>?,
        observeState: Boolean = true
    ): SyncableEntityParentDelegate<out Any, List<T>?>

    fun onStateChanged(immediatelyBindChanges: Boolean)
    fun replace(oldEntity: SyncableEntity, newEntity: SyncableEntity)
}