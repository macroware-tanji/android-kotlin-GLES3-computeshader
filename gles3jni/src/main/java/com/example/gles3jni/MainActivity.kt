package com.example.gles3jni

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.gles3jni.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        //binding.glsurfaceview.setRenderer(MyRenderer())

    }
}