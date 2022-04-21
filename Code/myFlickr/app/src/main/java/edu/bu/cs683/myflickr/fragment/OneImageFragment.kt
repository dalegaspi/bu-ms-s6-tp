package edu.bu.cs683.myflickr.fragment

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import edu.bu.cs683.myflickr.MyFlickrApplication
import edu.bu.cs683.myflickr.data.FlickrRepository
import edu.bu.cs683.myflickr.databinding.FragmentOneImageBinding
import edu.bu.cs683.myflickr.viewmodel.ImageViewModel
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
    private lateinit var flickrRepository: FlickrRepository
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

    override fun onResume() {
        super.onResume()
        val oneViewModel =
            ViewModelProvider(requireActivity()).get(ImageViewModel::class.java)
        oneViewModel.showMetadata.value ?.let {
            binding.showMetadata.isChecked = it
            binding.metadataGrid.visibility = if (it) View.VISIBLE else View.GONE
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentOneImageBinding.inflate(inflater, container, false)
        flickrRepository = (activity?.application as MyFlickrApplication).flickrRepository

        imageId?.let { loadImage() }

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val oneViewModel =
            ViewModelProvider(requireActivity()).get(ImageViewModel::class.java)

        // https://stackoverflow.com/a/29955621/918858
        val linearLayout = binding.mainlayout
        linearLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        val gridLayout = binding.metadataGrid
        gridLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        // go to previous activity/fragment
        binding.oneImageView.setOnSingleFlingListener { _, _, _, _ ->
            Log.d(TAG, "fling detected")
            parentFragmentManager.popBackStack()
            true
        }

        binding.showMetadata.setOnCheckedChangeListener { _, b ->
            binding.metadataGrid.visibility = if (b) View.VISIBLE else View.GONE
            oneViewModel.setShowMetadata(b)
        }

        oneViewModel.showMetadata.value ?.let {
            binding.showMetadata.isChecked = it
            binding.metadataGrid.visibility = if (it) View.VISIBLE else View.GONE
        }
    }

    @SuppressLint("StaticFieldLeak")
    fun loadImage() {
        // this goes to call the flickr API to get the photo
        // with the specified ID then updated the ImageView on callback
        binding.oneImageProgress.visibility = View.VISIBLE
        val getImageJob = CoroutineScope(Dispatchers.IO).async {

            val photo = imageId!!.let { flickrRepository.getPhotoWithFullMetadata(it) }
            return@async photo
        }
        binding.metadataGrid.visibility = View.INVISIBLE
        CoroutineScope(Dispatchers.Main).launch {
            val photo = getImageJob.await()
            with(photo) {
                com.squareup.picasso.Picasso.get()
                    .load(url)
                    .into(binding.oneImageView)
                description?. let {
                    binding.oneImageDesc.text = it
                }

                binding.oneImageDescText.text = title
                binding.oneImageCamera.text = camera
                binding.oneImageAperture.text = aperture
                binding.oneImageShutterSpeed.text = shutterSpeed
                binding.oneImageWhiteBalance.text = whiteBalance
                binding.oneImageFlash.text = flash
                binding.oneImageLens.text = lens
                binding.oneImageProgress.visibility = View.VISIBLE
                if (!isPublic)
                    binding.imagePrivate.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        const val ARG_USER_ID = "user_id"
        const val ARG_IMAGE_ID = "image_id"

        val TAG: String = OneImageFragment::class.java.simpleName
    }
}
