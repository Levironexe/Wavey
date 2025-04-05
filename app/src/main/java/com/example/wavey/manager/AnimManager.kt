package com.example.wavey.manager

import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import android.view.animation.AnimationUtils
import com.example.wavey.R

fun View.slideFromRight(animTime: Long, startOffset: Long) {
    val slideFromRight = AnimationUtils.loadAnimation(context, R.anim.slide_from_right).apply {
        duration = animTime
        interpolator = FastOutSlowInInterpolator()
        this.startOffset = startOffset
    }
    startAnimation(slideFromRight)
}
fun View.slideFromLeft(animTime: Long, startOffset: Long) {
    val slideFromLeft = AnimationUtils.loadAnimation(context, R.anim.slide_from_left).apply {
        duration = animTime
        interpolator = FastOutSlowInInterpolator()
        this.startOffset = startOffset
    }
    startAnimation(slideFromLeft)
}
