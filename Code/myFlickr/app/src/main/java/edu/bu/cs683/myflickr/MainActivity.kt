package edu.bu.cs683.myflickr

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Entry point
 *
 * @author dlegaspi@bu.edu
 */
class MainActivity : AppCompatActivity(R.layout.activity_main) {
    companion object {
        val TAG: String = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // remove action bar
        supportActionBar?.hide()
    }
}
