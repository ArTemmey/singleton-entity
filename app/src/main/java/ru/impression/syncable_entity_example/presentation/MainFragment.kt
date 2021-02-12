package ru.impression.syncable_entity_example.presentation

import androidx.fragment.app.Fragment
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.impression.syncable_entity.SyncableEntityParentViewModel
import ru.impression.syncable_entity_example.databinding.FragmentMainBinding
import ru.impression.syncable_entity_example.databinding.ItemPhoneBinding
import ru.impression.syncable_entity_example.gateway.MockedBackendApi
import ru.impression.ui_generator_annotations.MakeComponent
import ru.impression.ui_generator_base.ComponentScheme
import ru.impression.ui_generator_base.isLoading
import ru.impression.ui_generator_base.reload

@MakeComponent
class MainFragment :
    ComponentScheme<Fragment, MainFragmentViewModel>({ FragmentMainBinding::class })

class MainFragmentViewModel : SyncableEntityParentViewModel(), KoinComponent {

    private val mockedBackendApi by inject<MockedBackendApi>()

    var popularPhones by state(mockedBackendApi::getPopularPhones).andSyncableEntities()
    val popularPhonesIsLoading get() = ::popularPhones.isLoading
    fun reloadPopularPhones() {
        ::popularPhones.reload()
    }


    var goodPhones by state(mockedBackendApi::getGoodPhones).andSyncableEntities()
    val goodPhonesIsLoading get() = ::goodPhones.isLoading
    fun reloadGoodPhones() {
        ::goodPhones.reload()
    }


    val phoneItemBindingClass = ItemPhoneBinding::class
}