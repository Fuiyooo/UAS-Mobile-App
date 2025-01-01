package com.example.uts_map

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class ImageAdapter(
    private val images: List<Uri> // Daftar URI gambar
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    // ViewHolder untuk item gambar
    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uri = images[position]

        // Cek apakah URI adalah URL Firebase atau URI lokal
        if (uri.toString().startsWith("https://firebasestorage.googleapis.com")) {
            // URL Firebase, gunakan Picasso untuk memuat gambar
            Picasso.get().load(uri.toString()).into(holder.imageView)
        } else {
            // URI lokal, gunakan ImageView langsung
            holder.imageView.setImageURI(uri)
        }
    }

    override fun getItemCount(): Int {
        return images.size
    }
}
