package edu.bu.cs683.myflickr

import android.app.Application
import edu.bu.cs683.myflickr.data.ApplicationGraph
import edu.bu.cs683.myflickr.data.DaggerApplicationGraph
import edu.bu.cs683.myflickr.data.FlickrRepository
import edu.bu.cs683.myflickr.data.PhotoRepository

/**
 * The entry point
 *
 * @author dlegaspi@bu.edu
 */
class MyFlickrApplication : Application() {
    val applicationGraph: ApplicationGraph = DaggerApplicationGraph.create()
    val flickrRepository: FlickrRepository = applicationGraph.getFlickrRepository()

    override fun onCreate() {
        super.onCreate()
        PhotoRepository.initialize(this)
    }
}
