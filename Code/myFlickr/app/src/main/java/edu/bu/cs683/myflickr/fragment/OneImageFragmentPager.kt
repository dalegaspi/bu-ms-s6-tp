package edu.bu.cs683.myflickr.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.flickr4java.flickr.Flickr
import com.flickr4java.flickr.REST
import edu.bu.cs683.myflickr.BuildConfig
import edu.bu.cs683.myflickr.databinding.FragmentOneImagePagerBinding
import kotlinx.coroutines.*

/**
 * Fragment for one image
 *
 * @author dlegaspi@bu.edu
 */
class OneImageFragmentPager : Fragment() {
    private var _binding: FragmentOneImagePagerBinding? = null
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
        _binding = FragmentOneImagePagerBinding.inflate(inflater, container, false)

        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = ScreenSlidePagerAdapter(requireActivity())
        binding.oneImageViewPager.adapter = pagerAdapter

        return binding.root
    }

    val imagesBrowsed: MutableMap<Int, Pair<String, OneImageFragment>> = mutableMapOf()
    fun processImageOnPosition(position: Int) {
        // Toast.makeText(activity, "Current Page is going to ${position}; maxPosition is ${maxPosition}", Toast.LENGTH_SHORT).show()

        if (imagesBrowsed.containsKey(position)) {
            imageId = imagesBrowsed[position]?.first
        } else {
            val jobGetOneImage = CoroutineScope(Dispatchers.IO).async {
                val nextImage = nextImage()
                val args = Bundle()
                args.putString(OneImageFragment.ARG_IMAGE_ID, imageId)
                val fragment = OneImageFragment()
                fragment.arguments = args

                imagesBrowsed[position] = Pair(nextImage, fragment)
                imageId = nextImage
            }

            // i don't like this part but the issue here is that ScreenSlidePagerAdapter::createFragment
            // is called before this is finished; so we have to block :-( I probably should spend
            // more time at some point to understand this ViewPager better to avoid this
            runBlocking {
                jobGetOneImage.await()
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        processImageOnPosition(0)
        binding.oneImageViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                processImageOnPosition(position)
                super.onPageSelected(position)
            }
        })
    }

    suspend fun nextImage(): String {
        // Toast.makeText(activity, "Get next photo called", Toast.LENGTH_SHORT).show()

        val getImageJob = CoroutineScope(Dispatchers.IO).async {
            val flickr =
                Flickr(BuildConfig.FLICKR_API_KEY, BuildConfig.FLICKR_API_SECRET, REST())
            val photosInterface = flickr.photosInterface

            photosInterface.getPhoto(imageId)
            val context = photosInterface.getContext(imageId)

            return@async context.previousPhoto.id
        }

        return getImageJob.await()
    }

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 1000

        override fun createFragment(position: Int): Fragment {

            processImageOnPosition(position)

            return imagesBrowsed[position]!!.second
        }
    }

    companion object {
        const val ARG_USER_ID = "user_id"
        const val ARG_IMAGE_ID = "image_id"

        val TAG: String = OneImageFragmentPager::class.java.simpleName
    }
}
