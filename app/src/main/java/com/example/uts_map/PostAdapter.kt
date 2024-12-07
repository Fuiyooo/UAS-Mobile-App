package com.example.uts_map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class PostAdapter(private val posts: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val ivPostImage: ImageView = itemView.findViewById(R.id.ivPostImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // Menampilkan deskripsi
        holder.tvDescription.text = post.description

        // Jika ada gambar, tampilkan gambar menggunakan Picasso
        if (post.imageUrl != null) {
            holder.ivPostImage.visibility = View.VISIBLE
            Picasso.get().load(post.imageUrl).into(holder.ivPostImage)
        } else {
            holder.ivPostImage.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return posts.size

    }

}
