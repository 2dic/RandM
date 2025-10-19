package com.example.randm.ui.characters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.randm.data.repository.CharacterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CharactersViewModel(
    private val repository: CharacterRepository
) : ViewModel() {

    private val _characters = MutableStateFlow<Result<List<Character>>>(Result.Loading)
    val characters: StateFlow<Result<List<Character>>> = _characters.asStateFlow()

    fun loadCharacters(
        name: String? = null,
        status: String? = null,
        species: String? = null,
        gender: String? = null
    ) {
        viewModelScope.launch {
            repository.getCharacters(name, status, species, gender).collect { result ->
                _characters.value = result
            }
        }
    }

    fun refreshCharacters(
        name: String? = null,
        status: String? = null,
        species: String? = null,
        gender: String? = null
    ) {
        viewModelScope.launch {
            val refreshSuccess = repository.refreshCharacters()
            if (refreshSuccess) {
                loadCharacters(name, status, species, gender)
            } else {
                _characters.value = Result.Error("Failed to refresh data")
            }
        }
    }
}