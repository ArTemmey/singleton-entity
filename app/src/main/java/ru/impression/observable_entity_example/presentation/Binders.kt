package ru.impression.observable_entity_example.presentation

import android.view.View
import androidx.databinding.BindingAdapter

object Binders  {

    @JvmStatic
    @BindingAdapter("isVisible")
    fun setIsVisible(view: View, value: Boolean) {
        view.visibility = if (value) View.VISIBLE else View.GONE
    }
}