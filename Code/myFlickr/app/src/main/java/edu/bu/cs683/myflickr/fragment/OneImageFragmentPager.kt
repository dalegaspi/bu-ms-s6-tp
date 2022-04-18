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
import java.lang.Integer.max

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

    var maxPosition: Int = 0
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.oneImageViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {

                // Toast.makeText(activity, "Current Page is going to ${position}", Toast.LENGTH_SHORT).show()
                if (position > maxPosition)
                    imageId = nextImage()

                maxPosition = max(position, maxPosition)
                super.onPageSelected(position)
            }
        })
    }

    private fun nextImage(): String {
        return runBlocking {
            val getImageJob = CoroutineScope(Dispatchers.IO).async {
                val flickr =
                    Flickr(BuildConfig.FLICKR_API_KEY, BuildConfig.FLICKR_API_SECRET, REST())
                val photosInterface = flickr.photosInterface

                photosInterface.getPhoto(imageId)
                val context = photosInterface.getContext(imageId)

                return@async context.previousPhoto.id
            }.await()

            return@runBlocking getImageJob
        }
    }

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 1000

        override fun createFragment(position: Int): Fragment {
            val args = Bundle()
            args.putString(OneImageFragment.ARG_IMAGE_ID, imageId)
            val fragment = OneImageFragment()
            fragment.arguments = args

            return fragment
        }
    }
    companion object {
        const val ARG_USER_ID = "user_id"
        const val ARG_IMAGE_ID = "image_id"

        val TAG: String = OneImageFragmentPager::class.java.simpleName
    }
}
