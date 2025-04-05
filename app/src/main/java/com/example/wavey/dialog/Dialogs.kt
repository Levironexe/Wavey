package com.example.wavey

import android.app.AlertDialog
import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.setPadding

object DialogHelper {
    private var currentDialog: AlertDialog? = null
    fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
    fun showLogoutConfirmationDialog(
        context: Context,
        onLogoutConfirmed: () -> Unit
    ) {
        currentDialog?.dismiss()

        // Create container layout
        val containerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 0, 48, 32)
        }

        // Add image to container
        val sadFaceImage = ImageView(context).apply {
            setImageResource(R.drawable.ic_box)
            layoutParams = LinearLayout.LayoutParams(
                120.dpToPx(context),
                120.dpToPx(context)
            ).apply {
                gravity = Gravity.CENTER
                setMargins(0, 56, 0, 56)
            }
        }
        containerLayout.addView(sadFaceImage)

        // Add message to container
        val messageView = TextView(context).apply {
            text = context.getString(R.string.log_out_message)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            setLineSpacing(0f, 1.25f)
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
            gravity = Gravity.CENTER
        }

        containerLayout.addView(messageView)

        currentDialog = AlertDialog.Builder(context)
            .setView(containerLayout) // Use containerLayout instead of messageView
            .setPositiveButton("Log Out") { dialog, _ ->
                dialog.dismiss()
                onLogoutConfirmed()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        currentDialog?.setOnShowListener { dialog ->
            val alertDialog = dialog as AlertDialog

            // Style positive button (Log Out)
            val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.post {
                positiveButton.transformationMethod = null
                positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
                positiveButton.setTextColor(ContextCompat.getColor(context, R.color.main_orange))
                positiveButton.setPadding(32, 14, 32, 14)

            }

            // Style negative button (Cancel)
            val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            negativeButton.post {
                negativeButton.transformationMethod = null
                negativeButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
                negativeButton.setTextColor(ContextCompat.getColor(context, R.color.white))
                negativeButton.setPadding(32, 14, 32, 14)
            }
        }



        currentDialog?.show()
        currentDialog?.window?.setBackgroundDrawableResource(R.color.darker_main_bg)

    }

    fun showDialog( // Not using in this app
        context: Context,
        message: String,
        title: String = "Alert",
        positiveButtonText: String = "Got it"
    ) {
        currentDialog?.dismiss()

        val titleView = TextView(context).apply {
            text = title
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(48, 32, 48, 16)
        }

        val messageView = TextView(context).apply {
            text = message
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            setLineSpacing(0f, 1.25f)
            setTextColor(0x99000000.toInt())
            setPadding(48, 0, 48, 32)
        }

        currentDialog = AlertDialog.Builder(context)
            .setCustomTitle(titleView)
            .setView(messageView)
            .setPositiveButton(positiveButtonText) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        currentDialog?.setOnShowListener { dialog ->
            val positiveButton = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.post {
                ViewCompat.setBackgroundTintList(positiveButton, null)

                positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
                positiveButton.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                positiveButton.setBackgroundResource(R.drawable.bg_add_buttons)

            }
        }

        currentDialog?.show()
        currentDialog?.window?.setBackgroundDrawableResource(R.drawable.bg_add_buttons)
    }

    fun deleteDialog(
        context: Context,
        message: String,
        onDeleteConfirm: () -> Unit
    ) {
        currentDialog?.dismiss()

        // Create container layout
        val containerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(10, 0, 10, 32)
        }

        // Add image to container
        val sadFaceImage = ImageView(context).apply {
            setImageResource(R.drawable.ic_trash)
            layoutParams = LinearLayout.LayoutParams(
                120.dpToPx(context),
                120.dpToPx(context)
            ).apply {
                gravity = Gravity.CENTER
                setMargins(0, 56, 0, 56)
            }
        }
        containerLayout.addView(sadFaceImage)

        // Add message to container
        val messageView = TextView(context).apply {
            text = message
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            setLineSpacing(0f, 1.25f)
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
            gravity = Gravity.CENTER
        }

        containerLayout.addView(messageView)

        currentDialog = AlertDialog.Builder(context)
            .setView(containerLayout) // Use containerLayout instead of messageView
            .setPositiveButton("Delete") { dialog, _ ->
                dialog.dismiss()
                onDeleteConfirm()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        currentDialog?.setOnShowListener { dialog ->
            val alertDialog = dialog as AlertDialog

            // Style positive button (Log Out)
            val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.post {
                positiveButton.transformationMethod = null
                positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
                positiveButton.setTextColor(ContextCompat.getColor(context, R.color.main_orange))
                positiveButton.setPadding(32, 14, 32, 14)
            }

            // Style negative button (Cancel)
            val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            negativeButton.post {
                negativeButton.transformationMethod = null
                negativeButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
                negativeButton.setTextColor(ContextCompat.getColor(context, R.color.white))
                negativeButton.setPadding(32, 14, 32, 14)
            }
        }



        currentDialog?.show()
        currentDialog?.window?.setBackgroundDrawableResource(R.color.lighter_main_bg)
    }
}