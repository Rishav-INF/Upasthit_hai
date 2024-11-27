package com.example.upasthithai

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class splash_screen : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
        var img = findViewById<ImageView>(R.id.logo)


//        img.animate().translationY(-1600f).setDuration(1000).setStartDelay(4000)
        Handler(Looper.getMainLooper()).postDelayed({
            // Create a fade-in animation
            val fadeIn = AlphaAnimation(0f, 1f)
            fadeIn.duration = 1000 // 1 second

            // Apply the animation to the ImageView
            img.startAnimation(fadeIn)
            // Make the ImageView visible
            img.visibility = ImageView.VISIBLE
        }, 1000)
        Handler(Looper.getMainLooper()).postDelayed({

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

        }, 2000)

    }
}