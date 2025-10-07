package edu.udb.investigaciondsm2.model

data class Album(
    val albumId: Int = 0, // Primary Key in DB
    val title: String,
    val artistId: Int,
    val year: Int,
    val genre: String,
    // Note: We might want to store the artist's name too for easier display,
    // but for now, we'll keep the core DB structure.
    val artistName: String? = null // Optional: for display purposes
)