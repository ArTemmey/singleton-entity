package ru.impression.syncable_entity_example.presentation

import androidx.fragment.app.Fragment
import ru.impression.syncable_entity.SyncableEntityParentViewModel
import ru.impression.syncable_entity.andSyncableEntities
import ru.impression.syncable_entity_example.data_source.MockedBackendApi
import ru.impression.syncable_entity_example.databinding.FragmentMainBinding
import ru.impression.syncable_entity_example.databinding.ItemPhoneBinding
import ru.impression.syncable_entity_example.di.daggerComponent
import ru.impression.ui_generator_annotations.MakeComponent
import ru.impression.ui_generator_base.ComponentScheme
import ru.impression.ui_generator_base.isLoading
import ru.impression.ui_generator_base.reload
import javax.inject.Inject

@MakeComponent
class MainFragment :
    ComponentScheme<Fragment, MainFragmentViewModel>({ FragmentMainBinding::class })

class MainFragmentViewModel : SyncableEntityParentViewModel() {

    @Inject
    lateinit var mockedBackendApi: MockedBackendApi

    init {
        daggerComponent.inject(this)
    }


    var popularPhones by state(mockedBackendApi::getPopularPhones).andSyncableEntities(observeState = false)
    val popularPhonesIsLoading get() = ::popularPhones.isLoading
    fun reloadPopularPhones() {
        ::popularPhones.reload()
    }


    var goodPhones by state(mockedBackendApi::getGoodPhones).andSyncableEntities(observeState = false)
    val goodPhonesIsLoading get() = ::goodPhones.isLoading
    fun reloadGoodPhones() {
        ::goodPhones.reload()
    }


    val phoneItemBindingClass = ItemPhoneBinding::class
}