package edu.bu.cs683.myflickr.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.flickr4java.flickr.Flickr
import com.flickr4java.flickr.REST
import edu.bu.cs683.myflickr.BuildConfig
import edu.bu.cs683.myflickr.R
import edu.bu.cs683.myflickr.data.Photo
import edu.bu.cs683.myflickr.databinding.FragmentOneImageBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Fragment for one image
 *
 * @author dlegaspi@bu.edu
 */
class OneImageFragment : Fragment() {
    private var _binding: FragmentOneImageBinding? = null
    private val binding get() = _binding!!

    private var userId: String? = null
    private var imageId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get the image and user ids from the fragment arguments
        arguments?.let {
            userId = it.getString(ARG_USER_ID)
            imageId = it.getString(ARG_IMAGE_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentOneImageBinding.inflate(inflater, container, false)

        imageId?.let { loadImage() }

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // go to previous activity/fragment
        binding.oneImageView.setOnSingleFlingListener { _, _, _, _ ->
            Log.d(TAG, "fling detected")
            parentFragmentManager.popBackStack()
            true
        }
    }

    @SuppressLint("StaticFieldLeak")
    fun loadImage() {
        // this goes to call the flickr API to get the photo
        // with the specified ID then updated the ImageView on callback
        val getImageJob = CoroutineScope(Dispatchers.IO).async {
            val flickr = Flickr(BuildConfig.FLICKR_API_KEY, BuildConfig.FLICKR_API_SECRET, REST())
            val photosInterface = flickr.photosInterface

            photosInterface.getPhoto(imageId)
            val flickrPhoto = photosInterface.getPhoto(imageId)

            val photo = Photo(id = flickrPhoto.id, url = flickrPhoto.mediumUrl, title = flickrPhoto.title)
            return@async photo
        }

        CoroutineScope(Dispatchers.Main).launch {
            val photo = getImageJob.await()
            with(photo) {
                com.squareup.picasso.Picasso.get()
                    .load(url)
                    .into(binding.oneImageView)

                binding.oneImageDescText.text = title
            }
        }

    }

    companion object {
        const val ARG_USER_ID = "user_id"
        const val ARG_IMAGE_ID = "image_id"

        val TAG: String = OneImageFragment::class.java.simpleName
    }
}
