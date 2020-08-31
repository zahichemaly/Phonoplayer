package com.zc.phonoplayer.ui.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

abstract class FragmentViewModel<T>(application: Application) : AndroidViewModel(application) {
    private var _item = MutableLiveData<T>()
    fun item() = _item
    fun set(item: T) {
        _item.value = item
    }
}
