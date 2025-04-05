//package com.example.wavey
//
//import android.content.Context
//import android.content.Intent
//import android.util.Log
//import androidx.appcompat.app.AppCompatDelegate
//
//object ThemeManager {
//    private const val PREF_NAME = "theme_pref"
//    private const val KEY_IS_DARK_MODE = "is_dark_mode"
//
//    fun setDarkMode(context: Context, isDarkMode: Boolean) {
//        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
//        sharedPrefs.edit().putBoolean(KEY_IS_DARK_MODE, isDarkMode).apply()
//
//        // Update night mode
//        AppCompatDelegate.setDefaultNightMode(
//            if (isDarkMode)
//                AppCompatDelegate.MODE_NIGHT_YES
//            else
//                AppCompatDelegate.MODE_NIGHT_NO
//        )
//    }
//
//    fun isDarkMode(context: Context): Boolean {
//        val sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
//        return sharedPrefs.getBoolean(KEY_IS_DARK_MODE, true)
//    }
//}