package com.example.houserentalapp.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.houserentalapp.R
import com.example.houserentalapp.data.local.db.DatabaseHelper
import com.example.houserentalapp.databinding.ActivityMainBinding
import com.example.houserentalapp.presentation.utils.extensions.logInfo

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setWindowInsets()


    }

    @SuppressLint("ResourceAsColor")
    fun setWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
//            v.setBackgroundColor(R.color.black)
            insets
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        logInfo("item clicked--------> $item")
        return super.onOptionsItemSelected(item)
    }
}