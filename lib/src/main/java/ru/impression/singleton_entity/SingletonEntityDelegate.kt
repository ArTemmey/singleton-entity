package ru.impression.singleton_entity

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SingletonEntityDelegate<T : SingletonEntity?>(
    private val parent: SingletonEntityParent,
    initialValue: T
) : ReadWriteProperty<SingletonEntityParent, T> {

    @Volatile
    var value = initialValue
        private set

    init {
        initialValue?.addParent(parent)
    }

    override fun getValue(thisRef: SingletonEntityParent, property: KProperty<*>) = value

    override fun setValue(
        thisRef: SingletonEntityParent,
        property: KProperty<*>,
        value: T
    ) {
        setValue(value)
    }

    fun setValue(value: T) {
        this.value?.removeParent(parent)
        this.value = value
        value?.addParent(parent)
    }
}