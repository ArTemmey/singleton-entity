package ru.impression.syncable_entity_example.di

import dagger.Binds
import dagger.Module
import ru.impression.syncable_entity_example.data_source.MockedBackendApi
import ru.impression.syncable_entity_example.data_source.MockedBackendImpl
import javax.inject.Singleton

@Module
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun provideMockedBackendApi(mockedBackendImpl: MockedBackendImpl): MockedBackendApi
}