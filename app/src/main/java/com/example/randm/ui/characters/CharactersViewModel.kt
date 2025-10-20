package com.example.randm.ui.characters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import com.example.randm.data.models.Character as ModelCharacter
import com.example.randm.data.models.Result
import com.example.randm.data.repository.CharacterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CharactersViewModel(
    private val repository: CharacterRepository
) : ViewModel() {

    private val _characters = MutableStateFlow<Result<List<ModelCharacter>>>(Result.Loading)
    val characters = _characters
    val charactersLiveData = characters.asLiveData()

    private var currentPage = 1
    private var hasNextPage = true

    fun loadCharacters(
        name: String? = null,
        status: String? = null,
        species: String? = null,
        gender: String? = null
    ) {
        viewModelScope.launch {
            _characters.value = Result.Loading
            currentPage = 1
            hasNextPage = true

            try {
                repository.getCharacters(name, status, species, gender).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _characters.value = Result.Success(result.data)
                        }
                        is Result.Error -> {
                            _characters.value = Result.Error(result.message)
                        }
                        is Result.Loading -> {
                            _characters.value = Result.Loading
                        }
                    }
                }
            } catch (e: Exception) {
                _characters.value = Result.Error("Load error: ${e.message}")
            }
        }
    }

    fun loadNextPage() {
        if (!hasNextPage) return

        viewModelScope.launch {
            try {
                val currentResult = _characters.value
                if (currentResult is Result.Success) {
                    val nextPageResult = repository.getCharactersPage(currentPage + 1)
                    when (nextPageResult) {
                        is Result.Success -> {
                            if (nextPageResult.data.isNotEmpty()) {
                                currentPage++
                                val combinedList = currentResult.data + nextPageResult.data
                                _characters.value = Result.Success(combinedList)
                                hasNextPage = true
                            } else {
                                hasNextPage = false
                            }
                        }
                        is Result.Error -> {
                            // Сохраняем текущие данные, но показываем ошибку пагинации
                            _characters.value = Result.Success(currentResult.data)
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                // Игнорируем ошибки пагинации, сохраняя текущие данные
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
            try {
                val success = repository.refreshCharacters()
                if (success) {
                    loadCharacters(name, status, species, gender)
                } else {
                    _characters.value = Result.Error("Refresh failed")
                }
            } catch (e: Exception) {
                _characters.value = Result.Error("Refresh error: ${e.message}")
            }
        }
    }
}