// File: DatabaseHelper.kt
package edu.udb.investigaciondsm2.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import edu.udb.investigaciondsm2.model.Album
import edu.udb.investigaciondsm2.model.Artista
import edu.udb.investigaciondsm2.model.Cancion

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 3 // ✅ Incrementamos la versión
        private const val DATABASE_NAME = "MusicCatDB"

        // Album Table Details
        private const val TABLE_ALBUMS = "albums"
        private const val KEY_ID = "album_id"
        private const val KEY_TITLE = "title"
        private const val KEY_ARTIST_ID = "artist_id"
        private const val KEY_YEAR = "year"
        private const val KEY_GENRE = "genre"

        // Artist Table Details
        private const val TABLE_ARTISTS = "artists"
        private const val KEY_ARTIST_TABLE_ID = "artist_id"
        private const val KEY_ARTIST_NAME = "name"
        private const val KEY_ARTIST_GENRE = "genre"
        private const val KEY_ARTIST_COUNTRY = "country"
        private const val KEY_ARTIST_DESCRIPTION = "description"

        // Song Table Details
        private const val TABLE_SONGS = "songs"
        private const val KEY_SONG_ID = "song_id"
        private const val KEY_SONG_TITLE = "title"
        private const val KEY_SONG_ALBUM_ID = "album_id"
        private const val KEY_SONG_ARTIST_ID = "artist_id"
        private const val KEY_SONG_GENRE = "genre"
        private const val KEY_SONG_DURATION = "duration"
        private const val KEY_SONG_URL = "url"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // --- Creación de tabla de álbumes ---
        val CREATE_ALBUMS_TABLE = ("CREATE TABLE $TABLE_ALBUMS("
                + "$KEY_ID INTEGER PRIMARY KEY,"
                + "$KEY_TITLE TEXT,"
                + "$KEY_ARTIST_ID INTEGER,"
                + "$KEY_YEAR INTEGER,"
                + "$KEY_GENRE TEXT" + ")")
        db.execSQL(CREATE_ALBUMS_TABLE)

        // --- Creación de tabla de artistas ---
        val CREATE_ARTISTS_TABLE = ("CREATE TABLE $TABLE_ARTISTS("
                + "$KEY_ARTIST_TABLE_ID INTEGER PRIMARY KEY,"
                + "$KEY_ARTIST_NAME TEXT NOT NULL,"
                + "$KEY_ARTIST_GENRE TEXT,"
                + "$KEY_ARTIST_COUNTRY TEXT,"
                + "$KEY_ARTIST_DESCRIPTION TEXT" + ")")
        db.execSQL(CREATE_ARTISTS_TABLE)

        // --- Creación de tabla de canciones ---
        val CREATE_SONGS_TABLE = ("CREATE TABLE $TABLE_SONGS("
                + "$KEY_SONG_ID INTEGER PRIMARY KEY,"
                + "$KEY_SONG_TITLE TEXT NOT NULL,"
                + "$KEY_SONG_ALBUM_ID INTEGER,"
                + "$KEY_SONG_ARTIST_ID INTEGER,"
                + "$KEY_SONG_GENRE TEXT,"
                + "$KEY_SONG_DURATION INTEGER,"
                + "$KEY_SONG_URL TEXT" + ")")
        db.execSQL(CREATE_SONGS_TABLE)

        // --- Datos iniciales ---
        seedArtistData(db)
        seedAlbumData(db)
        seedSongData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SONGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ALBUMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ARTISTS")
        onCreate(db)
    }

    // ===================================================================
    // ==================== LÓGICA PARA ÁLBUMES ==========================
    // ===================================================================

    private fun addAlbum(album: Album, db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put(KEY_TITLE, album.title)
            put(KEY_ARTIST_ID, album.artistId)
            put(KEY_YEAR, album.year)
            put(KEY_GENRE, album.genre)
        }
        db.insert(TABLE_ALBUMS, null, values)
    }

    private fun seedAlbumData(db: SQLiteDatabase) {
        addAlbum(Album(title = "El Dorado", artistId = 1, year = 2017, genre = "Latin Pop", artistName = "Shakira"), db)
        addAlbum(Album(title = "Future Nostalgia", artistId = 2, year = 2020, genre = "Pop", artistName = "Dua Lipa"), db)
        addAlbum(Album(title = "Thriller", artistId = 3, year = 1982, genre = "Pop/R&B", artistName = "Michael Jackson"), db)
        addAlbum(Album(title = "Un Verano Sin Ti", artistId = 4, year = 2022, genre = "Reggaeton", artistName = "Bad Bunny"), db)
        addAlbum(Album(title = "Lemonade", artistId = 5, year = 2016, genre = "Pop/R&B", artistName = "Beyoncé"), db)
    }

    fun getAllAlbums(searchTerm: String = "", artistFilter: String = "", genreFilter: String = ""): List<Album> {
        val albumList = ArrayList<Album>()
        val db = this.readableDatabase

        val mockArtistNames = mapOf(
            1 to "Shakira",
            2 to "Dua Lipa",
            3 to "Michael Jackson",
            4 to "Bad Bunny",
            5 to "Beyoncé"
        )

        var query = "SELECT * FROM $TABLE_ALBUMS"
        val selectionArgs = mutableListOf<String>()
        val conditions = mutableListOf<String>()

        if (searchTerm.isNotBlank()) {
            val artistIdsForSearch = mockArtistNames.filterValues {
                it.contains(searchTerm, ignoreCase = true)
            }.keys.joinToString()

            conditions.add("$KEY_TITLE LIKE ? OR $KEY_ARTIST_ID IN ($artistIdsForSearch)")
            selectionArgs.add("%$searchTerm%")
        }

        if (artistFilter.isNotBlank()) {
            val artistId = mockArtistNames.entries.find { it.value == artistFilter }?.key
            if (artistId != null) {
                conditions.add("$KEY_ARTIST_ID = ?")
                selectionArgs.add(artistId.toString())
            }
        }

        if (genreFilter.isNotBlank()) {
            conditions.add("$KEY_GENRE = ?")
            selectionArgs.add(genreFilter)
        }

        if (conditions.isNotEmpty()) {
            query += " WHERE " + conditions.joinToString(" AND ")
        }

        val cursor = db.rawQuery(query, selectionArgs.toTypedArray())

        if (cursor.moveToFirst()) {
            do {
                val album = Album(
                    albumId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE)),
                    artistId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ARTIST_ID)),
                    year = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_YEAR)),
                    genre = cursor.getString(cursor.getColumnIndexOrThrow(KEY_GENRE)),
                    artistName = mockArtistNames[cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ARTIST_ID))]
                )
                albumList.add(album)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return albumList
    }

    fun getDistinctValues(columnName: String): List<String> {
        val distinctList = mutableListOf<String>()
        val db = this.readableDatabase

        if (columnName == KEY_ARTIST_ID) {
            return listOf("Shakira", "Dua Lipa", "Michael Jackson", "Bad Bunny", "Beyoncé").distinct().sorted()
        }

        val query = "SELECT DISTINCT $columnName FROM $TABLE_ALBUMS ORDER BY $columnName ASC"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                distinctList.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return distinctList
    }

    // ===================================================================
    // ==================== LÓGICA PARA ARTISTAS =========================
    // ===================================================================

    private fun addArtist(artist: Artista, db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put(KEY_ARTIST_NAME, artist.name)
            put(KEY_ARTIST_GENRE, artist.genre)
            put(KEY_ARTIST_COUNTRY, artist.country)
            put(KEY_ARTIST_DESCRIPTION, artist.description)
        }
        db.insert(TABLE_ARTISTS, null, values)
    }

    private fun seedArtistData(db: SQLiteDatabase) {
        addArtist(Artista(name = "Shakira", genre = "Latin Pop", country = "Colombia", description = "Cantante y compositora colombiana."), db)
        addArtist(Artista(name = "Dua Lipa", genre = "Pop", country = "Reino Unido", description = "Cantante y compositora británica."), db)
        addArtist(Artista(name = "Michael Jackson", genre = "Pop/R&B", country = "EE.UU.", description = "Rey del Pop."), db)
        addArtist(Artista(name = "Bad Bunny", genre = "Reggaeton", country = "Puerto Rico", description = "Artista de reggaeton y trap."), db)
        addArtist(Artista(name = "Beyoncé", genre = "Pop/R&B", country = "EE.UU.", description = "Cantante, compositora y actriz."), db)
    }

    fun getAllArtists(searchTerm: String = "", genreFilter: String = "", countryFilter: String = ""): List<Artista> {
        val artistList = ArrayList<Artista>()
        val db = this.readableDatabase

        var query = "SELECT * FROM $TABLE_ARTISTS"
        val selectionArgs = mutableListOf<String>()
        val conditions = mutableListOf<String>()

        if (searchTerm.isNotBlank()) {
            conditions.add("$KEY_ARTIST_NAME LIKE ? OR $KEY_ARTIST_DESCRIPTION LIKE ?")
            selectionArgs.add("%$searchTerm%")
            selectionArgs.add("%$searchTerm%")
        }

        if (genreFilter.isNotBlank()) {
            conditions.add("$KEY_ARTIST_GENRE = ?")
            selectionArgs.add(genreFilter)
        }

        if (countryFilter.isNotBlank()) {
            conditions.add("$KEY_ARTIST_COUNTRY = ?")
            selectionArgs.add(countryFilter)
        }

        if (conditions.isNotEmpty()) {
            query += " WHERE " + conditions.joinToString(" AND ")
        }

        val cursor = db.rawQuery(query, selectionArgs.toTypedArray())

        if (cursor.moveToFirst()) {
            do {
                val artist = Artista(
                    artistaId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ARTIST_TABLE_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ARTIST_NAME)),
                    genre = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ARTIST_GENRE)),
                    country = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ARTIST_COUNTRY)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ARTIST_DESCRIPTION))
                )
                artistList.add(artist)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return artistList
    }

    fun getDistinctArtistValues(columnName: String): List<String> {
        val distinctList = mutableListOf<String>()
        val db = this.readableDatabase

        if (columnName != KEY_ARTIST_GENRE && columnName != KEY_ARTIST_COUNTRY) {
            return distinctList
        }

        val query = "SELECT DISTINCT $columnName FROM $TABLE_ARTISTS WHERE $columnName IS NOT NULL AND $columnName != '' ORDER BY $columnName ASC"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                distinctList.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return distinctList
    }

    // ===================================================================
    // ==================== LÓGICA PARA CANCIONES ========================
    // ===================================================================

    private fun addSong(song: Cancion, db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put(KEY_SONG_TITLE, song.title)
            put(KEY_SONG_ALBUM_ID, song.albumId)
            put(KEY_SONG_ARTIST_ID, song.artistId)
            put(KEY_SONG_GENRE, song.genre)
            put(KEY_SONG_DURATION, song.duration)
            put(KEY_SONG_URL, song.url)
        }
        db.insert(TABLE_SONGS, null, values)
    }

    private fun seedSongData(db: SQLiteDatabase) {
        // Canciones de Shakira - El Dorado (Album ID: 1)
        addSong(Cancion(title = "Chantaje", albumId = 1, artistId = 1, genre = "Latin Pop", duration = 195, url = "https://music.example.com/chantaje"), db)
        addSong(Cancion(title = "Me Enamoré", albumId = 1, artistId = 1, genre = "Latin Pop", duration = 210, url = "https://music.example.com/meenamore"), db)

        // Canciones de Dua Lipa - Future Nostalgia (Album ID: 2)
        addSong(Cancion(title = "Don't Start Now", albumId = 2, artistId = 2, genre = "Pop", duration = 183, url = "https://music.example.com/dontstartnow"), db)
        addSong(Cancion(title = "Levitating", albumId = 2, artistId = 2, genre = "Pop", duration = 203, url = "https://music.example.com/levitating"), db)
        addSong(Cancion(title = "Physical", albumId = 2, artistId = 2, genre = "Pop", duration = 193, url = "https://music.example.com/physical"), db)

        // Canciones de Michael Jackson - Thriller (Album ID: 3)
        addSong(Cancion(title = "Thriller", albumId = 3, artistId = 3, genre = "Pop/R&B", duration = 357, url = "https://music.example.com/thriller"), db)
        addSong(Cancion(title = "Beat It", albumId = 3, artistId = 3, genre = "Pop/R&B", duration = 258, url = "https://music.example.com/beatit"), db)
        addSong(Cancion(title = "Billie Jean", albumId = 3, artistId = 3, genre = "Pop/R&B", duration = 294, url = "https://music.example.com/billiejean"), db)

        // Canciones de Bad Bunny - Un Verano Sin Ti (Album ID: 4)
        addSong(Cancion(title = "Moscow Mule", albumId = 4, artistId = 4, genre = "Reggaeton", duration = 242, url = "https://music.example.com/moscowmule"), db)
        addSong(Cancion(title = "Tití Me Preguntó", albumId = 4, artistId = 4, genre = "Reggaeton", duration = 256, url = "https://music.example.com/titimepregunton"), db)

        // Canciones de Beyoncé - Lemonade (Album ID: 5)
        addSong(Cancion(title = "Formation", albumId = 5, artistId = 5, genre = "Pop/R&B", duration = 206, url = "https://music.example.com/formation"), db)
        addSong(Cancion(title = "Sorry", albumId = 5, artistId = 5, genre = "Pop/R&B", duration = 232, url = "https://music.example.com/sorry"), db)
        addSong(Cancion(title = "Hold Up", albumId = 5, artistId = 5, genre = "Pop/R&B", duration = 221, url = "https://music.example.com/holdup"), db)
    }

    fun getAllSongs(searchTerm: String = "", artistFilter: String = "", genreFilter: String = "", albumFilter: String = ""): List<Cancion> {
        val songList = ArrayList<Cancion>()
        val db = this.readableDatabase

        // Mapas de referencia (mock data)
        val artistNames = mapOf(
            1 to "Shakira",
            2 to "Dua Lipa",
            3 to "Michael Jackson",
            4 to "Bad Bunny",
            5 to "Beyoncé"
        )

        val albumNames = mapOf(
            1 to "El Dorado",
            2 to "Future Nostalgia",
            3 to "Thriller",
            4 to "Un Verano Sin Ti",
            5 to "Lemonade"
        )

        var query = "SELECT * FROM $TABLE_SONGS"
        val selectionArgs = mutableListOf<String>()
        val conditions = mutableListOf<String>()

        // 1. Búsqueda por término (Título de canción)
        if (searchTerm.isNotBlank()) {
            conditions.add("$KEY_SONG_TITLE LIKE ?")
            selectionArgs.add("%$searchTerm%")
        }

        // 2. Filtro por Artista
        if (artistFilter.isNotBlank()) {
            val artistId = artistNames.entries.find { it.value == artistFilter }?.key
            if (artistId != null) {
                conditions.add("$KEY_SONG_ARTIST_ID = ?")
                selectionArgs.add(artistId.toString())
            }
        }

        // 3. Filtro por Género
        if (genreFilter.isNotBlank()) {
            conditions.add("$KEY_SONG_GENRE = ?")
            selectionArgs.add(genreFilter)
        }

        // 4. Filtro por Álbum
        if (albumFilter.isNotBlank()) {
            val albumId = albumNames.entries.find { it.value == albumFilter }?.key
            if (albumId != null) {
                conditions.add("$KEY_SONG_ALBUM_ID = ?")
                selectionArgs.add(albumId.toString())
            }
        }

        if (conditions.isNotEmpty()) {
            query += " WHERE " + conditions.joinToString(" AND ")
        }

        val cursor = db.rawQuery(query, selectionArgs.toTypedArray())

        if (cursor.moveToFirst()) {
            do {
                val artistId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SONG_ARTIST_ID))
                val albumId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SONG_ALBUM_ID))

                val song = Cancion(
                    songId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SONG_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SONG_TITLE)),
                    albumId = albumId,
                    artistId = artistId,
                    genre = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SONG_GENRE)),
                    duration = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SONG_DURATION)),
                    url = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SONG_URL)),
                    artistName = artistNames[artistId],
                    albumName = albumNames[albumId]
                )
                songList.add(song)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return songList
    }

    fun getDistinctSongValues(columnName: String): List<String> {
        val distinctList = mutableListOf<String>()
        val db = this.readableDatabase

        when (columnName) {
            KEY_SONG_ARTIST_ID -> {
                return listOf("Shakira", "Dua Lipa", "Michael Jackson", "Bad Bunny", "Beyoncé").distinct().sorted()
            }
            KEY_SONG_ALBUM_ID -> {
                return listOf("El Dorado", "Future Nostalgia", "Thriller", "Un Verano Sin Ti", "Lemonade").distinct().sorted()
            }
            KEY_SONG_GENRE -> {
                val query = "SELECT DISTINCT $KEY_SONG_GENRE FROM $TABLE_SONGS WHERE $KEY_SONG_GENRE IS NOT NULL AND $KEY_SONG_GENRE != '' ORDER BY $KEY_SONG_GENRE ASC"
                val cursor = db.rawQuery(query, null)
                if (cursor.moveToFirst()) {
                    do {
                        distinctList.add(cursor.getString(0))
                    } while (cursor.moveToNext())
                }
                cursor.close()
            }
        }
        return distinctList
    }
}