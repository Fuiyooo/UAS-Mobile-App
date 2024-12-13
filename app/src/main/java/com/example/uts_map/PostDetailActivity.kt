package com.example.uts_map

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.squareup.picasso.Picasso

class PostDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        // Inisialisasi Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Menampilkan tombol back
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back) // Icon back

        // Inisialisasi Views
        val tvDescription: TextView = findViewById(R.id.tvDescription)
        val llImagesContainer: LinearLayout = findViewById(R.id.llImagesContainer)

        // Mendapatkan data dari Intent
        val description = intent.getStringExtra("description")
        val imageUrls = intent.getStringArrayListExtra("imageUrls") ?: arrayListOf()
        val postTitle = intent.getStringExtra("title") // Mendapatkan judul

        // Atur judul Toolbar
        supportActionBar?.title = postTitle

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

    // Fungsi untuk menangani tombol back
    override fun onSupportNavigateUp(): Boolean {
        finish() // Kembali ke halaman sebelumnya
        return true
    }
}
