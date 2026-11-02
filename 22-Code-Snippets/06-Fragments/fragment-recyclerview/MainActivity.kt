package com.example.recyclerviewandfragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.recyclerviewandfragment.databinding.ActivityMainBinding
import com.google.android.material.color.MaterialColors
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.TypedValue


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //set the home fragment
        if(savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentViewContaier, HomeFragment())
                .commit()
        }

        // set theme mode icon
        toggleIcon()
        // set theme mode
        binding.toolbarHome.setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.homeThemeMode -> {
                    toggleTheme()
                    true
                }
                else -> false
            }
        }
    }

    fun toggleTheme() {
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        val menuItem = binding.toolbarHome.menu.findItem(R.id.homeThemeMode)

        if(currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            // night -> light
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        else {
            // light -> night
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )
        }
    }

    fun toggleIcon() {
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        val menuItem = binding.toolbarHome.menu.findItem(R.id.homeThemeMode)

        if(currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            // night -> light
            menuItem.setIcon(R.drawable.ic_light_mode)
            menuItem.title = "Dark Mode"
        }
        else {
            // light -> night
            menuItem.setIcon(R.drawable.ic_dark_mode)
            menuItem.title = "Light Mode"
        }

        // change the menu item icon color
        val toolbar = binding.toolbarHome
        val typedValue = TypedValue()

        theme.resolveAttribute(
            android.R.attr.textColorPrimary,
            typedValue,
            true
        )
        val iconColor = typedValue.data

        // set toolbar navigation arrow color
        //toolbar.setNavigationIconTint(iconColor)

        // set color to menu icons
        for(i in 0 until toolbar.menu.size()) {
            val menuItem = toolbar.menu.getItem(i)

            //menuItem.iconTintList = ColorStateList.valueOf(iconColor)
        }
    }
}