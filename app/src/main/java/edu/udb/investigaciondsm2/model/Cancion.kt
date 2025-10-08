package edu.udb.investigaciondsm2.model

data class Cancion(
    val songId: Int = 0, // Primary Key in DB
    val title: String,
    val albumId: Int,
    val artistId: Int,
    val genre: String,
    val duration: Int, // Duración en segundos
    val url: String,
    // Campos opcionales para display
    val artistName: String? = null,
    val albumName: String? = null
) {
    // Función helper para formatear la duración
    fun getFormattedDuration(): String {
        val minutes = duration / 60
        val seconds = duration % 60
        return String.format("%d:%02d", minutes, seconds)
    }
}
