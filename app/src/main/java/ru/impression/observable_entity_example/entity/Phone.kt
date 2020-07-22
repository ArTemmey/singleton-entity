package ru.impression.observable_entity_example.entity

import ru.impression.observable_entity.ObservableEntity

class Phone(id: Long, val name: String, val price: String) : ObservableEntity() {

    override val primaryProperty = id

    var isLiked by state(false)
}