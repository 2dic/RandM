package com.example.randm.utils

import com.example.randm.data.models.Character
import com.example.randm.data.models.CharacterEntity

// Extension function для Character -> CharacterEntity
fun Character.toEntity(): CharacterEntity {
    return CharacterEntity(
        id = id,
        name = name,
        status = status,
        species = species,
        type = type,
        gender = gender,
        image = image,
        originName = origin.name,
        locationName = location.name,
        created = created
    )
}

// Extension function для CharacterEntity -> Character
fun CharacterEntity.toCharacter(): Character {
    return Character(
        id = id,
        name = name,
        status = status,
        species = species,
        type = type,
        gender = gender,
        origin = Character.Origin(originName, ""),
        location = Character.Location(locationName, ""),
        image = image,
        episode = emptyList(),
        url = "",
        created = created
    )
}