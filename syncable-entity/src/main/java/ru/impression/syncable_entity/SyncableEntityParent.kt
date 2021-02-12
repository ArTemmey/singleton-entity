package ru.impression.syncable_entity

import ru.impression.ui_generator_base.StateOwner
import kotlin.reflect.KProperty

interface SyncableEntityParent : StateOwner {

    fun <T> createDelegate(initialValue: T): SyncableEntityDelegate<SyncableEntityParent, T> =
        Delegate(this, initialValue)

    open class Delegate<T>(parent: SyncableEntityParent, value: T) :
        SyncableEntityDelegate<SyncableEntityParent, T>(parent, value) {

        init {
            bindParentToValue()
        }

        override fun setValue(thisRef: SyncableEntityParent, property: KProperty<*>, value: T) {
            unbindParentFromValue()
            super.setValue(thisRef, property, value)
            bindParentToValue()
        }

        protected fun bindParentToValue() {
            (value as? SyncableEntity)?.addParent(parent)
        }

        protected fun unbindParentFromValue() {
            (value as? SyncableEntity)?.removeParent(parent)
        }
    }
}