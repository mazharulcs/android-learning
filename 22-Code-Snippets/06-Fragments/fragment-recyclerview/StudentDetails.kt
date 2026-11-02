package com.example.recyclerviewandfragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.recyclerviewandfragment.databinding.ActivityStudentDetailsBinding

class StudentDetails : AppCompatActivity() {
    private lateinit var binding: ActivityStudentDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // getting data from intent
        val bundle = intent.extras
        val name = bundle?.getString("name") ?: "Abir Choudhury"
        val id = bundle?.getString("id") ?: "10000000"
        val dept = bundle?.getString("dept") ?: "CSE"

        // set data
        binding.tvName.text = name
        binding.tvId.text = id
        binding.tvDept.text = dept

        // implement back navigation toolbar
        binding.toolbarStudent.setNavigationOnClickListener {
            finish()
        }
    }
}