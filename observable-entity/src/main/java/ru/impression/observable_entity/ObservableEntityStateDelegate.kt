package ru.impression.observable_entity

import ru.impression.ui_generator_base.StateDelegate

open class ObservableEntityStateDelegate<R : ObservableEntity, T>(
    parent: R,
    initialValue: T,
    getInitialValue: (suspend () -> T)?,
    immediatelyBindChanges: Boolean?,
    onChanged: ((T) -> Unit)?
) : StateDelegate<R, T>(
    parent,
    initialValue,
    getInitialValue,
    immediatelyBindChanges,
    onChanged
) {

    @Synchronized
    override fun notifyStateChanged() {
        immediatelyBindChanges?.let { (parent as? ObservableEntity)?.onStateChanged(it) }
    }
}