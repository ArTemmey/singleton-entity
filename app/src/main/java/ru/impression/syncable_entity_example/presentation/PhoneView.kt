package ru.impression.syncable_entity_example.presentation

import android.widget.FrameLayout
import kotlinx.coroutines.launch
import ru.impression.kotlin_delegate_concatenator.plus
import ru.impression.syncable_entity.SyncableViewModel
import ru.impression.syncable_entity.isSyncing
import ru.impression.syncable_entity.syncAndSet
import ru.impression.syncable_entity_example.databinding.ViewPhoneBinding
import ru.impression.syncable_entity_example.entity.Phone
import ru.impression.ui_generator_annotations.MakeComponent
import ru.impression.ui_generator_annotations.Prop
import ru.impression.ui_generator_base.ComponentScheme


@MakeComponent
class PhoneView : ComponentScheme<FrameLayout, PhoneViewModel>({ ViewPhoneBinding::class })

class PhoneViewModel : SyncableViewModel() {

    @Prop
    var phone: Phone? by state<Phone?>(null) + syncableEntity<Phone?>(null)
    val phoneIsLikedIsSyncing get() = phone?.let { it::isLiked.isSyncing } == true

    fun likeOrUnlikePhone() {
        val phone = phone ?: return
        launch { phone::isLiked.syncAndSet(!phone.isLiked) }
    }
}