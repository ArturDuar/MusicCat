package edu.udb.investigaciondsm2.model

data class Artista(
    val artistaId: Int = 0, // Primary Key in DB
    val name: String,
    val genre: String,
    val country: String,
    val description: String
)
