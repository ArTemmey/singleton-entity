package ru.impression.syncable_entity

import ru.impression.ui_generator_base.StateDelegate

open class SyncableEntityStateDelegate<T>(
    parent: SyncableEntity,
    initialValue: T,
    getInitialValue: (suspend () -> T)?,
    immediatelyBindChanges: Boolean?,
    onChanged: ((T) -> Unit)?
) : StateDelegate<SyncableEntity, T>(
    parent,
    initialValue,
    getInitialValue,
    immediatelyBindChanges,
    onChanged
) {

    @Synchronized
    override fun notifyStateChanged() {
        immediatelyBindChanges?.let { parent.onStateChanged(it) }
    }
}