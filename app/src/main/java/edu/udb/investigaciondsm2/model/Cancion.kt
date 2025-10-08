// File: Cancion.kt
package edu.udb.investigaciondsm2.model

data class Cancion(
    val songId: Int = 0, // Primary Key in DB
    val title: String,
    val albumId: Int, // Foreign Key
    val artistId: Int, // Foreign Key
    val genre: String,
    val duration: Int, // En segundos
    val url: String,
)