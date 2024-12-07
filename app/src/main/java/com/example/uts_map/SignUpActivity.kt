package com.example.uts_map

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var etSignUpEmail: EditText
    private lateinit var etSignUpPassword: EditText
    private lateinit var btnSignUp: Button
    private lateinit var btnBackToLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Inisialisasi Firebase Authentication dan Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etSignUpEmail = findViewById(R.id.etSignUpEmail)
        etSignUpPassword = findViewById(R.id.etSignUpPassword)
        btnSignUp = findViewById(R.id.btnSignUp)
        btnBackToLogin = findViewById(R.id.btnBackToLogin)

        // Fungsi ketika tombol Sign Up ditekan
        btnSignUp.setOnClickListener {
            val email = etSignUpEmail.text.toString()
            val password = etSignUpPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signUpUser(email, password)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        // Fungsi ketika tombol Back to Login ditekan
        btnBackToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // Fungsi untuk mendaftarkan user ke Firebase Authentication
    private fun signUpUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Jika pendaftaran berhasil, simpan data ke Firestore
                    saveUserDataToFirestore(email)
                    Toast.makeText(this, "Sign Up successful", Toast.LENGTH_SHORT).show()
                    // Arahkan kembali ke halaman login setelah sukses
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    // Jika pendaftaran gagal
                    Toast.makeText(this, "Sign Up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Fungsi untuk menyimpan data user ke Firestore
    private fun saveUserDataToFirestore(email: String) {
        val user = hashMapOf(
            "email" to email
        )

        // Menyimpan data user berdasarkan UID dari Firebase Authentication
        db.collection("users")
            .document(auth.currentUser?.uid.toString())
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "User data saved to Firestore", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
