package ru.impression.syncable_entity_example.entity

import ru.impression.syncable_entity.SyncableEntity
import ru.impression.syncable_entity_example.data_source.MockedBackendApi
import ru.impression.syncable_entity_example.di.daggerComponent
import javax.inject.Inject

class Phone(private val id: Long, val name: String, val price: String, isLiked: Boolean) :
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
}