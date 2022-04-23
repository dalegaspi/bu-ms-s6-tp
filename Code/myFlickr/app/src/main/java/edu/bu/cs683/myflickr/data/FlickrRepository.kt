package edu.bu.cs683.myflickr.data

import android.webkit.CookieManager
import com.flickr4java.flickr.Flickr
import com.flickr4java.flickr.REST
import com.flickr4java.flickr.RequestContext
import com.flickr4java.flickr.auth.Auth
import com.flickr4java.flickr.auth.AuthInterface
import com.flickr4java.flickr.people.User
import com.flickr4java.flickr.photos.Exif
import com.flickr4java.flickr.photos.PhotoContext
import com.flickr4java.flickr.photos.PhotosInterface
import com.flickr4java.flickr.photos.SearchParameters
import com.flickr4java.flickr.stats.Totals
import com.flickr4java.flickr.util.FileAuthStore
import com.github.scribejava.apis.FlickrApi
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.model.OAuth1AccessToken
import com.github.scribejava.core.oauth.OAuth10aService
import edu.bu.cs683.myflickr.BuildConfig
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

    var avatarUrl = "https://www.flickr.com/images/buddyicon.gif"

    fun setSession(token: OAuth1AccessToken, user: User) {
        _authToken = token
        _user = user
        storeAuth()
        setAvatarUrl()
    }

    fun hasActiveSession() = _auth != null

    fun clearSession() {
        _authToken = null
        _user = null
        authStore.clearAll()
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
    }

    private fun storeAuth() {
        _auth = authInterface.checkToken(authToken)
        authStore.store(auth)
        RequestContext.getRequestContext().auth = auth
    }

    fun setAvatarUrl() {
        _user = this.flickr.peopleInterface.getInfo(user.id)
        avatarUrl = user.secureBuddyIconUrl
    }

    fun hydrateAuth(userId: String) {
        _auth = authStore.retrieve(userId)
        _user = auth.user
        RequestContext.getRequestContext().auth = auth
        setAvatarUrl()
    }

    fun getPhotoWithFullMetadata(imageId: String): Photo {
        RequestContext.getRequestContext().auth = auth
        val flickrPhoto = photosInterface.getPhoto(imageId)
        val exif = photosInterface.getExif(imageId, apiSecret)

        val photo = toPhoto(flickrPhoto, exif)

        return photo
    }

    fun getPhotoWithBasicMetadata(imageId: String): Photo {
        val flickrPhoto = photosInterface.getPhoto(imageId)
        val photo = toPhoto(flickrPhoto, emptyList())

        return photo
    }

    fun getFullMetadataForPhotos(images: List<Photo>): List<Photo> {
        RequestContext.getRequestContext().auth = auth
        val photos = images.map {
            val exif = photosInterface.getExif(it.id, apiSecret)
            addExifToPhoto(it, exif)
        }

        return photos
    }

    fun getContext(imageId: String): PhotoContext? {
        RequestContext.getRequestContext().auth = auth
        return photosInterface.getContext(imageId)
    }

    fun searchPhotos(page: Int, size: Int): List<Photo> {
        RequestContext.getRequestContext().auth = auth
        val searchParameters = SearchParameters()

        searchParameters.userId = user.id
        searchParameters.media = "photos"
        val photos = photosInterface.search(searchParameters, size, page)
            .map {
                toPhoto(it, emptyList())
            }

        return photos
    }

    fun getViewStats(): Totals? {
        RequestContext.getRequestContext().auth = auth
        val stats = flickr.statsInterface.getTotalViews(null)
        return stats
    }

    fun getTopFivePopularPhotos(): List<Pair<Photo, Int>> {
        RequestContext.getRequestContext().auth = auth
        val popular = flickr.statsInterface.getPopularPhotos(null, null, 5, 1)
        return popular.map {
            val flickrPhoto = photosInterface.getPhoto(it.id)
            Pair(toPhoto(flickrPhoto, emptyList()), it.views)
        }
    }

    companion object {
        const val AUTHS_DIR = "myFlickr"

        fun addExifToPhoto(photo: Photo, exif: Collection<Exif>): Photo {
            return photo.copy(
                camera = exif.find { it.tag == "Model" }?.raw,
                lens = exif.find { it.tag == "LensModel" }?.raw,
                shutterSpeed = exif.find { it.tag == "ExposureTime" }?.clean,
                aperture = exif.find { it.tag == "FNumber" }?.clean,
                whiteBalance = exif.find { it.tag == "WhiteBalance" }?.raw,
                flash = exif.find { it.tag == "Flash" }?.raw
            )
        }

        fun toPhoto(flickrPhoto: com.flickr4java.flickr.photos.Photo, exif: Collection<Exif>): Photo {
            return addExifToPhoto(
                Photo(
                    id = flickrPhoto.id,
                    url = flickrPhoto.mediumUrl,
                    title = flickrPhoto.title,
                    description = flickrPhoto.description,
                    isPublic = flickrPhoto.isPublicFlag
                ),
                exif
            )
        }
    }
}
