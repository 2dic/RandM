package com.example.randm.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.randm.data.api.RickMortyApiService
import com.example.randm.data.database.CharacterDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CharacterRepository(
    private val apiService: RickMortyApiService,
    private val characterDao: CharacterDao,
    private val context: Context
) {

    private val connectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun getCharacters(
        name: String? = null,
        status: String? = null,
        species: String? = null,
        gender: String? = null
    ): Flow<Result<List<Character>>> = flow {
        emit(Result.Loading)

        try {
            if (isNetworkAvailable()) {
                val response = apiService.getCharacters(
                    name = name,
                    status = status,
                    species = species,
                    gender = gender
                )

                if (name == null && status == null && species == null && gender == null) {
                    characterDao.insertAll(response.results.map { it.toEntity() })
                }

                emit(Result.Success(response.results))
            } else {
                val cachedCharacters = characterDao.getCharactersWithFilters(name, status, species, gender)
                emit(Result.Success(cachedCharacters.map { it.toCharacter() }))
            }
        } catch (e: Exception) {
            val cachedCharacters = characterDao.getCharactersWithFilters(name, status, species, gender)
            if (cachedCharacters.isNotEmpty()) {
                emit(Result.Success(cachedCharacters.map { it.toCharacter() }))
            } else {
                emit(Result.Error(e.message ?: "Unknown error"))
            }
        }
    }

    suspend fun getCharacterById(id: Int): Result<Character> {
        return try {
            val cached = characterDao.getCharacterById(id)
            if (cached != null) {
                Result.Success(cached.toCharacter())
            } else if (isNetworkAvailable()) {
                val character = apiService.getCharacterById(id)
                characterDao.insert(character.toEntity())
                Result.Success(character)
            } else {
                Result.Error("Character not found in cache")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to load character")
        }
    }

    suspend fun refreshCharacters(): Boolean {
        return try {
            if (!isNetworkAvailable()) return false

            var page = 1
            var hasNextPage = true
            val allCharacters = mutableListOf<Character>()

            while (hasNextPage) {
                val response = apiService.getCharacters(page = page)
                allCharacters.addAll(response.results)
                hasNextPage = response.info.next != null
                page++
            }

            characterDao.clearAll()
            characterDao.insertAll(allCharacters.map { it.toEntity() })
            true
        } catch (e: Exception) {
            false
        }
    }
}