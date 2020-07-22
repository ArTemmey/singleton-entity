package ru.impression.observable_entity

import androidx.lifecycle.Lifecycle
import ru.impression.ui_generator_base.CoroutineViewModel
import kotlin.properties.ReadWriteProperty

abstract class ObservableEntityParentCoroutineViewModel : CoroutineViewModel(), ObservableEntityParent {

    private val delegates = ArrayList<ObservableEntityParentDelegate<ObservableEntityParentCoroutineViewModel, *>>()

    protected fun <T : ObservableEntity> observableEntity(
        getInitialValue: suspend () -> T?,
        immediatelyBindChanges: Boolean = false,
        onChanged: ((T?) -> Unit)? = null
    ): ReadWriteProperty<ObservableEntityParentCoroutineViewModel, T?> = ObservableEntityParentDelegate(
        this,
        null,
        getInitialValue,
        immediatelyBindChanges,
        onChanged
    ).also { delegates.add(it) }

    protected fun <T : ObservableEntity> observableEntities(
        getInitialValue: suspend () -> List<T>?,
        immediatelyBindChanges: Boolean = false,
        onChanged: ((List<T>?) -> Unit)? = null
    ): ReadWriteProperty<ObservableEntityParentCoroutineViewModel, List<T>?> = ObservableEntityParentDelegate(
        this,
        null,
        getInitialValue,
        immediatelyBindChanges,
        onChanged
    ).also { delegates.add(it) }

    override fun onLifecycleEvent(event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> delegates.forEach { it.attachCurrentValue() }
            Lifecycle.Event.ON_DESTROY -> delegates.forEach { it.detachCurrentValue() }
        }
    }

    override fun replace(oldEntity: ObservableEntity, newEntity: ObservableEntity) {
        delegates.forEach { it.replace(oldEntity, newEntity) }
    }
}