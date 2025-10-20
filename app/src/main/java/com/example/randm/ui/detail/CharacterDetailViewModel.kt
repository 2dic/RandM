package com.example.randm.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import com.example.randm.data.models.Character as ModelCharacter
import com.example.randm.data.models.Result
import com.example.randm.data.repository.CharacterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CharacterDetailViewModel(
    private val repository: CharacterRepository,
    private val characterId: Int
) : ViewModel() {

    private val _character = MutableStateFlow<Result<ModelCharacter>>(Result.Loading)
    val character: StateFlow<Result<ModelCharacter>> = _character.asStateFlow()

    val characterLiveData = character.asLiveData()

    init {
        loadCharacter()
    }

    fun loadCharacter() {
        viewModelScope.launch {
            try {
                _character.value = repository.getCharacterById(characterId)
            } catch (e: Exception) {
                _character.value = Result.Error("Failed to load character: ${e.message}")
            }
        }
    }
}