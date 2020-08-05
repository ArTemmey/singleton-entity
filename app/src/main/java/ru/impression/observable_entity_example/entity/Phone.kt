package ru.impression.observable_entity_example.entity

import ru.impression.observable_entity.ObservableEntity

data class Phone(val id: Long, val name: String, val price: String) : ObservableEntity() {

    override val primaryProperty = id

    var isLiked by state(false)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Phone) return false

        if (id != other.id) return false
        if (name != other.name) return false
        if (price != other.price) return false
        if (isLiked != other.isLiked) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + price.hashCode()
        result = 31 * result + isLiked.hashCode()
        return result
    }


}