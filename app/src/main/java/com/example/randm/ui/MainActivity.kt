package com.example.randm.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.randm.R
import com.example.randm.databinding.ActivityMainBinding
import com.example.randm.ui.characters.CharactersFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        // Если это первый запуск, добавляем CharactersFragment
        if (savedInstanceState == null) {
            showCharactersFragment()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Rick and Morty Characters"
    }

    private fun showCharactersFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, CharactersFragment())
            .commit()
    }

    // Обработка нажатия кнопки "Назад"
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}