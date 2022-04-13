package edu.bu.cs683.myflickr.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.flickr4java.flickr.Flickr
import com.flickr4java.flickr.REST
import com.github.scribejava.apis.FlickrApi
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.model.OAuth1AccessToken
import com.github.scribejava.core.model.OAuth1RequestToken
import com.github.scribejava.core.oauth.OAuth10aService
import edu.bu.cs683.myflickr.BuildConfig
import edu.bu.cs683.myflickr.ImagesActivity
import edu.bu.cs683.myflickr.MainActivity
import edu.bu.cs683.myflickr.R
import edu.bu.cs683.myflickr.data.FlickrApiState
import edu.bu.cs683.myflickr.databinding.FragmentAuthBinding
/**
 * Authentication fragment to deal with Flickr Auth
 *
 * @author dlegaspi@bu.edu
 */
class AuthFragment : Fragment() {

    val apiKey = BuildConfig.FLICKR_API_KEY
    val apiSecret = BuildConfig.FLICKR_API_SECRET

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    lateinit var webview: WebView
    lateinit var service: OAuth10aService
    lateinit var verifier: String
    lateinit var requestToken: OAuth1RequestToken
    lateinit var flickr: Flickr
    lateinit var userId: String

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_auth, container, false)
        _binding = FragmentAuthBinding.inflate(inflater, container, false)

        webview = binding.authWebView

        // these next 2 are very important otherwise flickr website will not
        // be able to load the OAuth pages and for the webview be able to intercept
        // the OAuth tokens
        webview.settings.javaScriptEnabled = true
        webview.settings.domStorageEnabled = true
        service = ServiceBuilder(apiKey)
            .apiSecret(apiSecret)
            .build(FlickrApi.instance(FlickrApi.FlickrPerm.READ))

        // launch the webview to authenticate the user and/or authorize the app
        // to get the users information and images
        FlickrGetAuthUrlTask(requireActivity()).execute()

        return binding.root
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
                // uncomment the following lines to test the API calls working
                // val photosInterface = flickr.photosInterface
                // val searchParameters = SearchParameters()
                // searchParameters.userId = user.id
                // val photos = photosInterface.search(searchParameters, 5, 1)
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

    @SuppressLint("StaticFieldLeak")
    inner class FlickrGetAuthUrlTask(val activity: FragmentActivity) : AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg params: Void?): String? {

            flickr = Flickr(apiKey, apiSecret, REST())
            val authInterface = flickr.authInterface

            // we are testing that the API works with the specified API Keys
            // if it fails we set authURL to null and display the error by replacing
            // the current fragment with AuthErrorFragment
            val authURL = kotlin.runCatching {
                requestToken = authInterface.getRequestToken("https://www.flickr.com/auth-72157720836323165")
                service.getAuthorizationUrl(requestToken)
            }

            return authURL.getOrNull()
        }

        override fun onPostExecute(authURL: String?) {
            authURL?.let {
                webview.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        // check for our custom callback protocol otherwise use default behavior
                        if (url != null) {
                            if (url.contains("oauth_verifier")) {
                                // authorization complete hide web view
                                webview.visibility = View.GONE

                                val uri = Uri.parse(url)
                                verifier = uri.getQueryParameter("oauth_verifier")!!

                                // we extract the token here
                                getAccessToken()

                                // launch to the Images Activity and leave the current activity
                                val intent = Intent(activity, ImagesActivity::class.java)
                                intent.putExtra("user_id", userId)

                                // remove from back stack
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                                return true
                            }
                        }

                        return super.shouldOverrideUrlLoading(view, url)
                    }
                }

                webview.loadUrl(authURL)
            } ?: run {
                Log.e(TAG, "API Authentication failed due to missing/invalid Flickr API key.")

                // this displays the error for any API-related issues (invalid key, etc)
                parentFragmentManager.commit {
                    replace<AuthErrorFragment>(R.id.container)
                }
            }
        }
    }

    companion object {
        val TAG: String = AuthFragment::class.java.simpleName
    }
}
