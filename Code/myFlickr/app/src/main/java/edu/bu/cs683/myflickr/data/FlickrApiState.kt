package edu.bu.cs683.myflickr.data

import com.flickr4java.flickr.Flickr
import com.flickr4java.flickr.people.User
import com.github.scribejava.core.model.OAuth1AccessToken

/**
 * This is a _temporary_ location to store the OAuth access token for the Flickr API
 *
 * @author dlegaspi@bu.edu
 */
data class FlickrApiState(
    val user: User,
    val api: Flickr,
    val accessToken: OAuth1AccessToken
) {
    companion object {
        lateinit var instance: FlickrApiState
    }
}
