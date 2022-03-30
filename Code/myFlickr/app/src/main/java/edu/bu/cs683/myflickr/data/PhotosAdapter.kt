package edu.bu.cs683.myflickr.data

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import edu.bu.cs683.myflickr.databinding.PhotoBinding

class PhotosAdapter(val photos: MutableList<Photo> = mutableListOf()) : RecyclerView.Adapter<PhotosAdapter.PhotosViewHolder>() {
    val IMAGE_SIDE_PX = 400

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotosViewHolder {

        val binding = PhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotosViewHolder(binding)
    }

    override fun getItemCount(): Int = photos.size

    override fun onBindViewHolder(holder: PhotosViewHolder, position: Int) {
        with(holder) {
            with(photos[position]) {
                Picasso.get()
                    .load(url)
                    .into(binding.imageView)
            }
        }
    }

    inner class PhotosViewHolder(val binding: PhotoBinding) : RecyclerView.ViewHolder(binding.root)
}
