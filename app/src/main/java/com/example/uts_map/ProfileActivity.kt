package com.example.uts_map

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class ProfileActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etNIM: EditText
    private lateinit var profileImageView: ImageView
    private lateinit var btnUpdate: Button
    private lateinit var btnBackToHome: Button
    private lateinit var btnChangePicture: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var imageUri: Uri? = null

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        etName = findViewById(R.id.etName)
        etNIM = findViewById(R.id.etNIM)
        profileImageView = findViewById(R.id.profileImageView)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnBackToHome = findViewById(R.id.btnBackToHome)
        btnChangePicture = findViewById(R.id.btnChangePicture)

        // Memuat data profil jika ada
        loadProfileData()

        // Event listener untuk tombol "Back to Home"
        btnBackToHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        // Event listener untuk tombol "Update"
        btnUpdate.setOnClickListener {
            val name = etName.text.toString()
            val nim = etNIM.text.toString()

            if (name.isNotEmpty() && nim.isNotEmpty()) {
                updateProfile(name, nim)
            } else {
                Toast.makeText(this, "Please enter both Name and NIM", Toast.LENGTH_SHORT).show()
            }
        }

        // Event listener untuk tombol "Change Picture"
        btnChangePicture.setOnClickListener {
            // Pilih dari galeri
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }

    // Fungsi untuk menangani hasil pemilihan gambar dari galeri
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            profileImageView.setImageURI(imageUri)
            uploadProfilePicture()  // Setelah gambar dipilih, unggah ke Firebase Storage
        }
    }

    // Fungsi untuk mengunggah gambar ke Firebase Storage
    private fun uploadProfilePicture() {
        if (imageUri != null) {
            val storageReference = storage.getReference("profile_pics")
            val fileReference = storageReference.child(auth.currentUser?.uid + ".jpg")

            fileReference.putFile(imageUri!!)
                .addOnSuccessListener {
                    fileReference.downloadUrl.addOnSuccessListener { uri ->
                        val downloadUrl = uri.toString()
                        // Simpan URL gambar profil ke Firestore
                        saveProfileImageUrl(downloadUrl)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to upload profile picture: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Fungsi untuk menyimpan URL gambar profil ke Firestore
    private fun saveProfileImageUrl(downloadUrl: String) {
        val userId = auth.currentUser?.uid
        val userRef = db.collection("users").document(userId!!)

        userRef.update("profileImageUrl", downloadUrl)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update profile picture: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Fungsi untuk memuat data profil (Nama, NIM, dan Profile Picture)
    private fun loadProfileData() {
        val userId = auth.currentUser?.uid
        val userRef = db.collection("users").document(userId!!)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    etName.setText(document.getString("name"))
                    etNIM.setText(document.getString("nim"))
                    val profileImageUrl = document.getString("profileImageUrl")

                    // Tampilkan gambar profil jika ada
                    if (!profileImageUrl.isNullOrEmpty()) {
                        Picasso.get().load(profileImageUrl).into(profileImageView)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile data", Toast.LENGTH_SHORT).show()
            }
    }

    // Fungsi untuk mengupdate Nama dan NIM di Firestore
    private fun updateProfile(name: String, nim: String) {
        val userId = auth.currentUser?.uid
        val userRef = db.collection("users").document(userId!!)

        val updates = mapOf(
            "name" to name,
            "nim" to nim
        )

        userRef.set(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
