package edu.bu.cs683.myflickr

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import edu.bu.cs683.myflickr.fragment.ImageGridFragment

/**
 * This is where all the images stuff will be done; all the related fragments
 * will be using this activity
 *
 * @author dlegaspi@bu.edu
 */
class ImagesActivity : AppCompatActivity() {

    lateinit var userId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        // this gets the Flickr user id as parameter to this activity
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_images)
        userId = intent.extras?.get("user_id") as String

        val args = Bundle()
        args.putString(ImageGridFragment.ARG_USER_ID, userId)

        // also add the user id in the fragment
        supportFragmentManager.commit {
            replace<ImageGridFragment>(R.id.container, args = args)
        }

        // remove action bar and implement an (auto-hiding) toolbar instead
        supportActionBar?.hide()
    }
}
