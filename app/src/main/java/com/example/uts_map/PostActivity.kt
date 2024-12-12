package com.example.uts_map

import ImageAdapter
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.File

class PostActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var rvImages: RecyclerView
    private lateinit var btnChooseImage: Button
    private lateinit var btnTakePhoto: Button
    private lateinit var btnPost: Button
    private lateinit var btnBackToHome: Button

    private val imageUris = mutableListOf<Uri>()
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var storage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore

    private val PICK_IMAGE_REQUEST = 1
    private val TAKE_PHOTO_REQUEST = 2
    private lateinit var photoUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        // Inisialisasi Firebase
        storage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Inisialisasi view
        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        rvImages = findViewById(R.id.rvImages)
        btnChooseImage = findViewById(R.id.btnChooseImage)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        btnPost = findViewById(R.id.btnPost)
        btnBackToHome = findViewById(R.id.btnBackToHome)

        // Setup RecyclerView
        imageAdapter = ImageAdapter(imageUris)
        rvImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvImages.adapter = imageAdapter

        // Tombol untuk memilih gambar
        btnChooseImage.setOnClickListener {
            val chooseIntent = Intent(Intent.ACTION_PICK)
            chooseIntent.type = "image/*"
            chooseIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(chooseIntent, PICK_IMAGE_REQUEST)
        }

        // Tombol untuk mengambil foto dari kamera
        btnTakePhoto.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                val photoFile = File.createTempFile("photo_", ".jpg", cacheDir)
                photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST)
            } else {
                Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
            }
        }

        // Tombol untuk memposting
        btnPost.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (description.isEmpty() && imageUris.isEmpty()) {
                Toast.makeText(this, "Please add a description or images", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            uploadImagesAndPost(title, description)
        }

        // Tombol kembali ke Home
        btnBackToHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data?.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val imageUri = data.clipData!!.getItemAt(i).uri
                    imageUris.add(imageUri)
                }
            } else if (data?.data != null) {
                imageUris.add(data.data!!)
            }
            imageAdapter.notifyDataSetChanged()
        } else if (requestCode == TAKE_PHOTO_REQUEST && resultCode == Activity.RESULT_OK) {
            imageUris.add(photoUri)
            imageAdapter.notifyDataSetChanged()
        }
    }

    private fun uploadImagesAndPost(title: String, description: String) {
        val uploadedImageUrls = mutableListOf<String>()

        if (imageUris.isNotEmpty()) {
            val storageRef = storage.reference
            val uploadTasks = imageUris.map { uri ->
                val imageRef = storageRef.child("posts/${System.currentTimeMillis()}.jpg")
                imageRef.putFile(uri).continueWithTask { task ->
                    if (!task.isSuccessful) throw task.exception!!
                    imageRef.downloadUrl
                }
            }

            uploadTasks.forEach { task ->
                task.addOnSuccessListener { uri ->
                    uploadedImageUrls.add(uri.toString())
                    if (uploadedImageUrls.size == imageUris.size) {
                        savePostToFirestore(title, description, uploadedImageUrls)
                    }
                }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to upload images: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            savePostToFirestore(title, description, null)
        }
    }

    private fun savePostToFirestore(title: String, description: String, imageUrls: List<String>?) {
        val post = hashMapOf(
            "title" to title,
            "description" to description,
            "imageUrls" to imageUrls,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("posts")
            .add(post)
            .addOnSuccessListener {
                Toast.makeText(this, "Post uploaded successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload post: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
