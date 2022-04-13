package edu.bu.cs683.myflickr.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.opengl.Visibility
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flickr4java.flickr.Flickr
import com.flickr4java.flickr.REST
import com.flickr4java.flickr.photos.SearchParameters
import com.google.android.material.bottomsheet.BottomSheetDialog
import edu.bu.cs683.myflickr.BuildConfig
import edu.bu.cs683.myflickr.listener.OneImageDetailListener
import edu.bu.cs683.myflickr.R
import edu.bu.cs683.myflickr.data.Photo
import edu.bu.cs683.myflickr.adapter.PhotosAdapter
import edu.bu.cs683.myflickr.data.PhotoRepository
import edu.bu.cs683.myflickr.databinding.FragmentImageGridBinding
import edu.bu.cs683.myflickr.viewmodel.ImagesViewModel

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

    var isGrid: Boolean = true

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
        loadPrefs()
        return binding.root
    }

    fun redraw() {
        val args = Bundle()
        args.putString(ImageGridFragment.ARG_USER_ID, userId)

        // also add the user id in the fragment
        parentFragmentManager.commit {
            replace<ImageGridFragment>(R.id.container, args = args)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listViewModel =
            ViewModelProvider(this).get(ImagesViewModel::class.java)

        userId?.let {
            loadImages()
        }

        binding.showAsGrid.setOnClickListener {
            isGrid = true
            savePrefs()
            redraw()
        }

        binding.showAsList.setOnClickListener {
            isGrid = false
            savePrefs()
            redraw()
        }

        binding.showAnalytics.setOnClickListener {
            // also add the user id in the fragment
            parentFragmentManager.commit {
                replace<StatsFragment>(R.id.container)
                addToBackStack(null)
            }
        }

        binding.showAppSettings.setOnClickListener {
            showAppOptions()
        }
    }


    @SuppressLint("UseSwitchCompatOrMaterialCode")
    fun showAppOptions() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.options_layout)

        val switchShowAsGrid = bottomSheetDialog.findViewById<Switch>(R.id.showAsGrid)

        switchShowAsGrid ?. let {
            it.isChecked = isGrid
            it.setOnCheckedChangeListener { _, b ->
                isGrid = b
                savePrefs()
                bottomSheetDialog.dismiss()
                redraw()
            }
        }

        bottomSheetDialog.findViewById<Switch>(R.id.showMetadata)?.setOnCheckedChangeListener { _, _ ->
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    fun savePrefs() {
        val sharedPreferences = activity?.getSharedPreferences(IMAGE_GRID_PREFS, Context.MODE_PRIVATE)

        sharedPreferences?.let {
            Log.i(TAG, "Saving $IMAGE_GRID_PREFS = $isGrid")
            it.edit()
                .putBoolean(IMAGE_IS_GRID, isGrid)
                .apply()
        }
    }

    fun loadPrefs() {
        val sharedPreferences = activity?.getSharedPreferences(IMAGE_GRID_PREFS, Context.MODE_PRIVATE)
        sharedPreferences?.let {
            isGrid = it.getBoolean(IMAGE_IS_GRID, true)
            Log.i(TAG, "Setting from $IMAGE_IS_GRID = $isGrid")
        }
    }

    override fun onPause() {
        super.onPause()

        savePrefs()
    }

    override fun onResume() {
        super.onResume()


    }

    /**
     * Gets the image grid column per row depending on orientation
     * of the phone screen
     */
    fun getGridColumnsPerRow(): Int {
        if (!isGrid)
            return 1

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
                searchParameters.media = "photos"
                val photos = photosInterface.search(searchParameters, GRID_PAGE_SIZE, 1)
                    .map { Photo(id = it.id, url = it.medium640Url, title = it.title) }
                    .toMutableList()

                photos.forEach {
                    PhotoRepository.get().add(it)
                }

                return photos
            }

            override fun onPostExecute(photos: MutableList<Photo>) {
                val listViewModel =
                    ViewModelProvider(this@ImageGridFragment).get(ImagesViewModel::class.java)

                listViewModel.setImagesList(photos)

                // build the recycler view and bind the adapter to it so we
                // can draw the individual images from the photo metadata
                // returned by the API
                recyclerView = binding.photosRecyclerView
                val columnCount = getGridColumnsPerRow()
                val layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                recyclerView.layoutManager = layoutManager
                recyclerView.adapter = PhotosAdapter(this@ImageGridFragment,
                    listViewModel.currentImagesList.value!!.toMutableList())

                binding.progress.visibility = View.GONE
            }
        }.execute()
    }

    companion object {
        const val IMAGE_GRID_PREFS = "ImageGridPrefs"
        const val IMAGE_IS_GRID = "ImagesIsGrid"
        const val ARG_USER_ID = "user_id"
        const val GRID_PAGE_SIZE = 50
        const val ROWS_PER_COLUMN_PORTRAIT = 2
        const val ROWS_PER_COLUMN_LANDSCAPE = 3

        val TAG: String = ImageGridFragment::class.java.simpleName
    }

    override fun getImageDetails(photo: Photo) {
        // Load the fragment to images activity
        // this passes the flickr image id to the one image fragment
        Log.i(TAG, "Loading image ${photo.id}")
        val args = Bundle()
        args.putString(OneImageFragment.ARG_IMAGE_ID, photo.id)
        parentFragmentManager.commit {
            replace<OneImageFragment>(R.id.container, args = args)
            addToBackStack(null)
        }
    }
}
