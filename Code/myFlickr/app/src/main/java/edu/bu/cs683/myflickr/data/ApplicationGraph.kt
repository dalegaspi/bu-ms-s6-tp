package edu.bu.cs683.myflickr.data

import dagger.Component
import javax.inject.Singleton

/**
 * Our Dagger interface
 *
 * @author dlegaspi@bu.edu
 */
@Singleton
@Component
public interface ApplicationGraph {
    fun getFlickrRepository(): FlickrRepository
}
