package ru.impression.syncable_entity

import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import ru.impression.kotlin_delegate_concatenator.plus
import ru.impression.ui_generator_base.ComponentViewModel
import ru.impression.ui_generator_base.CoroutineViewModel
import ru.impression.ui_generator_base.StateDelegate
import kotlin.properties.ReadWriteProperty

abstract class SyncableEntityParentViewModel(attrs: IntArray? = null) : CoroutineViewModel(attrs),
    SyncableEntityParent {

    private val delegates = ArrayList<Delegate<*>>()

    override fun <T> createDelegate(initialValue: T): SyncableEntityDelegate<SyncableEntityParent, T> =
        Delegate(this, initialValue).also { delegates.add(it) }

    fun <T : SyncableEntity?> StateDelegate<ComponentViewModel, T>.andSyncableEntity() =
        (this + syncableEntity(value)) as ReadWriteProperty<ComponentViewModel, T>

    fun <T : List<SyncableEntity>?> StateDelegate<CoroutineViewModel, T>.andSyncableEntities() =
        (this + (syncableEntities(value) as ReadWriteProperty<CoroutineViewModel, T>))
                as ReadWriteProperty<CoroutineViewModel, T>

    @CallSuper
    override fun onLifecycleEvent(event: Lifecycle.Event) {
        delegates.forEach { it.onLifecycleEvent(event) }
    }

    private class Delegate<T>(parent: SyncableEntityParentViewModel, value: T) :
        SyncableEntityParent.Delegate<T>(parent, value) {

        private var isDestroyed = false

        fun onLifecycleEvent(event: Lifecycle.Event) {
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
                    if (isDestroyed) bindParentToValue()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    unbindParentFromValue()
                    isDestroyed = true
                }
            }
        }

    }
}