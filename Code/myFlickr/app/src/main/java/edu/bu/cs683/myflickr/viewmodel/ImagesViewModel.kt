package edu.bu.cs683.myflickr.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edu.bu.cs683.myflickr.data.Photo

/**
 * ViewModel for the images list
 *
 * @author dlegaspi@bu.edu
 */
class ImagesViewModel : ViewModel() {
    private val _imagesList: MutableLiveData<List<Photo>> = MutableLiveData()
    val currentImagesList: LiveData<List<Photo>>
        get() = _imagesList

    fun setImagesList(list: List<Photo>) {
        _imagesList.value = list
    }
}
