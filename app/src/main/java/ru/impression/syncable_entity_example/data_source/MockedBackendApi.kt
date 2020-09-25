package ru.impression.syncable_entity_example.data_source

import ru.impression.syncable_entity_example.entity.Phone

interface MockedBackendApi {

    suspend fun getPopularPhones(): List<Phone>

    suspend fun getGoodPhones(): List<Phone>

    suspend fun setPhoneLiked(phoneId: Long, isLiked: Boolean)
}