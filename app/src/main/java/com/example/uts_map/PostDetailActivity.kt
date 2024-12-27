package com.example.uts_map

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
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
        val btnEdit: Button = findViewById(R.id.btnEdit) // Inisialisasi tombol Edit

        // Mendapatkan data dari Intent
        val description = intent.getStringExtra("description")
        val imageUrls = intent.getStringArrayListExtra("imageUrls") ?: arrayListOf()
        val postTitle = intent.getStringExtra("title") // Mendapatkan judul
        val postId = intent.getStringExtra("postId") // Mendapatkan postId

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

        // Tombol Edit
        btnEdit.setOnClickListener {
            val intent = Intent(this, EditPostActivity::class.java)
            intent.putExtra("postId", postId) // Mengirim postId
            intent.putExtra("title", postTitle) // Mengirim title
            intent.putExtra("description", description) // Mengirim description
            intent.putStringArrayListExtra("imageUrls", imageUrls) // Mengirim imageUrls
            startActivity(intent)
        }
    }

    // Fungsi untuk menangani tombol back
    override fun onSupportNavigateUp(): Boolean {
        finish() // Kembali ke halaman sebelumnya
        return true
    }
}
