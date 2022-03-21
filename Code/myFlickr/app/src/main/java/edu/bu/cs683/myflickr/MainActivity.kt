package edu.bu.cs683.myflickr

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * @author dlegaspi@bu.edu
 */
class MainActivity : AppCompatActivity() {

    val APIKEY = "9f6312e4c2267993ed1555c1e53b7e9a"
    val APISECRET = "da07bc0f98be24dd"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
