// File: model/Cancion.kt
package edu.udb.investigaciondsm2.model

data class Cancion(
    val songId: Int? = null, // Hacer el ID opcional
    val title: String,
    val albumId: Int,
    val artistId: Int,
    val genre: String,
    val duration: Int,
    val url: String,
    val artistName: String? = null,
    val albumTitle: String? = null
)
