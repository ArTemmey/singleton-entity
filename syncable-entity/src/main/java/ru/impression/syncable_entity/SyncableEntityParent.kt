package ru.impression.syncable_entity

interface SyncableEntityParent {
    fun onStateChanged(immediatelyBindChanges: Boolean)
    fun replace(oldEntity: SyncableEntity, newEntity: SyncableEntity)
}