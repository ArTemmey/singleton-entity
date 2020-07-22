package ru.impression.observable_entity

import androidx.lifecycle.Lifecycle
import ru.impression.ui_generator_base.ComponentViewModel
import kotlin.properties.ReadWriteProperty

abstract class ObservableEntityParentViewModel : ComponentViewModel(), ObservableEntityParent {

    private val delegates = ArrayList<ObservableEntityParentDelegate<ObservableEntityParentViewModel, *>>()

    protected fun <T : ObservableEntity?> observableEntity(
        initialValue: T,
        immediatelyBindChanges: Boolean = false,
        onChanged: ((T) -> Unit)? = null
    ): ReadWriteProperty<ObservableEntityParentViewModel, T> = ObservableEntityParentDelegate(
        this,
        initialValue,
        null,
        immediatelyBindChanges,
        onChanged
    ).also { delegates.add(it) }

    protected fun <T : ObservableEntity> observableEntities(
        initialValue: List<T>?,
        immediatelyBindChanges: Boolean = false,
        onChanged: ((List<T>?) -> Unit)? = null
    ): ReadWriteProperty<ObservableEntityParentViewModel, List<T>?> = ObservableEntityParentDelegate(
        this,
        initialValue,
        null,
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