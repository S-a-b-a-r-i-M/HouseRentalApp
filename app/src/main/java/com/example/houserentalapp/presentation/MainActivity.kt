package com.example.houserentalapp.presentation

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.ActivityMainBinding
import com.example.houserentalapp.presentation.ui.StarterActivity
import com.example.houserentalapp.presentation.utils.extensions.logInfo

// TODO: HANDLE AUTH PORTION
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setWindowInsets()

        startActivity(Intent(this, StarterActivity::class.java))
        finish()
    }

    fun setWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }
}