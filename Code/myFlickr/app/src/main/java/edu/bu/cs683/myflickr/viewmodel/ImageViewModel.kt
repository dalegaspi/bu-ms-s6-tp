package edu.bu.cs683.myflickr.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ImageViewModel : ViewModel() {
    private val _showMetadata = MutableLiveData<Boolean>(false)
    val showMetadata: LiveData<Boolean> get() = _showMetadata

    fun setShowMetadata(showMetadata: Boolean) {
        _showMetadata.value = showMetadata
    }
}
