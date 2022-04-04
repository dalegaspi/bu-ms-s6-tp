package edu.bu.cs683.myflickr

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.flickr4java.flickr.Flickr
import com.flickr4java.flickr.REST
import com.flickr4java.flickr.photos.SearchParameters
import com.github.scribejava.core.model.OAuth1AccessToken
import com.github.scribejava.core.model.OAuth1RequestToken
import com.github.scribejava.core.oauth.OAuth10aService
import edu.bu.cs683.myflickr.data.FlickrApiState


/**
 * Authentication fragment to deal with Flickr Auth
 *
 * @author dlegaspi@bu.edu
 */
class AuthFragment : Fragment() {

    val APIKEY = BuildConfig.FLICKR_API_KEY
    val APISECRET = BuildConfig.FLICKR_API_SECRET

    lateinit var webview: WebView
    lateinit var service: OAuth10aService
    lateinit var verifier: String
    lateinit var requestToken: OAuth1RequestToken
    lateinit var flickr: Flickr
    lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_auth, container, false)
    }

    @SuppressLint("StaticFieldLeak")
    fun getAccessToken() {
        object : AsyncTask<Void, Void, OAuth1AccessToken>() {
            override fun doInBackground(vararg p0: Void?): OAuth1AccessToken? {
                val accessToken =
                    service.getAccessToken(requestToken, verifier)

                val authInterface = flickr.authInterface.checkToken(accessToken)
                val user = authInterface.user
                userId = user.id
                Log.d(MainActivity.TAG, "user = ${user.realName}")

                // let's set our API state holder
                FlickrApiState.instance = FlickrApiState(user, flickr, accessToken)
                val photosInterface = flickr.photosInterface
                val searchParameters = SearchParameters()
                searchParameters.userId = user.id
                val photos = photosInterface.search(searchParameters, 5, 1)
                return accessToken
            }

            override fun onPostExecute(result: OAuth1AccessToken) {
                Toast.makeText(
                    activity,
                    "Token = " + result.token + "Secret = " + result.tokenSecret,
                    Toast.LENGTH_LONG
                ).show()
            }
        }.execute().get()
    }

    inner class FlickrGetAuthUrlTask(val activity: MainActivity) : AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg params: Void?): String? {

            flickr = Flickr(APIKEY, APISECRET, REST())
            val authInterface = flickr.authInterface
            authInterface
            requestToken = authInterface.getRequestToken("https://www.flickr.com/auth-72157720836323165")
            val authURL = service.getAuthorizationUrl(requestToken)

            return authURL
        }

        override fun onPostExecute(authURL: String?) {
            webview.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    // check for our custom callback protocol otherwise use default behavior
                    if (url != null) {
                        if (url.contains("oauth_verifier")) {
                            // if (url.startsWith("oauth")) {
                            // authorization complete hide webview for now.
                            webview.setVisibility(View.GONE)

                            val uri = Uri.parse(url)
                            verifier = uri.getQueryParameter("oauth_verifier")!!

                            getAccessToken() // i extract the token here

                            // launch to the Images Activity
                            val intent = Intent(activity, ImagesActivity::class.java)
                            intent.putExtra("user_id", userId)
                            startActivity(intent)
                            return true
                        }
                    }

                    return super.shouldOverrideUrlLoading(view, url)
                }
            }

            if (authURL != null) {
                webview.loadUrl(authURL)
            }
        }
    }


    companion object {
        val TAG: String = AuthFragment::class.java.simpleName
    }
}