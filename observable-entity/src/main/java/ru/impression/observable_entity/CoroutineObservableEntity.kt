package ru.impression.observable_entity

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.properties.ReadWriteProperty

abstract class CoroutineObservableEntity : ObservableEntity(),
    CoroutineScope by CoroutineScope(Dispatchers.IO) {

    protected fun <T> state(
        getInitialValue: suspend () -> T,
        immediatelyBindChanges: Boolean = false,
        onChanged: ((T?) -> Unit)? = null
    ): ReadWriteProperty<CoroutineObservableEntity, T?> = ObservableEntityStateDelegate(
        this,
        null,
        getInitialValue,
        immediatelyBindChanges,
        onChanged
    )
}