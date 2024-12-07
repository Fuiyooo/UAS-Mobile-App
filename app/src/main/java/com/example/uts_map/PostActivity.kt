package com.example.uts_map

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PostActivity : AppCompatActivity() {

    private lateinit var etDescription: EditText
    private lateinit var ivPostImage: ImageView
    private lateinit var btnChooseImage: Button
    private lateinit var btnTakePhoto: Button
    private lateinit var btnPost: Button
    private lateinit var btnBackToHome: Button
    private var imageUri: Uri? = null
    private lateinit var storage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore

    private val PICK_IMAGE_REQUEST = 1
    private val TAKE_PHOTO_REQUEST = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        // Inisialisasi Firebase
        storage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Inisialisasi view
        etDescription = findViewById(R.id.etDescription)
        ivPostImage = findViewById(R.id.ivPostImage)
        btnChooseImage = findViewById(R.id.btnChooseImage)
        btnTakePhoto = findViewById(R.id.btnTakePhoto) // Button baru untuk kamera
        btnPost = findViewById(R.id.btnPost)
        btnBackToHome = findViewById(R.id.btnBackToHome)

        // Tombol untuk memilih gambar dari galeri
        btnChooseImage.setOnClickListener {
            val chooseIntent = Intent(Intent.ACTION_PICK)
            chooseIntent.type = "image/*"
            startActivityForResult(chooseIntent, PICK_IMAGE_REQUEST)
        }

        // Tombol untuk mengambil gambar dari kamera
        btnTakePhoto.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                // Buat file untuk menyimpan foto
                try {
                    val photoFile = createImageFile()
                    photoFile?.also {
                        imageUri = FileProvider.getUriForFile(
                            this,
                            "${applicationContext.packageName}.provider",
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                        startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST)
                    }
                } catch (ex: IOException) {
                    Toast.makeText(this, "Error creating file: ${ex.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Tombol untuk memposting data ke Firebase
        btnPost.setOnClickListener {
            val description = etDescription.text.toString()

            // Pengecekan apakah ada yang diisi (teks atau gambar)
            if (description.isNotEmpty() || imageUri != null) {
                // Jika ada gambar, upload gambar terlebih dahulu
                if (imageUri != null) {
                    uploadImageAndPost(description, imageUri!!)
                } else {
                    // Jika hanya teks yang ada, simpan postingan tanpa gambar
                    savePostToFirestore(description, null)
                }
            } else {
                Toast.makeText(this, "Please add a description or an image", Toast.LENGTH_SHORT).show()
            }
        }

        // Tombol kembali ke halaman Home
        btnBackToHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    // Fungsi untuk membuat file gambar sementara untuk kamera
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    // Fungsi untuk mengunggah gambar ke Firebase Storage dan menyimpan ke Firestore
    private fun uploadImageAndPost(description: String, imageUri: Uri) {
        val storageReference = storage.reference.child("posts/${System.currentTimeMillis()}.jpg")

        storageReference.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    savePostToFirestore(description, uri.toString()) // Menyimpan teks dan URL gambar
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload image: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Fungsi untuk menyimpan deskripsi dan URL gambar (jika ada) ke Firestore
    private fun savePostToFirestore(description: String, imageUrl: String?) {
        val post = hashMapOf(
            "description" to description,
            "timestamp" to System.currentTimeMillis()
        )

        // Tambahkan URL gambar jika ada
        if (imageUrl != null) {
            post["imageUrl"] = imageUrl
        }

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

    // Mendapatkan gambar yang dipilih dari galeri atau kamera
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            ivPostImage.setImageURI(imageUri)
        } else if (requestCode == TAKE_PHOTO_REQUEST && resultCode == Activity.RESULT_OK) {
            ivPostImage.setImageURI(imageUri)
        }
    }
}
