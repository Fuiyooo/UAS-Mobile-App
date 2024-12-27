package com.example.uts_map

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class EditPostActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var rvImages: RecyclerView
    private lateinit var btnChooseImage: Button
    private lateinit var btnTakePhoto: Button
    private lateinit var btnSaveChanges: Button
    private lateinit var btnCancelEdit: Button // Inisialisasi tombol Cancel Edit

    private val imageUris = mutableListOf<Uri>()
    private lateinit var imageAdapter: ImageAdapter

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private val PICK_IMAGE_REQUEST = 1
    private val TAKE_PHOTO_REQUEST = 2
    private lateinit var photoUri: Uri

    private var postId: String? = null
    private val uploadedImageUrls = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_post)

        // Inisialisasi Firebase
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Inisialisasi views
        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        rvImages = findViewById(R.id.rvImages)
        btnChooseImage = findViewById(R.id.btnChooseImage)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)
        btnCancelEdit = findViewById(R.id.btnCancelEdit) // Menghubungkan tombol Cancel Edit

        // Setup RecyclerView
        imageAdapter = ImageAdapter(imageUris) // Adapter hanya untuk menampilkan gambar
        rvImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvImages.adapter = imageAdapter

        // Ambil data dari intent
        postId = intent.getStringExtra("postId")
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val images = intent.getStringArrayListExtra("imageUrls") ?: arrayListOf()

        // Set data awal
        etTitle.setText(title)
        etDescription.setText(description)
        imageUris.addAll(images.map { Uri.parse(it) }) // Konversi URL string ke URI
        imageAdapter.notifyDataSetChanged()

        // Tombol Pilih Gambar
        btnChooseImage.setOnClickListener {
            val chooseIntent = Intent(Intent.ACTION_PICK)
            chooseIntent.type = "image/*"
            chooseIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(chooseIntent, PICK_IMAGE_REQUEST)
        }

        // Tombol Ambil Foto
        btnTakePhoto.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoFile = createImageFile()
            if (photoFile != null) {
                photoUri = Uri.fromFile(photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST)
            }
        }

        // Tombol Simpan Perubahan
        btnSaveChanges.setOnClickListener {
            saveChanges()
        }

        // Tombol Cancel Edit
        btnCancelEdit.setOnClickListener {
            cancelEdit() // Memanggil fungsi cancelEdit() saat tombol diklik
        }
    }

    // Menyimpan perubahan
    private fun saveChanges() {
        val updatedTitle = etTitle.text.toString().trim()
        val updatedDescription = etDescription.text.toString().trim()

        if (updatedTitle.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (updatedDescription.isEmpty()) {
            Toast.makeText(this, "Description cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUris.isEmpty()) {
            updatePostInFirestore(updatedTitle, updatedDescription, uploadedImageUrls)
            return
        }

        // Upload images
        uploadImages(updatedTitle, updatedDescription)
    }

    private fun uploadImages(updatedTitle: String, updatedDescription: String) {
        val storageRef = storage.reference
        val uploadTasks = mutableListOf<Task<Uri>>() // Menyimpan semua task upload

        // Loop untuk setiap URI gambar
        imageUris.forEach { uri ->
            val imageRef = storageRef.child("posts/${System.currentTimeMillis()}.jpg")

            // Tambahkan setiap task ke daftar
            val uploadTask = imageRef.putFile(uri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    // Mengambil URL gambar setelah upload berhasil
                    imageRef.downloadUrl
                }
            uploadTasks.add(uploadTask)
        }

        // Menunggu semua upload selesai
        Tasks.whenAllComplete(uploadTasks)
            .addOnSuccessListener { tasks ->
                val uploadedUrls = tasks.mapNotNull { task ->
                    if (task.isSuccessful) {
                        (task.result as? Uri)?.toString()
                    } else {
                        null // Jika gagal, abaikan URL ini
                    }
                }

                // Pastikan semua URL sudah terunggah
                if (uploadedUrls.size == uploadTasks.size) {
                    updatePostInFirestore(updatedTitle, updatedDescription, uploadedUrls)
                } else {
                    Toast.makeText(this, "Some images failed to upload.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to upload images: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Membatalkan perubahan dan kembali ke activity sebelumnya
    private fun cancelEdit() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    // Mengupdate data di Firestore
    private fun updatePostInFirestore(
        title: String,
        description: String,
        imageUrls: List<String>
    ) {
        val updatedPost = mapOf(
            "title" to title,
            "description" to description,
            "imageUrls" to imageUrls
        )

        postId?.let {
            firestore.collection("posts").document(it)
                .update(updatedPost)
                .addOnSuccessListener {
                    Toast.makeText(this, "Post updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update post: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun createImageFile(): File? {
        return try {
            File.createTempFile("photo_", ".jpg", cacheDir)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
