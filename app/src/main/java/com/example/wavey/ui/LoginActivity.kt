package com.example.wavey.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.wavey.R
import com.example.wavey.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val TAG = "GoogleSignIn"

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        } else {

            Toast.makeText(this, "Google sign-in canceled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithGoogle() {
        binding.btnGoogleLogin.text = ""
        binding.progressBar2.visibility = View.VISIBLE

        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.floating)

        setContentView(binding.root)
        Log.d(TAG, "LoginActivity onCreate")

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            view.updatePadding(bottom = bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()

        binding.ivLogo.startAnimation(animation)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("606526375453-4t3ado7210ppq1mj93fkjl2vobck9nfr.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnLogin.setOnClickListener{
            binding.progressBar1.visibility = View.VISIBLE
            binding.btnLogin.text = ""

            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener{
                    if (it.isSuccessful) {

                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("EMAIL_OR_USERNAME", email)
                        startActivity(intent)
                    } else {
                        binding.progressBar1.visibility = View.GONE
                        binding.btnLogin.text = getString(R.string.login)
                        Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (email.isEmpty()) {
                binding.progressBar1.visibility = View.GONE
                binding.btnLogin.text = getString(R.string.login)
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            } else {
                binding.progressBar1.visibility = View.GONE
                binding.btnLogin.text = getString(R.string.login)
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
            }

        }

        binding.btnGoogleLogin.setOnClickListener{
            signInWithGoogle()
        }

        //Sign up if don't have account
        binding.tvSignUp.setOnClickListener{
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun handleSignInResult(completedTask: com.google.android.gms.tasks.Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            // Signed in successfully
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            // Sign in failed
            binding.btnGoogleLogin.text = getString(R.string.continue_with_google)
            binding.progressBar2.visibility = View.GONE

            Toast.makeText(this, "Google sign in failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        Log.d(TAG, "Starting Firebase auth with Google")
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    Log.d(TAG, "Firebase auth successful")
                    val user = firebaseAuth.currentUser
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("EMAIL_OR_USERNAME", user?.displayName)
                    // Pass profile picture URL if available
                    user?.photoUrl?.let {
                        intent.putExtra("PROFILE_PICTURE", it.toString())
                    }
                    startActivity(intent)
                    finish()
                } else {
                    binding.btnGoogleLogin.text = getString(R.string.continue_with_google)
                    binding.progressBar2.visibility = View.GONE

                    // If sign in fails, display a message to the user.
                    Log.e(TAG, "Firebase auth failed", task.exception)
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null) {
            Log.d(TAG, "User already signed in, redirecting to MainActivity")
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("email", firebaseAuth.currentUser?.email)
            firebaseAuth.currentUser?.photoUrl?.let {
                intent.putExtra("PROFILE_PICTURE", it.toString())
            }
            startActivity(intent)
            finish()
        }
    }
}