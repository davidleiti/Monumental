package ubb.thesis.david.monumental.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import ubb.thesis.david.monumental.common.BaseViewModel
import ubb.thesis.david.monumental.common.ViewModelFactory

inline fun <reified T : BaseViewModel> Fragment.getViewModel(noinline creator: (() -> T)? = null): T =
    if (creator == null)
        ViewModelProviders.of(this).get(T::class.java)
    else
        ViewModelProviders.of(this, ViewModelFactory(creator)).get(T::class.java)

inline fun <reified T : BaseViewModel> FragmentActivity.getViewModel(noinline creator: (() -> T)? = null): T =
    if (creator == null)
        ViewModelProviders.of(this).get(T::class.java)
    else
        ViewModelProviders.of(this, ViewModelFactory(creator)).get(T::class.java)