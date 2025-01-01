package com.example.uts_map

import Post
import com.example.uts_map.TitleAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var rvTitles: RecyclerView
    private lateinit var titleAdapter: TitleAdapter
    private lateinit var firestore: FirebaseFirestore
    private var postList = mutableListOf<Post>()

    private lateinit var postDetailLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Inisialisasi Firestore
        firestore = FirebaseFirestore.getInstance()

        // Inisialisasi RecyclerView
        rvTitles = findViewById(R.id.rvTitles)
        rvTitles.layoutManager = LinearLayoutManager(this)

        // Inisialisasi Adapter dengan onItemClick
        titleAdapter = TitleAdapter(postList) { post ->
            // Aksi saat judul diklik
            val intent = Intent(this, PostDetailActivity::class.java)
            intent.putExtra("postId", post.postId) // Kirimkan postId
            intent.putExtra("title", post.title)
            intent.putExtra("description", post.description)
            intent.putStringArrayListExtra("imageUrls", ArrayList(post.imageUrls))
            postDetailLauncher.launch(intent) // Gunakan launcher
        }
        rvTitles.adapter = titleAdapter

        // Inisialisasi ActivityResultLauncher untuk menangkap hasil dari PostDetailActivity
        postDetailLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val isDeleted = result.data?.getBooleanExtra("postDeleted", false) ?: false
                if (isDeleted) {
                    loadTitlesFromFirestore() // Muat ulang data setelah penghapusan
                }
            }
        }

        // Ambil data postingan dari Firestore
        loadTitlesFromFirestore()

        // Inisialisasi Bottom Navigation
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Event listener untuk navigasi
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_post -> {
                    startActivity(Intent(this, PostActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        bottomNavigation.selectedItemId = R.id.nav_home
    }

    private fun loadTitlesFromFirestore() {
        firestore.collection("posts")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    postList.clear()
                    for (document in documents) {
                        try {
                            val post = Post(
                                postId = document.id, // Ambil ID dokumen sebagai postId
                                title = document.getString("title") ?: "Untitled",
                                description = document.getString("description") ?: "",
                                imageUrls = document["imageUrls"] as? List<String> ?: emptyList()
                            )
                            postList.add(post)
                        } catch (e: Exception) {
                            Toast.makeText(this, "Error parsing post: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    titleAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this, "No titles found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load titles: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
