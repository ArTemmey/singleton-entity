package ru.impression.observable_entity_example.di

import dagger.Component
import ru.impression.observable_entity_example.presentation.MainFragmentViewModel
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun inject(viewModel: MainFragmentViewModel)
}

val daggerComponent: AppComponent = DaggerAppComponent.builder().build()