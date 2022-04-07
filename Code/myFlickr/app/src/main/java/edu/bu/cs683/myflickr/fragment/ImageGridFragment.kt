package edu.bu.cs683.myflickr.fragment

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flickr4java.flickr.Flickr
import com.flickr4java.flickr.REST
import com.flickr4java.flickr.photos.SearchParameters
import edu.bu.cs683.myflickr.BuildConfig
import edu.bu.cs683.myflickr.listener.OneImageDetailListener
import edu.bu.cs683.myflickr.R
import edu.bu.cs683.myflickr.data.Photo
import edu.bu.cs683.myflickr.adapter.PhotosAdapter
import edu.bu.cs683.myflickr.databinding.FragmentImageGridBinding

/**
 * Fragment for Image grid
 *
 * @author dlegaspi@bu.edu
 */
class ImageGridFragment : Fragment(), OneImageDetailListener {
    private var _binding: FragmentImageGridBinding? = null
    private val binding get() = _binding!!

    private var userId: String? = null
    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(ARG_USER_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentImageGridBinding.inflate(inflater, container, false)

        userId?.let { loadImages() }

        return binding.root
    }

    /**
     * Gets the image grid column per row depending on orientation
     * of the phone screen
     */
    fun getGridColumnsPerRow(): Int {
        return if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            ROWS_PER_COLUMN_LANDSCAPE
        else
            ROWS_PER_COLUMN_PORTRAIT
    }

    @SuppressLint("StaticFieldLeak")
    fun loadImages() {
        object : AsyncTask<Void, Void, MutableList<Photo>>() {
            override fun doInBackground(vararg p0: Void?): MutableList<Photo> {
                val flickr = Flickr(BuildConfig.FLICKR_API_KEY, BuildConfig.FLICKR_API_SECRET, REST())
                val photosInterface = flickr.photosInterface
                val searchParameters = SearchParameters()

                searchParameters.userId = userId
                val photos = photosInterface.search(searchParameters, GRID_PAGE_SIZE, 1)
                    .map { Photo(id = it.id, url = it.medium640Url, title = it.title) }
                    .toMutableList()

                return photos
            }

            override fun onPostExecute(photos: MutableList<Photo>) {
                // build the recycler view and bind the adapter to it so we
                // can draw the individual images from the photo metadata
                // returned by the API
                recyclerView = binding.photosRecyclerView
                val layoutManager = GridLayoutManager(context, getGridColumnsPerRow())
                recyclerView.layoutManager = layoutManager
                recyclerView.adapter = PhotosAdapter(this@ImageGridFragment, photos)
            }
        }.execute()
    }

    companion object {
        const val ARG_USER_ID = "user_id"
        const val GRID_PAGE_SIZE = 24
        const val ROWS_PER_COLUMN_PORTRAIT = 2
        const val ROWS_PER_COLUMN_LANDSCAPE = 3

        val TAG: String = ImageGridFragment::class.java.simpleName
    }

    override fun getImageDetails(photo: Photo) {
        // Load the fragment to
        Log.i(TAG, "Loading image ${photo.id}")
        val args = Bundle()
        args.putString(OneImageFragment.ARG_IMAGE_ID, photo.id)
        parentFragmentManager.commit {
            replace<OneImageFragment>(R.id.container, args = args)
            addToBackStack(null)
        }
    }
}
