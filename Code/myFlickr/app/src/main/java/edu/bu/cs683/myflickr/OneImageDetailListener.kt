package edu.bu.cs683.myflickr

import edu.bu.cs683.myflickr.data.Photo

interface OneImageDetailListener {
    fun getImageDetails(photo: Photo)
}
