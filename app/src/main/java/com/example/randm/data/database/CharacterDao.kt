package com.example.randm.data.database

import androidx.room.*
import com.example.randm.data.models.CharacterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {

    @Query("SELECT * FROM characters")
    fun getAllCharacters(): Flow<List<CharacterEntity>>

    @Query("SELECT * FROM characters WHERE id = :id")
    suspend fun getCharacterById(id: Int): CharacterEntity?

    @Query("SELECT * FROM characters WHERE name LIKE :query")
    suspend fun searchCharacters(query: String): List<CharacterEntity>

    @Query("""
        SELECT * FROM characters 
        WHERE (:name IS NULL OR name LIKE '%' || :name || '%')
        AND (:status IS NULL OR status = :status)
        AND (:species IS NULL OR species = :species)
        AND (:gender IS NULL OR gender = :gender)
    """)
    suspend fun getCharactersWithFilters(
        name: String?,
        status: String?,
        species: String?,
        gender: String?
    ): List<CharacterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(character: CharacterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(characters: List<CharacterEntity>)

    @Query("DELETE FROM characters")
    suspend fun clearAll()
}