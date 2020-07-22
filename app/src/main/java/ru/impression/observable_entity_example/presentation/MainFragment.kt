package ru.impression.observable_entity_example.presentation

import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.fragment.app.Fragment
import ru.impression.observable_entity.ObservableEntityParentCoroutineViewModel
import ru.impression.observable_entity_example.data_source.MockedBackendApi
import ru.impression.observable_entity_example.databinding.FragmentMainBinding
import ru.impression.observable_entity_example.databinding.ItemPhoneBinding
import ru.impression.observable_entity_example.di.daggerComponent
import ru.impression.ui_generator_annotations.MakeComponent
import ru.impression.ui_generator_base.ComponentScheme
import ru.impression.ui_generator_base.isLoading
import ru.impression.ui_generator_base.reload
import javax.inject.Inject

@MakeComponent
class MainFragment : ComponentScheme<Fragment, MainFragmentViewModel>({ viewModel ->
    viewModel.toastMessage?.let {
        Toast.makeText(context, it, LENGTH_SHORT).show()
        viewModel.toastMessage = null
    }
    FragmentMainBinding::class
})

class MainFragmentViewModel : ObservableEntityParentCoroutineViewModel() {

    @Inject
    lateinit var mockedBackendApi: MockedBackendApi

    init {
        daggerComponent.inject(this)
    }


    var popularPhones by observableEntities(request { mockedBackendApi.getPopularPhones() })
    val popularPhonesIsLoading get() = ::popularPhones.isLoading
    fun reloadPopularPhones() {
        ::popularPhones.reload()
    }

    var goodPhones by observableEntities(request { mockedBackendApi.getGoodPhones() })
    val goodPhonesIsLoading get() = ::goodPhones.isLoading
    fun reloadGoodPhones() {
        ::goodPhones.reload()
    }

    val phoneItemBindingClass = ItemPhoneBinding::class


    var toastMessage by state<String?>(null)

    private fun <T> request(block: suspend () -> T?) = suspend {
    //    try {
            block()
//        } catch (e: Exception) {
//            toastMessage = "Request failed"
//            null
//        }
    }
}