package edu.bu.cs683.myflickr.data

import com.flickr4java.flickr.Flickr
import com.flickr4java.flickr.REST
import com.flickr4java.flickr.RequestContext
import com.flickr4java.flickr.auth.Auth
import com.flickr4java.flickr.auth.AuthInterface
import com.flickr4java.flickr.people.User
import com.flickr4java.flickr.photos.Exif
import com.flickr4java.flickr.photos.PhotosInterface
import com.flickr4java.flickr.photos.SearchParameters
import com.flickr4java.flickr.util.FileAuthStore
import com.github.scribejava.apis.FlickrApi
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.model.OAuth1AccessToken
import com.github.scribejava.core.oauth.OAuth10aService
import edu.bu.cs683.myflickr.BuildConfig
import edu.bu.cs683.myflickr.fragment.ImageGridFragment
import java.io.File
import javax.inject.Inject

/**
 * Flickr API
 *
 * @author dlegaspi@bu.edu
 */
class FlickrRepository @Inject constructor() {
    private val apiKey = BuildConfig.FLICKR_API_KEY
    private val apiSecret = BuildConfig.FLICKR_API_SECRET
    val service: OAuth10aService = ServiceBuilder(apiKey)
        .apiSecret(apiSecret)
        .build(FlickrApi.instance(FlickrApi.FlickrPerm.READ))
    val flickr: Flickr = Flickr(apiKey, apiSecret, REST())
    val authInterface: AuthInterface = flickr.authInterface
    val photosInterface: PhotosInterface = flickr.photosInterface

    var _authStore: FileAuthStore? = null
    val authStore get() = _authStore!!

    private var _authToken: OAuth1AccessToken? = null
    val authToken get() = _authToken!!

    private var _user: User? = null
    val user get() = _user!!

    private var _oAuthVerifier: String? = null
    fun setOAuthVerifier(verifier: String) {
        _oAuthVerifier = verifier
    }

    private var _auth: Auth? = null
    val auth get() = _auth!!

    private var _baseDir: File? = null
    val baseDir get() = _baseDir!!
    fun setBaseDir(baseDir: File) {
        _authStore = FileAuthStore(File(baseDir, AUTHS_DIR))
    }

    fun setSession(token: OAuth1AccessToken, user: User) {
        _authToken = token
        _user = user
        storeAuth()
    }

    fun hasActiveSession() = _auth != null

    fun clearSession() {
        _authToken = null
        _user = null
        authStore.clearAll()
    }

    private fun storeAuth() {
        _auth = authInterface.checkToken(authToken)
        authStore.store(auth)
        RequestContext.getRequestContext().auth = auth
    }

    fun hydrateAuth(userId: String) {
        _auth = authStore.retrieve(userId)
        _user = auth.user
        RequestContext.getRequestContext().auth = auth
    }

    fun getPhoto(imageId: String): Photo {
        val flickrPhoto = photosInterface.getPhoto(imageId)
        val exif = photosInterface.getExif(imageId, apiSecret)

        val photo = toPhoto(flickrPhoto, exif)

        return photo
    }



    fun searchPhotos(page: Int, size: Int): List<Photo> {
        val searchParameters = SearchParameters()

        searchParameters.userId = user.id
        searchParameters.media = "photos"
        val photos = photosInterface.search(searchParameters, size, page)
            .map {
                val exif = photosInterface.getExif(it.id, apiSecret)
                toPhoto(it, exif)
            }

        return photos

    }

    companion object {
        const val AUTHS_DIR = "myFlickr"

        fun toPhoto(flickrPhoto: com.flickr4java.flickr.photos.Photo, exif: Collection<Exif> ): Photo {
            return Photo(id = flickrPhoto.id,
                url = flickrPhoto.mediumUrl,
                title = flickrPhoto.title,
                description = flickrPhoto.description,
                camera = exif.find { it.tag == "Model"}?.raw,
                lens = exif.find { it.tag == "LensModel"}?.raw,
                shutterSpeed = exif.find { it.tag == "ExposureTime"}?.clean,
                aperture = exif.find { it.tag == "FNumber"}?.clean,
                whiteBalance = exif.find { it.tag == "WhiteBalance"}?.raw,
                flash = exif.find { it.tag == "Flash"}?.raw
            )
        }
    }
}
