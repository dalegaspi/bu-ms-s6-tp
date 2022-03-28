package edu.bu.cs683.myflickr

import android.annotation.SuppressLint
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.flickr4java.flickr.Flickr
import com.flickr4java.flickr.REST
import com.github.scribejava.apis.FlickrApi
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.model.OAuth1AccessToken
import com.github.scribejava.core.model.OAuth1RequestToken
import com.github.scribejava.core.model.OAuthRequest
import com.github.scribejava.core.model.Verb
import com.github.scribejava.core.oauth.OAuth10aService

/**
 * @author dlegaspi@bu.edu
 */
class MainActivity : AppCompatActivity() {

    val APIKEY = BuildConfig.FLICKR_API_KEY
    val APISECRET = BuildConfig.FLICKR_API_SECRET

    lateinit var webview: WebView
    lateinit var service: OAuth10aService
    lateinit var verifier: String
    lateinit var requestToken: OAuth1RequestToken
    lateinit var accessToken: OAuth1AccessToken
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webview = findViewById<WebView>(R.id.authWebView)
        webview.settings.javaScriptEnabled = true
        webview.settings.domStorageEnabled = true
        service = ServiceBuilder(APIKEY)
            .apiSecret(APISECRET)
            .build(FlickrApi.instance(FlickrApi.FlickrPerm.DELETE))
        FlickrGetAuthUrlTask().execute()
    }

    @SuppressLint("StaticFieldLeak")
    fun getAccessToken() {
        object : AsyncTask<Void, Void, OAuth1AccessToken>() {
            override fun doInBackground(vararg p0: Void?): OAuth1AccessToken? {
                accessToken =
                    service.getAccessToken(requestToken, verifier)

                val request = OAuthRequest(Verb.GET, "https://api.flickr.com/services/rest/")
                request.addQuerystringParameter("method", "flickr.test.login")
                request.addQuerystringParameter("format", "json")
                request.addQuerystringParameter("nojsoncallback", "1")
                service.signRequest(accessToken, request)
                val response = service.execute(request).body
                Log.d(TAG, "user = $response")
                return accessToken
            }

            override fun onPostExecute(result: OAuth1AccessToken) {
                Toast.makeText(
                    applicationContext,
                    "Token = " + result.token + "Secret = " + result.tokenSecret,
                    Toast.LENGTH_LONG
                ).show()
            }
        }.execute()
    }

    inner class FlickrGetAuthUrlTask : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg params: Void?): String? {

            val flickr = Flickr(APIKEY, APISECRET, REST())
            val authInterface = flickr.authInterface
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

                            getAccessToken()

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
        val TAG: String = MainActivity::class.java.simpleName
    }
}
