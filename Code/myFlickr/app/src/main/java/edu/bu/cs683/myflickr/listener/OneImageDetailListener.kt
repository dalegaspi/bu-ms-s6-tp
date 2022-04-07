package edu.bu.cs683.myflickr.listener

import edu.bu.cs683.myflickr.data.Photo

/**
 * This is the callback interface when user taps on one of the images on the grid
 * to bring up the individual image browser
 *
 * @author dlegaspi@bu.edu
 */
interface OneImageDetailListener {

    /**
     * Get image details for the specified photo tapped by user
     *
     * @param photo the photo
     */
    fun getImageDetails(photo: Photo)
}
