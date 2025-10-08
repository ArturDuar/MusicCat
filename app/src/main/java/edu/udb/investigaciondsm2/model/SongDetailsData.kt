// File: SongDetailsData.kt
package edu.udb.investigaciondsm2.model

/**
 * Data class simplificada para devolver solo el nombre del artista y el título del álbum
 * para el modal de detalles de la canción.
 */
data class SongDetailsData(
    val artistName: String,
    val albumTitle: String
)