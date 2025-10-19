package com.example.randm.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "species") val species: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "gender") val gender: String,
    @ColumnInfo(name = "image") val image: String,
    @ColumnInfo(name = "origin_name") val originName: String,
    @ColumnInfo(name = "location_name") val locationName: String,
    @ColumnInfo(name = "created") val created: String,
    @ColumnInfo(name = "last_updated") val lastUpdated: Long = System.currentTimeMillis()
)