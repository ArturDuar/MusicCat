// File: model/Album.kt
package edu.udb.investigaciondsm2.model

data class Album(
    val albumId: Int? = null, // Hacer el ID opcional
    val title: String,
    val artistId: Int,
    val year: Int,
    val genre: String,
    val artistName: String? = null
)
