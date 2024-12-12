package com.example.uts_map

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso

class PostDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        val tvDescription: TextView = findViewById(R.id.tvDescription)
        val llImagesContainer: LinearLayout = findViewById(R.id.llImagesContainer)

        val description = intent.getStringExtra("description")
        val imageUrls = intent.getStringArrayListExtra("imageUrls") ?: arrayListOf()

        // Tampilkan deskripsi
        tvDescription.text = description

        // Tambahkan setiap gambar ke dalam LinearLayout
        if (imageUrls.isNotEmpty()) {
            llImagesContainer.visibility = View.VISIBLE
            for (url in imageUrls) {
                val imageView = ImageView(this)
                imageView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16, 0, 16) // Margin antar gambar
                }
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                Picasso.get().load(url).into(imageView)
                llImagesContainer.addView(imageView)
            }
        } else {
            llImagesContainer.visibility = View.GONE
        }
    }
}
