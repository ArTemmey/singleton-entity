package ru.impression.syncable_entity_example.di

import dagger.Component
import ru.impression.syncable_entity_example.entity.Phone
import ru.impression.syncable_entity_example.presentation.MainFragmentViewModel
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun inject(viewModel: MainFragmentViewModel)

    fun inject(entity: Phone)
}

val daggerComponent: AppComponent = DaggerAppComponent.builder().build()