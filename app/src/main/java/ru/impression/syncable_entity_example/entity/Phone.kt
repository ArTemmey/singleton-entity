package ru.impression.syncable_entity_example.entity

import ru.impression.syncable_entity.SyncableEntity
import ru.impression.syncable_entity.andSyncableProperty
import ru.impression.syncable_entity_example.data_source.MockedBackendApi
import ru.impression.syncable_entity_example.di.daggerComponent
import javax.inject.Inject

class Phone(val id: Long, val name: String, val price: String, isLiked: Boolean) :
    SyncableEntity() {

    @Inject
    lateinit var mockedBackendApi: MockedBackendApi

    init {
        daggerComponent.inject(this)
    }

    override val primaryProperty = id

    var isLiked by state(isLiked).andSyncableProperty { mockedBackendApi.setPhoneLiked(id, it) }

    init {
        isSingleton = true
    }

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