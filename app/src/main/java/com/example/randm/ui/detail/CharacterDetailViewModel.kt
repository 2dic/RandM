package com.example.randm.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.randm.data.repository.CharacterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CharacterDetailViewModel(
    private val repository: CharacterRepository,
    private val characterId: Int
) : ViewModel() {

    private val _character = MutableStateFlow<Result<Character>>(Result.Loading)
    val character: StateFlow<Result<Character>> = _character.asStateFlow()

    init {
        loadCharacter()
    }

    fun loadCharacter() {
        viewModelScope.launch {
            _character.value = repository.getCharacterById(characterId)
        }
    }
}