package ru.impression.syncable_entity_example.data_source

import kotlinx.coroutines.delay
import ru.impression.syncable_entity_example.entity.Phone
import javax.inject.Inject

class MockedBackendImpl @Inject constructor() : MockedBackendApi {

    override suspend fun getPopularPhones(): MutableList<Phone> {
        delay(1000)
        return mutableListOf(
            Phone(0, "Samsung Galaxy S20", "700$", false),
            Phone(1, "IPhone XS Max", "999$", false),
            Phone(2, "OnePlus 8 Pro", "700$", false)
        )
    }

    override suspend fun getGoodPhones(): MutableList<Phone> {
        delay(2000)
        return mutableListOf(
            Phone(2, "OnePlus 8 Pro", "700$", false),
            Phone(3, "Nokia 3310", "10$", false)
        )
    }

    override suspend fun setPhoneLiked(phoneId: Long, isLiked: Boolean) {
        delay(1000)
    }
}