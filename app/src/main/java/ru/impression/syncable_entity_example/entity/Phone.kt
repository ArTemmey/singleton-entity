package ru.impression.syncable_entity_example.entity

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.impression.syncable_entity.SyncableEntity
import ru.impression.syncable_entity_example.gateway.MockedBackendApi

class Phone(override val id: Long, val name: String, val price: String, isLiked: Boolean) :
    SyncableEntity(), KoinComponent {

    init {
        isSingleton = true
    }

    private val mockedBackendApi by inject<MockedBackendApi>()

    var isLiked by state(isLiked).andSyncableProperty { mockedBackendApi.setPhoneLiked(id, it) }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Phone

        if (this.id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }
}