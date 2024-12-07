package com.example.uts_map

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var rvPosts: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var firestore: FirebaseFirestore
    private var postList = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Inisialisasi Firestore
        firestore = FirebaseFirestore.getInstance()

        // Inisialisasi RecyclerView
        rvPosts = findViewById(R.id.rvPosts)
        rvPosts.layoutManager = LinearLayoutManager(this)
        postAdapter = PostAdapter(postList)
        rvPosts.adapter = postAdapter


        // Ambil data postingan dari Firestore
        loadPostsFromFirestore()

        // Inisialisasi Bottom Navigation
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Event listener untuk navigasi
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Home dipilih, tetap di halaman ini
                    true
                }
                R.id.nav_post -> {
                    // Buka PostActivity
                    startActivity(Intent(this, PostActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    // Buka ProfileActivity
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Set home sebagai default item yang dipilih
        bottomNavigation.selectedItemId = R.id.nav_home
    }

    // Fungsi untuk mengambil data postingan dari Firestore
    private fun loadPostsFromFirestore() {
        firestore.collection("posts")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    postList.clear() // Kosongkan list sebelum menambah data baru
                    for (document in documents) {
                        val post = document.toObject(Post::class.java)
                        postList.add(post)
                        // Tambahkan log untuk memastikan data diambil
                        println("Post: ${post.description}, Image URL: ${post.imageUrl}")
                    }
                    postAdapter.notifyDataSetChanged() // Refresh RecyclerView
                } else {
                    Toast.makeText(this, "No posts found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load posts: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


}
