package com.example.randm.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.randm.data.api.ApiClient
import com.example.randm.data.database.CharacterDatabase
import com.example.randm.data.repository.CharacterRepository
import com.example.randm.ui.characters.CharactersViewModel
import com.example.randm.ui.detail.CharacterDetailViewModel

class CharactersViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CharactersViewModel::class.java)) {
            val database = CharacterDatabase.getDatabase(application)
            val repository = CharacterRepository(
                ApiClient.characterService,
                database.characterDao(),
                application.applicationContext
            )
            return CharactersViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class CharacterDetailViewModelFactory(
    private val application: Application,
    private val characterId: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CharacterDetailViewModel::class.java)) {
            val database = CharacterDatabase.getDatabase(application)
            val repository = CharacterRepository(
                ApiClient.characterService,
                database.characterDao(),
                application.applicationContext
            )
            return CharacterDetailViewModel(repository, characterId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}