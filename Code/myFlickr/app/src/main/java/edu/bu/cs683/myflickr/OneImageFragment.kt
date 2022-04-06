package edu.bu.cs683.myflickr

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.flickr4java.flickr.Flickr
import com.flickr4java.flickr.REST
import com.squareup.picasso.Picasso
import edu.bu.cs683.myflickr.data.Photo
import edu.bu.cs683.myflickr.databinding.FragmentOneImageBinding

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
        arguments?.let {
            userId = it.getString(OneImageFragment.ARG_USER_ID)
            imageId = it.getString(OneImageFragment.ARG_IMAGE_ID)
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

    @SuppressLint("StaticFieldLeak")
    fun loadImage() {
        object : AsyncTask<Void, Void, Photo>() {
            override fun doInBackground(vararg p0: Void?): Photo {
                val flickr = Flickr(BuildConfig.FLICKR_API_KEY, BuildConfig.FLICKR_API_SECRET, REST())
                val photosInterface = flickr.photosInterface

                photosInterface.getPhoto(imageId)
                val flickrPhoto = photosInterface.getPhoto(imageId)
                val photo = Photo(id = flickrPhoto.id, url = flickrPhoto.mediumUrl, title = flickrPhoto.title)
                return photo
            }

            override fun onPostExecute(photo: Photo) {
                with(photo) {
                    Picasso.get()
                        .load(url)
                        .into(binding.oneImageView)

                    binding.oneImageDescText.text = title
                }
            }
        }.execute()
    }

    companion object {
        const val ARG_USER_ID = "user_id"
        const val ARG_IMAGE_ID = "image_id"

        val TAG: String = OneImageFragment::class.java.simpleName
    }
}
