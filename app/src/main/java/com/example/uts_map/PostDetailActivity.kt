package com.example.uts_map

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore
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
        val btnEdit: Button = findViewById(R.id.btnEdit)
        val btnDelete: Button = findViewById(R.id.btnDelete) // Inisialisasi tombol Delete

        // Mendapatkan data dari Intent
        val description = intent.getStringExtra("description")
        val imageUrls = intent.getStringArrayListExtra("imageUrls") ?: arrayListOf()
        val postTitle = intent.getStringExtra("title")
        val postId = intent.getStringExtra("postId")

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
            intent.putExtra("postId", postId)
            intent.putExtra("title", postTitle)
            intent.putExtra("description", description)
            intent.putStringArrayListExtra("imageUrls", imageUrls)
            startActivity(intent)
        }

        // Tombol Delete
        btnDelete.setOnClickListener {
            if (postId != null) {
                deletePost(postId)
            } else {
                Toast.makeText(this, "Post ID not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deletePost(postId: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("posts").document(postId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Post deleted successfully", Toast.LENGTH_SHORT).show()
                val resultIntent = Intent()
                resultIntent.putExtra("postDeleted", true) // Kirim informasi bahwa post dihapus
                setResult(RESULT_OK, resultIntent) // Beri tanda bahwa operasi berhasil
                finish() // Kembali ke HomeActivity
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to delete post: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Fungsi untuk menangani tombol back
    override fun onSupportNavigateUp(): Boolean {
        finish() // Kembali ke halaman sebelumnya
        return true
    }
}