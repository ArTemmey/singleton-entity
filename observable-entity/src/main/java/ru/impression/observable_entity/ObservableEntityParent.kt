package ru.impression.observable_entity

interface ObservableEntityParent {
    fun onStateChanged(immediatelyBindChanges: Boolean)
    fun replace(oldEntity: ObservableEntity, newEntity: ObservableEntity)
}