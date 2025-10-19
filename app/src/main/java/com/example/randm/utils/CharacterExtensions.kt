package com.example.rickmorty.utils

import com.example.randm.data.models.CharacterEntity

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

fun CharacterEntity.toCharacter(): Character {
    return Character(
        id = id,
        name = name,
        status = status,
        species = species,
        type = type,
        gender = gender,
        origin = Character.Location(originName, ""),
        location = Character.Location(locationName, ""),
        image = image,
        episode = emptyList(),
        url = "",
        created = created
    )
}