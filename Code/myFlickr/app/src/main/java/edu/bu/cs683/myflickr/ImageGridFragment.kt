package edu.bu.cs683.myflickr

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flickr4java.flickr.Flickr
import com.flickr4java.flickr.REST
import com.flickr4java.flickr.photos.SearchParameters
import edu.bu.cs683.myflickr.data.Photo
import edu.bu.cs683.myflickr.data.PhotosAdapter
import edu.bu.cs683.myflickr.databinding.FragmentImageGridBinding

/**
 * Fragment for Image grid
 *
 * @author dlegaspi@bu.edu
 */
class ImageGridFragment : Fragment() {
    private var _binding: FragmentImageGridBinding? = null
    private val binding get() = _binding!!

    private var userId: String? = null
    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(Companion.ARG_USER_ID)
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

    fun getGridSize(): Int {
        return if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 3 else 2
    }

    @SuppressLint("StaticFieldLeak")
    fun loadImages() {
        object : AsyncTask<Void, Void, MutableList<Photo>>() {
            override fun doInBackground(vararg p0: Void?): MutableList<Photo> {
                val flickr = Flickr(BuildConfig.FLICKR_API_KEY, BuildConfig.FLICKR_API_SECRET, REST())
                val photosInterface = flickr.photosInterface
                val searchParameters = SearchParameters()

                searchParameters.userId = userId
                val photos = photosInterface.search(searchParameters, 24, 1)
                    .map { Photo(id = it.id, url = it.medium640Url, title = it.title) }
                    .toMutableList()

                return photos
            }

            override fun onPostExecute(photos: MutableList<Photo>) {
                recyclerView = binding.photosRecyclerView
                val layoutManager = GridLayoutManager(context, getGridSize())
                recyclerView.layoutManager = layoutManager
                recyclerView.adapter = PhotosAdapter(photos)
            }
        }.execute()
    }

    companion object {
        const val ARG_USER_ID = "user_id"

        val TAG: String = ImageGridFragment::class.java.simpleName
    }
}
