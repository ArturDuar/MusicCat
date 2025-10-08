// File: model/Artista.kt
package edu.udb.investigaciondsm2.model

data class Artista(
    val artistId: Int? = null, // Hacer el ID opcional
    val name: String,
    val genre: String,
    val country: String,
    val description: String
)
