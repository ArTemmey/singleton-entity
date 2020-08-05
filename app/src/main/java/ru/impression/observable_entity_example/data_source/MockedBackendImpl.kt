package ru.impression.observable_entity_example.data_source

import kotlinx.coroutines.delay
import ru.impression.observable_entity_example.entity.Phone
import javax.inject.Inject

class MockedBackendImpl @Inject constructor() : MockedBackendApi {

    override suspend fun getPopularPhones(): List<Phone> {
        delay(1000)
        return listOf(
            Phone(0, "Samsung Galaxy S20", "700$"),
            Phone(1, "IPhone XS Max", "999$"),
            Phone(2, "OnePlus 8 Pro", "700$")
        ).apply { forEach { it.isGlobal = true } }
    }

    override suspend fun getGoodPhones(): List<Phone> {
        delay(2000)
        return listOf(
            Phone(2, "OnePlus 8 Pro", "700$"),
            Phone(3, "Nokia 3310", "10$")
        ).apply { forEach { it.isGlobal = true } }
    }
}