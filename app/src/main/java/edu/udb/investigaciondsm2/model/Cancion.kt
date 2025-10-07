package edu.udb.investigaciondsm2.model


data class Cancion(
    val songId: Int = 0, // Primary Key in DB
    val title: String,
    val albumId: Int,
    val artistId: Int,
    val genre: String,
    val duration: Int,
    val url: String,
)
