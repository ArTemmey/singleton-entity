package ru.impression.observable_entity

internal fun <T> List<T>.replace(oldValueIndex: Int, newValue: T) = ArrayList<T>().apply {
    this@replace.forEachIndexed { index, value ->
        if (index == oldValueIndex) add(newValue) else add(value)
    }
}