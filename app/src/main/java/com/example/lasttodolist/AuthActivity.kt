package com.example.lasttodolist

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.text
import com.example.lasttodolist.databinding.ActivityAuthBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class AuthActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Check if the user is already logged in.
        if (auth.currentUser != null) {
            // User is logged in, go to MainActivity.
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.signInButton.setOnClickListener {
            signIn()
        }

        binding.signUpButton.setOnClickListener {
            signUp()
        }
    }

    private fun signIn() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        if (!validateInput(email, password)) return

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    handleSignInError(task.exception)
                }
            }
    }

    private fun signUp() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        if (!validateInput(email, password)) return

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Sign Up Successful!", Toast.LENGTH_SHORT).show()
                } else {
                    handleSignUpError(task.exception)
                }
            }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (TextUtils.isEmpty(email)) {
            binding.emailEditText.error = "Email is required"
            return false
        }

        if (TextUtils.isEmpty(password)) {
            binding.passwordEditText.error = "Password is required"
            return false
        }

        if (password.length < 6) {
            binding.passwordEditText.error = "Password must be at least 6 characters"
            return false
        }

        return true
    }

    private fun handleSignInError(exception: Exception?) {
        when (exception) {
            is FirebaseAuthInvalidCredentialsException -> {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Sign In Failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleSignUpError(exception: Exception?) {
        when (exception) {
            is FirebaseAuthWeakPasswordException -> {
                Toast.makeText(this, "Password is too weak", Toast.LENGTH_SHORT).show()
            }
            is FirebaseAuthInvalidCredentialsException -> {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            }
            is FirebaseAuthUserCollisionException -> {
                Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Sign Up Failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}