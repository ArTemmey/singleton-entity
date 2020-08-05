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
    ): ReadWriteProperty<CoroutineObservableEntity, T?> = ObservableEntityStateImpl(
        this,
        null,
        getInitialValue,
        immediatelyBindChanges,
        onChanged
    )

    protected fun <T : ObservableEntity> observableEntity(
        getInitialValue: suspend () -> T?,
        immediatelyBindChanges: Boolean = false,
        onChanged: ((T?) -> Unit)? = null
    ): ReadWriteProperty<CoroutineObservableEntity, T?> = ObservableEntityParentStateImpl(
        this,
        null,
        getInitialValue,
        immediatelyBindChanges,
        onChanged
    ).also { delegates.add(it) }

    protected fun <T : ObservableEntity> observableEntities(
        getInitialValue: suspend () -> Iterable<T>?,
        immediatelyBindChanges: Boolean = false,
        onChanged: ((Iterable<T>?) -> Unit)? = null
    ): ReadWriteProperty<CoroutineObservableEntity, Iterable<T>?> = ObservableEntityParentStateImpl(
        this,
        null,
        getInitialValue,
        immediatelyBindChanges,
        onChanged
    ).also { delegates.add(it) }
}