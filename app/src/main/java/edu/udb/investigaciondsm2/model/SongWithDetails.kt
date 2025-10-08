// File: SongWithDetails.kt
package edu.udb.investigaciondsm2.model

// Importamos la data class Cancion (asumiendo que está en el mismo paquete)
import edu.udb.investigaciondsm2.model.Cancion

/**
 * Data class para representar una Cancion junto con el Nombre del Artista
 * y el Título del Álbum, obtenidos de un JOIN en la base de datos.
 * Esta clase es usada por el RecyclerView Adapter.
 */
data class SongWithDetails(
    val song: Cancion,
    val artistName: String,
    val albumTitle: String
)