package edu.bu.cs683.myflickr

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flickr4java.flickr.Flickr
import com.flickr4java.flickr.REST
import com.flickr4java.flickr.photos.SearchParameters
import edu.bu.cs683.myflickr.data.Photo
import edu.bu.cs683.myflickr.data.PhotosAdapter

/**
 * This is where all the images stuff will be done; all the related fragments
 * will be using this activity
 *
 * @author dlegaspi@bu.edu
 */
class ImagesActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var userId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_images)
        userId = intent.extras?.get("user_id") as String

        loadImages()
    }

    @SuppressLint("StaticFieldLeak")
    fun loadImages() {
        object : AsyncTask<Void, Void, MutableList<Photo>>() {
            override fun doInBackground(vararg p0: Void?): MutableList<Photo> {
                val flickr = Flickr(BuildConfig.FLICKR_API_KEY, BuildConfig.FLICKR_API_SECRET, REST())
                val photosInterface = flickr.photosInterface
                val searchParameters = SearchParameters()

                searchParameters.userId = intent.extras?.get("user_id") as String?
                val photos = photosInterface.search(searchParameters, 24, 2)
                    .map { Photo(id = it.id, url = it.medium640Url, title = it.title) }
                    .toMutableList()

                return photos
            }

            override fun onPostExecute(photos: MutableList<Photo>) {
                recyclerView = findViewById(R.id.photosRecyclerView)
                val layoutManager = GridLayoutManager(this@ImagesActivity, 1)
                recyclerView.layoutManager = layoutManager
                recyclerView.adapter = PhotosAdapter(photos)
            }
        }.execute()
    }
}
