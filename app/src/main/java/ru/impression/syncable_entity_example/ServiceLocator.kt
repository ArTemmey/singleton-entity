package ru.impression.syncable_entity_example

import org.koin.dsl.module
import ru.impression.syncable_entity_example.gateway.MockedBackendApi
import ru.impression.syncable_entity_example.gateway.MockedBackendImpl

val appModule = module {
    single { MockedBackendImpl() as MockedBackendApi }
}