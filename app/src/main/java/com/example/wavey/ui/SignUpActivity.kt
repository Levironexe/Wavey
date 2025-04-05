package com.example.wavey.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.wavey.databinding.ActivitySignUpBinding
import android.view.animation.AnimationUtils
import com.example.wavey.R
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)

        val floatingAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.floating)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            view.updatePadding(bottom = bottom)
            insets
        }

        //Client creation for Firebase
        firebaseAuth = FirebaseAuth.getInstance()

        //Apply animation for ImageView Icon - HandPhone
        binding.ivLogo.startAnimation(floatingAnimation)

        binding.btnSignUp.setOnClickListener{
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etRepeatPassword.text.toString()

            binding.progressBar1.visibility = View.VISIBLE
            binding.btnSignUp.text = ""


            println("Password $password")
            println("Password confirm $confirmPassword")

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword && password.length > 6) {
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {

                        if (it.isSuccessful) {
                            binding.progressBar1.visibility = View.GONE
                            binding.btnSignUp.text = getString(R.string.signup)
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            intent.putExtra("EMAIL_OR_USERNAME", email)
                            startActivity(intent)
                        } else {
                            binding.progressBar1.visibility = View.GONE
                            binding.btnSignUp.text = getString(R.string.signup)
                            Toast.makeText(this, it.exception?.message, Toast.LENGTH_LONG).show()
                        }
                    }
                } else if (password != confirmPassword) {
                    binding.progressBar1.visibility = View.GONE
                    binding.btnSignUp.text = getString(R.string.signup)
                    Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
                } else if (password.length <= 6) {
                    binding.progressBar1.visibility = View.GONE
                    binding.btnSignUp.text = getString(R.string.signup)
                    Toast.makeText(this, "Password must have more than 6 character", Toast.LENGTH_SHORT).show()
                } else {
                    binding.progressBar1.visibility = View.GONE
                    binding.btnSignUp.text = getString(R.string.signup)
                    Toast.makeText(this, "Unknown error", Toast.LENGTH_LONG).show()
                }
            } else {
                binding.progressBar1.visibility = View.GONE
                binding.btnSignUp.text = getString(R.string.signup)
                Toast.makeText(this, "Empty field is not allowed", Toast.LENGTH_SHORT).show()
            }
        }
        binding.tvLogInInstead.setOnClickListener{
            finish()
        }
    }
}
