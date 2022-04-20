package edu.bu.cs683.myflickr.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
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
import edu.bu.cs683.myflickr.MainActivity
import edu.bu.cs683.myflickr.MyFlickrApplication
import edu.bu.cs683.myflickr.R
import edu.bu.cs683.myflickr.adapter.EndlessRecyclerViewScrollListener
import edu.bu.cs683.myflickr.adapter.PhotosAdapter
import edu.bu.cs683.myflickr.data.FlickrRepository
import edu.bu.cs683.myflickr.data.Photo
import edu.bu.cs683.myflickr.data.PhotoRepository
import edu.bu.cs683.myflickr.databinding.FragmentImageGridBinding
import edu.bu.cs683.myflickr.listener.OneImageDetailListener
import edu.bu.cs683.myflickr.viewmodel.ImagesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

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

    var currentGridPage = 1

    private lateinit var flickrRepository: FlickrRepository
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
        flickrRepository = (activity?.application as MyFlickrApplication).flickrRepository
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
            loadImages(1)
        }

        recyclerView = binding.photosRecyclerView
        val columnCount = getGridColumnsPerRow()
        val layoutManager = when {
            columnCount <= 1 -> LinearLayoutManager(context)
            else -> GridLayoutManager(context, columnCount)
        }
        recyclerView.layoutManager = layoutManager
        recyclerView = binding.photosRecyclerView
        recyclerView.adapter = PhotosAdapter(
            this@ImageGridFragment,
            ArrayList()
        )

        binding.photosRecyclerView.addOnScrollListener(object : EndlessRecyclerViewScrollListener(layoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                // Toast.makeText(activity, "Loading page $page with $totalItemsCount.", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Loading page $page with $totalItemsCount")
                if (page > 1)
                    binding.progressMore.visibility = View.VISIBLE
                loadImages(page + 1)
            }
        })
        binding.swipeContainer.setOnRefreshListener {
            // binding.progress.visibility = View.VISIBLE
            binding.progressMore.visibility = View.VISIBLE
            loadImages(1)
            Toast.makeText(activity, "Image catalog retrieval complete.", Toast.LENGTH_SHORT).show()
            binding.swipeContainer.isRefreshing = false
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
        // unlike the typical UI hydration where we use fragments and view bindings
        // we're keeping the options dialog to do it the manual way to keep
        // it simple so we are back to using findViewById here
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

        val logoutButton = bottomSheetDialog.findViewById<Button>(R.id.logOutButton)
        logoutButton?.setOnClickListener {

            // clear the cookies and it will log out the user
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()

            Toast.makeText(
                activity,
                "User logged out.",
                Toast.LENGTH_LONG
            ).show()

            bottomSheetDialog.dismiss()

            // return to auth without backstack
            val intent = Intent(activity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        bottomSheetDialog.show()
    }

    private fun savePrefs() {
        val sharedPreferences = activity?.getSharedPreferences(IMAGE_GRID_PREFS, Context.MODE_PRIVATE)

        sharedPreferences?.let {
            Log.i(TAG, "Saving $IMAGE_GRID_PREFS = $isGrid")
            it.edit()
                .putBoolean(IMAGE_IS_GRID, isGrid)
                .apply()
        }
    }

    private fun loadPrefs() {
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

        loadPrefs()
    }

    /**
     * Gets the image grid column per row depending on orientation
     * of the phone screen
     */
    private fun getGridColumnsPerRow(): Int {
        if (!isGrid)
            return 1

        return if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            ROWS_PER_COLUMN_LANDSCAPE
        else
            ROWS_PER_COLUMN_PORTRAIT
    }

    private fun loadImages(page: Int) {

        val getImagesJob = CoroutineScope(Dispatchers.IO).async {
            //val flickr = Flickr(BuildConfig.FLICKR_API_KEY, BuildConfig.FLICKR_API_SECRET, REST())
           // val photosInterface = flickr.photosInterface
           // val searchParameters = SearchParameters()

           // searchParameters.userId = userId
           // searchParameters.media = "photos"
            //val photos = photosInterface.search(searchParameters, GRID_PAGE_SIZE, page)
           //     .map { Photo(id = it.id, url = it.medium640Url, title = it.title) }
            //    .toMutableList()

            val photos = flickrRepository.searchPhotos(page, GRID_PAGE_SIZE).toMutableList()

            photos.forEach {
                PhotoRepository.get().add(it)
            }

            return@async photos
        }

        CoroutineScope(Dispatchers.Main).launch {
            val photos = getImagesJob.await()

            val listViewModel =
                ViewModelProvider(this@ImageGridFragment).get(ImagesViewModel::class.java)

            val currentlist = listViewModel.currentImagesList.value
            val currentSize = listViewModel.currentImagesList.value?.size

            val newList = currentlist?.plus(photos) ?: photos
            listViewModel.setImagesList(currentlist?.plus(photos) ?: photos)

            // build the recycler view and bind the adapter to it so we
            // can draw the individual images from the photo metadata
            // returned by the API
            recyclerView = binding.photosRecyclerView
            (recyclerView.adapter as PhotosAdapter).photos.addAll(photos)

            if (page > 1)
                (recyclerView.adapter as PhotosAdapter).notifyItemRangeChanged(
                    currentSize!!,
                    listViewModel.currentImagesList.value!!.size - 1
                )

            binding.progress.visibility = View.GONE
            binding.progressMore.visibility = View.GONE
        }
    }

    companion object {
        const val IMAGE_GRID_PREFS = "ImageGridPrefs"
        const val IMAGE_IS_GRID = "ImagesIsGrid"
        const val ARG_USER_ID = "user_id"
        const val GRID_PAGE_SIZE = 12
        const val ROWS_PER_COLUMN_PORTRAIT = 2
        const val ROWS_PER_COLUMN_LANDSCAPE = 3

        val TAG: String = ImageGridFragment::class.java.simpleName
    }

    override fun getImageDetails(photo: Photo) {
        // Load the fragment to images activity
        // this passes the flickr image id to the one image fragment
        Log.i(TAG, "Loading image ${photo.id}")
        val args = Bundle()
        args.putString(OneImageFragmentPager.ARG_IMAGE_ID, photo.id)
        parentFragmentManager.commit {
            replace<OneImageFragmentPager>(R.id.container, args = args)
            addToBackStack(null)
        }
    }
}
