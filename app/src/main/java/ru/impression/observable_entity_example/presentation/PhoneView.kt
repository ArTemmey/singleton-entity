package ru.impression.observable_entity_example.presentation

import android.widget.FrameLayout
import ru.impression.kotlin_delegate_concatenator.plus
import ru.impression.observable_entity.ObservableEntityParentViewModel
import ru.impression.observable_entity_example.databinding.ViewPhoneBinding
import ru.impression.observable_entity_example.entity.Phone
import ru.impression.ui_generator_annotations.MakeComponent
import ru.impression.ui_generator_annotations.Prop
import ru.impression.ui_generator_base.ComponentScheme


@MakeComponent
class PhoneView : ComponentScheme<FrameLayout, PhoneViewModel>({ ViewPhoneBinding::class })

class PhoneViewModel : ObservableEntityParentViewModel() {

    @Prop
    var phone: Phone? by state<Phone?>(null) + observableEntity<Phone?>(null)

    fun likeOrUnlikePhone() {
        val phone = phone ?: return
        phone.isLiked = !phone.isLiked
    }
}