package com.example.randm.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.randm.data.api.ApiClient
import com.example.randm.data.api.RickMortyApiService
import com.example.randm.data.database.CharacterDao
import com.example.randm.data.models.Character as ModelCharacter
import com.example.randm.data.models.CharacterEntity
import com.example.randm.data.models.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first

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
    ): Flow<Result<List<ModelCharacter>>> = flow {
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
                    val entities = response.results.map { character ->
                        CharacterEntity(
                            id = character.id,
                            name = character.name,
                            status = character.status,
                            species = character.species,
                            type = character.type,
                            gender = character.gender,
                            image = character.image,
                            originName = character.origin.name,
                            locationName = character.location.name,
                            created = character.created
                        )
                    }
                    characterDao.insertAll(entities)
                }

                emit(Result.Success(response.results))
            } else {
                val cachedEntities = characterDao.getCharactersWithFilters(name, status, species, gender)
                val characters = cachedEntities.map { entity ->
                    ModelCharacter(
                        id = entity.id,
                        name = entity.name,
                        status = entity.status,
                        species = entity.species,
                        type = entity.type,
                        gender = entity.gender,
                        origin = ModelCharacter.Origin(entity.originName, ""),
                        location = ModelCharacter.Location(entity.locationName, ""),
                        image = entity.image,
                        episode = emptyList(),
                        url = "",
                        created = entity.created
                    )
                }
                emit(Result.Success(characters))
            }
        } catch (e: Exception) {
            val cachedEntities = characterDao.getCharactersWithFilters(name, status, species, gender)
            if (cachedEntities.isNotEmpty()) {
                val characters = cachedEntities.map { entity ->
                    ModelCharacter(
                        id = entity.id,
                        name = entity.name,
                        status = entity.status,
                        species = entity.species,
                        type = entity.type,
                        gender = entity.gender,
                        origin = ModelCharacter.Origin(entity.originName, ""),
                        location = ModelCharacter.Location(entity.locationName, ""),
                        image = entity.image,
                        episode = emptyList(),
                        url = "",
                        created = entity.created
                    )
                }
                emit(Result.Success(characters))
            } else {
                emit(Result.Error(e.message ?: "Unknown error"))
            }
        }
    }

    // Добавьте этот метод в CharacterRepository
    suspend fun getCharactersPage(page: Int): Result<List<ModelCharacter>> {
        return try {
            if (isNetworkAvailable()) {
                val response = apiService.getCharacters(page = page)
                // Кешируем данные
                characterDao.insertAll(response.results.map { character ->
                    CharacterEntity(
                        id = character.id,
                        name = character.name,
                        status = character.status,
                        species = character.species,
                        type = character.type,
                        gender = character.gender,
                        image = character.image,
                        originName = character.origin.name,
                        locationName = character.location.name,
                        created = character.created
                    )
                })
                Result.Success(response.results)
            } else {
                // В оффлайн режиме используем существующий метод с фильтрами
                try {
                    val cachedCharacters = characterDao.getAllCharacters().first()
                    val startIndex = (page - 1) * 20
                    val endIndex = minOf(startIndex + 20, cachedCharacters.size)

                    if (startIndex < cachedCharacters.size) {
                        val pagedEntities = cachedCharacters.subList(startIndex, endIndex)
                        val characters = pagedEntities.map { it.toCharacter() }
                        Result.Success(characters)
                    } else {
                        Result.Success(emptyList())
                    }
                } catch (e: Exception) {
                    Result.Success(emptyList())
                }
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to load page")
        }
    }

    suspend fun getCharacterById(id: Int): Result<ModelCharacter> {
        return try {
            characterDao.getCharacterById(id)?.let { cached ->
                val character = ModelCharacter(
                    id = cached.id,
                    name = cached.name,
                    status = cached.status,
                    species = cached.species,
                    type = cached.type,
                    gender = cached.gender,
                    origin = ModelCharacter.Origin(cached.originName, ""),
                    location = ModelCharacter.Location(cached.locationName, ""),
                    image = cached.image,
                    episode = emptyList(),
                    url = "",
                    created = cached.created
                )
                return Result.Success(character)
            }

            if (isNetworkAvailable()) {
                val character = apiService.getCharacterById(id)
                val entity = CharacterEntity(
                    id = character.id,
                    name = character.name,
                    status = character.status,
                    species = character.species,
                    type = character.type,
                    gender = character.gender,
                    image = character.image,
                    originName = character.origin.name,
                    locationName = character.location.name,
                    created = character.created
                )
                characterDao.insert(entity)
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
            val allCharacters = mutableListOf<ModelCharacter>()

            while (hasNextPage && page <= 5) {
                val response = apiService.getCharacters(page = page)
                allCharacters.addAll(response.results)
                hasNextPage = response.info.next != null
                page++
            }

            val entities = allCharacters.map { character ->
                CharacterEntity(
                    id = character.id,
                    name = character.name,
                    status = character.status,
                    species = character.species,
                    type = character.type,
                    gender = character.gender,
                    image = character.image,
                    originName = character.origin.name,
                    locationName = character.location.name,
                    created = character.created
                )
            }

            characterDao.clearAll()
            characterDao.insertAll(entities)
            true
        } catch (e: Exception) {
            false
        }
    }
}