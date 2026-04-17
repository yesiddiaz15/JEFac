package com.yediaz.jefac.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.yediaz.jefac.viewmodel.HomeViewModel
import kotlin.reflect.KClass

class HomeViewModelFactory(
    private val businessId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        @Suppress("UNCHECKED_CAST")
        return HomeViewModel(businessId = businessId) as T
    }
}