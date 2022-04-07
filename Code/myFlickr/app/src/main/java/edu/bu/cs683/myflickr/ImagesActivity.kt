package edu.bu.cs683.myflickr

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
/**
 * This is where all the images stuff will be done; all the related fragments
 * will be using this activity
 *
 * @author dlegaspi@bu.edu
 */
class ImagesActivity : AppCompatActivity() {

    lateinit var userId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_images)
        userId = intent.extras?.get("user_id") as String

        val args = Bundle()
        args.putString(ImageGridFragment.ARG_USER_ID, userId)
        supportFragmentManager.commit {
            replace<ImageGridFragment>(R.id.container, args = args)
        }

        // remove title bar
        supportActionBar?.hide()
    }
}
