package com.example.wavey.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.wavey.DialogHelper
import com.example.wavey.R
import com.example.wavey.SettingItem
import com.example.wavey.SettingsAdapter
import com.example.wavey.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userEmail = intent.getStringExtra("EMAIL_OR_USERNAME")?: "conmemay"
        val userProfilePictureUrlString = intent.getStringExtra("PROFILE_PICTURE")
        val userProfilePictureUrl: Uri? = if (userProfilePictureUrlString != null) Uri.parse(userProfilePictureUrlString) else null

        setupUI(userEmail, userProfilePictureUrl)
        setupListeners()
        setupRecyclerView()
    }

    private fun setupUI(userEmail: String, userProfilePictureUrl: Uri? = null) {
        if (userProfilePictureUrl != null) {
            // Load image from URL using Glide
            Glide.with(this)
                .load(userProfilePictureUrl)
                .placeholder(R.drawable.ic_profile_default)
                .error(R.drawable.ic_profile_default)
                .into(binding.profileImage)
        } else {
            // Set default profile image
            binding.profileImage.setImageResource(R.drawable.ic_profile_default)
        }
        binding.username.text = userEmail
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.editButton.setOnClickListener {
            // Handle edit profile action
        }


        // Handle logout action
        binding.logoutButton.setOnClickListener {
            DialogHelper.showLogoutConfirmationDialog(
                context = this,
                onLogoutConfirmed = {
                    FirebaseAuth.getInstance().signOut()
                    // Navigate back to login screen
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            )
        }
    }

    private fun setupRecyclerView() {
        val settingsList = listOf(
            SettingItem(R.drawable.ic_category_placeholder, "Privacy Policy"),
            SettingItem(R.drawable.ic_circle_help, "Help & Support"),
            SettingItem(R.drawable.ic_users_round, "About")
        )

        val adapter = SettingsAdapter(settingsList) { item ->
            // Handle item click based on title
            when(item.title) {
                "Privacy Policy" -> {
                    val url = "https://wavey-swinburne.netlify.app/privatepolicy"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }
                "Help & Support" -> {
                    val url = "https://wavey-swinburne.netlify.app/helpandsupport"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }
                "About" -> {
                    val url = "https://wavey-swinburne.netlify.app/about"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }

            }
        }

        binding.settingsList.layoutManager = LinearLayoutManager(this)
        binding.settingsList.adapter = adapter
    }
}