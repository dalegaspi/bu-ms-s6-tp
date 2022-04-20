package edu.bu.cs683.myflickr.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import edu.bu.cs683.myflickr.R
import edu.bu.cs683.myflickr.data.Photo
import edu.bu.cs683.myflickr.databinding.PhotoBinding
import edu.bu.cs683.myflickr.listener.OneImageDetailListener

class PhotosAdapter(
    val oneImageDetailListener: OneImageDetailListener,
    val photos: MutableList<Photo> = mutableListOf()
) : RecyclerView.Adapter<PhotosAdapter.PhotosViewHolder>() {
    val IMAGE_SIDE_PX = 400

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotosViewHolder {

        val binding = PhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotosViewHolder(binding)
    }

    override fun getItemCount(): Int = photos.size

    override fun onBindViewHolder(holder: PhotosViewHolder, position: Int) {
        with(holder) {
            with(photos[position]) {
                // Using Picasso we are pulling the image down and updating the ImagView
                // in one go.
                Picasso.get()
                    .load(url)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.image_outline)
                    .into(binding.imageView)
            }

            contentView.setOnClickListener {
                oneImageDetailListener.getImageDetails(photos[position])
            }
        }
    }

    inner class PhotosViewHolder(val binding: PhotoBinding) : RecyclerView.ViewHolder(binding.root) {
        val contentView = binding.imageView
    }
}
