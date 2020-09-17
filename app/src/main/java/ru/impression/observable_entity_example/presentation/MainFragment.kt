package ru.impression.observable_entity_example.presentation

import androidx.fragment.app.Fragment
import ru.impression.kotlin_delegate_concatenator.plus
import ru.impression.observable_entity.ObservableEntityParentCoroutineViewModel
import ru.impression.observable_entity_example.data_source.MockedBackendApi
import ru.impression.observable_entity_example.databinding.FragmentMainBinding
import ru.impression.observable_entity_example.databinding.ItemPhoneBinding
import ru.impression.observable_entity_example.di.daggerComponent
import ru.impression.observable_entity_example.entity.Phone
import ru.impression.ui_generator_annotations.MakeComponent
import ru.impression.ui_generator_base.ComponentScheme
import ru.impression.ui_generator_base.isLoading
import ru.impression.ui_generator_base.reload
import javax.inject.Inject

@MakeComponent
class MainFragment :
    ComponentScheme<Fragment, MainFragmentViewModel>({ FragmentMainBinding::class })

class MainFragmentViewModel : ObservableEntityParentCoroutineViewModel() {

    @Inject
    lateinit var mockedBackendApi: MockedBackendApi

    init {
        daggerComponent.inject(this)
    }


    var popularPhones by state(mockedBackendApi::getPopularPhones) +
            observableEntities<Phone>(null, observeChanges = false)
    val popularPhonesIsLoading get() = ::popularPhones.isLoading
    fun reloadPopularPhones() {
        ::popularPhones.reload()
    }


    var goodPhones by state(mockedBackendApi::getGoodPhones) +
            observableEntities<Phone>(null, observeChanges = false)
    val goodPhonesIsLoading get() = ::goodPhones.isLoading
    fun reloadGoodPhones() {
        ::goodPhones.reload()
    }


    val phoneItemBindingClass = ItemPhoneBinding::class
}