package com.example.uts_map

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class PostDetailActivity : AppCompatActivity() {

    // ActivityResultLauncher untuk EditPostActivity
    private lateinit var editPostLauncher: ActivityResultLauncher<Intent>

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
        val btnDelete: Button = findViewById(R.id.btnDelete)

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
                Picasso.get().load(url).into(imageView) // Load gambar menggunakan Picasso
                llImagesContainer.addView(imageView)
            }
        } else {
            llImagesContainer.visibility = View.GONE // Sembunyikan jika tidak ada gambar
        }

        // Inisialisasi ActivityResultLauncher untuk EditPostActivity
        editPostLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val updatedTitle = result.data?.getStringExtra("updatedTitle")
                val updatedDescription = result.data?.getStringExtra("updatedDescription")
                val updatedImageUrls = result.data?.getStringArrayListExtra("updatedImageUrls")

                // Perbarui UI dengan data terbaru
                supportActionBar?.title = updatedTitle
                tvDescription.text = updatedDescription

                // Perbarui gambar di LinearLayout
                llImagesContainer.removeAllViews()
                updatedImageUrls?.forEach { url ->
                    val imageView = ImageView(this)
                    imageView.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 16, 0, 16)
                    }
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    Picasso.get().load(url).into(imageView)
                    llImagesContainer.addView(imageView)
                }
            }
        }

        // Tombol Edit
        btnEdit.setOnClickListener {
            val intent = Intent(this, EditPostActivity::class.java)
            intent.putExtra("postId", postId) // Kirim postId
            intent.putExtra("title", postTitle) // Kirim judul
            intent.putExtra("description", description) // Kirim deskripsi
            intent.putStringArrayListExtra("imageUrls", ArrayList(imageUrls)) // Kirim URL gambar
            editPostLauncher.launch(intent) // Gunakan ActivityResultLauncher
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

    // Fungsi untuk menghapus postingan berdasarkan postId
    private fun deletePost(postId: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("posts").document(postId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Post deleted successfully", Toast.LENGTH_SHORT).show()
                val resultIntent = Intent()
                resultIntent.putExtra("postDeleted", true) // Kirim informasi bahwa post dihapus
                setResult(RESULT_OK, resultIntent) // Set hasil untuk HomeActivity
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
