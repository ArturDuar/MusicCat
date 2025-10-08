// File: DatabaseHelper.kt
package edu.udb.investigaciondsm2.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import edu.udb.investigaciondsm2.model.Album
import edu.udb.investigaciondsm2.model.Artista
import edu.udb.investigaciondsm2.model.Cancion
import edu.udb.investigaciondsm2.model.SongWithDetails
import edu.udb.investigaciondsm2.model.SongDetailsData

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "MusicCatDB"

        // Album Table Details
        private const val TABLE_ALBUMS = "albums"
        private const val KEY_ID = "album_id"
        private const val KEY_TITLE = "title"
        private const val KEY_ARTIST_ID = "artist_id" // Linking to an Artist table later
        private const val KEY_YEAR = "year"
        private const val KEY_GENRE = "genre"

        // Artist Table Details
        private const val TABLE_ARTISTS = "artists"
        private const val KEY_ARTIST_TABLE_ID = "artist_id"
        private const val KEY_ARTIST_NAME = "name"
        private const val KEY_ARTIST_GENRE = "genre"
        private const val KEY_ARTIST_COUNTRY = "country"
        private const val KEY_ARTIST_DESCRIPTION = "description"

        // Cancion Table Details (NUEVO)
        private const val TABLE_SONGS = "songs"
        private const val KEY_SONG_ID = "song_id"
        private const val KEY_SONG_TITLE = "title"
        private const val KEY_SONG_ALBUM_ID = "album_id" // FK to albums
        private const val KEY_SONG_ARTIST_ID = "artist_id" // FK to artists
        private const val KEY_SONG_GENRE = "genre"
        private const val KEY_SONG_DURATION = "duration" // In seconds (INT)
        private const val KEY_SONG_URL = "url"
    }

    // SQL strings (usamos IF NOT EXISTS para poder ejecutarlas en onOpen() sin romper)
    private val CREATE_ALBUMS_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS $TABLE_ALBUMS (
          $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
          $KEY_TITLE TEXT,
          $KEY_ARTIST_ID INTEGER,
          $KEY_YEAR INTEGER,
          $KEY_GENRE TEXT
        );
    """.trimIndent()

    private val CREATE_ARTISTS_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS $TABLE_ARTISTS (
          $KEY_ARTIST_TABLE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
          $KEY_ARTIST_NAME TEXT NOT NULL,
          $KEY_ARTIST_GENRE TEXT,
          $KEY_ARTIST_COUNTRY TEXT,
          $KEY_ARTIST_DESCRIPTION TEXT
        );
    """.trimIndent()

    private val CREATE_SONGS_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS $TABLE_SONGS (
          $KEY_SONG_ID INTEGER PRIMARY KEY AUTOINCREMENT,
          $KEY_SONG_TITLE TEXT NOT NULL,
          $KEY_SONG_ALBUM_ID INTEGER,
          $KEY_SONG_ARTIST_ID INTEGER,
          $KEY_SONG_GENRE TEXT,
          $KEY_SONG_DURATION INTEGER,
          $KEY_SONG_URL TEXT
        );
    """.trimIndent()

    override fun onCreate(db: SQLiteDatabase) {
        // Creamos tablas (si no existen)
        db.execSQL(CREATE_ARTISTS_TABLE_SQL)
        db.execSQL(CREATE_ALBUMS_TABLE_SQL)
        db.execSQL(CREATE_SONGS_TABLE_SQL)

        // Seeds: sólo si las tablas están vacías
        if (isTableEmpty(db, TABLE_ARTISTS)) seedArtistData(db)
        if (isTableEmpty(db, TABLE_ALBUMS)) seedAlbumData(db)
        if (isTableEmpty(db, TABLE_SONGS)) seedCancionData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Evitamos eliminar datos automáticamente — en su lugar, aseguramos que las tablas nuevas existan.
        // Si necesitas limpieza/desarrollo rápido, puedes descomentar los DROPs.
        // db.execSQL("DROP TABLE IF EXISTS $TABLE_SONGS")
        // db.execSQL("DROP TABLE IF EXISTS $TABLE_ALBUMS")
        // db.execSQL("DROP TABLE IF EXISTS $TABLE_ARTISTS")
        // onCreate(db)

        // Si estamos actualizando desde una versión antigua que no tenía songs, crearla:
        db.execSQL(CREATE_ARTISTS_TABLE_SQL)
        db.execSQL(CREATE_ALBUMS_TABLE_SQL)
        db.execSQL(CREATE_SONGS_TABLE_SQL)

        // Sembrar datos sólo si tablas vacías (evita duplicados)
        if (isTableEmpty(db, TABLE_ARTISTS)) seedArtistData(db)
        if (isTableEmpty(db, TABLE_ALBUMS)) seedAlbumData(db)
        if (isTableEmpty(db, TABLE_SONGS)) seedCancionData(db)
    }

    // onOpen se ejecuta cada vez que se abre la DB; aquí garantizamos que la tabla songs exista
    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        // Crea tablas si faltan (IF NOT EXISTS evita excepción)
        db.execSQL(CREATE_ARTISTS_TABLE_SQL)
        db.execSQL(CREATE_ALBUMS_TABLE_SQL)
        db.execSQL(CREATE_SONGS_TABLE_SQL)

        // Si songs está vacía, sembramos datos (no duplicaremos si ya hay)
        if (isTableEmpty(db, TABLE_SONGS)) {
            seedCancionData(db)
        }
        // Opcional: también sembrar artistas/álbumes si faltan
        if (isTableEmpty(db, TABLE_ARTISTS)) {
            seedArtistData(db)
        }
        if (isTableEmpty(db, TABLE_ALBUMS)) {
            seedAlbumData(db)
        }
    }

    // Helper: revisa si una tabla está vacía
    private fun isTableEmpty(db: SQLiteDatabase, tableName: String): Boolean {
        return try {
            val cursor = db.rawQuery("SELECT COUNT(1) FROM $tableName", null)
            cursor.use {
                if (it.moveToFirst()) {
                    it.getInt(0) == 0
                } else true
            }
        } catch (e: Exception) {
            // Si la tabla no existe o hay error, la consideramos "vacía" para provocar creación/seed
            true
        }
    }

    // ===================================================================
    // ==================== LÓGICA PARA ÁLBUMES (ACTUALIZADA) ============
    // ===================================================================

    // Helper function to insert an album
    private fun addAlbum(album: Album, db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put(KEY_TITLE, album.title)
            put(KEY_ARTIST_ID, album.artistId)
            put(KEY_YEAR, album.year)
            put(KEY_GENRE, album.genre)
        }
        db.insert(TABLE_ALBUMS, null, values)
    }

    // Seed data for albums (artistId referenciales asumidos según seedArtistData)
    private fun seedAlbumData(db: SQLiteDatabase) {
        addAlbum(Album(title = "El Dorado", artistId = 1, year = 2017, genre = "Latin Pop", artistName = "Shakira"), db)
        addAlbum(Album(title = "Future Nostalgia", artistId = 2, year = 2020, genre = "Pop", artistName = "Dua Lipa"), db)
        addAlbum(Album(title = "Thriller", artistId = 3, year = 1982, genre = "Pop/R&B", artistName = "Michael Jackson"), db)
        addAlbum(Album(title = "Un Verano Sin Ti", artistId = 4, year = 2022, genre = "Reggaeton", artistName = "Bad Bunny"), db)
        addAlbum(Album(title = "Lemonade", artistId = 5, year = 2016, genre = "Pop/R&B", artistName = "Beyoncé"), db)
    }

    /**
     * Retrieves all albums or filters them based on a search term, artist, and genre.
     * Usa un JOIN con la tabla de Artistas para obtener el nombre del artista real.
     */
    fun getAllAlbums(searchTerm: String = "", artistFilter: String = "", genreFilter: String = ""): List<Album> {
        val albumList = ArrayList<Album>()
        val db = this.readableDatabase

        val selectColumns = "T1.$KEY_ID, T1.$KEY_TITLE, T1.$KEY_ARTIST_ID, T1.$KEY_YEAR, T1.$KEY_GENRE, T2.$KEY_ARTIST_NAME AS artist_name"
        var query = "SELECT $selectColumns FROM $TABLE_ALBUMS AS T1 INNER JOIN $TABLE_ARTISTS AS T2 ON T1.$KEY_ARTIST_ID = T2.$KEY_ARTIST_TABLE_ID"

        val selectionArgs = mutableListOf<String>()
        val conditions = mutableListOf<String>()

        if (searchTerm.isNotBlank()) {
            conditions.add("(T1.$KEY_TITLE LIKE ? OR T2.$KEY_ARTIST_NAME LIKE ?)")
            selectionArgs.add("%$searchTerm%")
            selectionArgs.add("%$searchTerm%")
        }

        if (artistFilter.isNotBlank()) {
            conditions.add("T2.$KEY_ARTIST_NAME = ?")
            selectionArgs.add(artistFilter)
        }

        if (genreFilter.isNotBlank()) {
            conditions.add("T1.$KEY_GENRE = ?")
            selectionArgs.add(genreFilter)
        }

        if (conditions.isNotEmpty()) {
            query += " WHERE " + conditions.joinToString(" AND ")
        }

        val cursor = db.rawQuery(query, selectionArgs.toTypedArray())
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val album = Album(
                        albumId = it.getInt(it.getColumnIndexOrThrow(KEY_ID)),
                        title = it.getString(it.getColumnIndexOrThrow(KEY_TITLE)),
                        artistId = it.getInt(it.getColumnIndexOrThrow(KEY_ARTIST_ID)),
                        year = it.getInt(it.getColumnIndexOrThrow(KEY_YEAR)),
                        genre = it.getString(it.getColumnIndexOrThrow(KEY_GENRE)),
                        artistName = it.getString(it.getColumnIndexOrThrow("artist_name"))
                    )
                    albumList.add(album)
                } while (it.moveToNext())
            }
        }
        return albumList
    }

    /**
     * Gets a list of distinct values from a column (e.g., Genres or Artist Names).
     */
    fun getDistinctValues(columnName: String): List<String> {
        val distinctList = mutableListOf<String>()
        val db = this.readableDatabase

        val normalized = columnName.trim().lowercase()

        val query: String? = when (normalized) {
            KEY_ARTIST_ID, "artist_id", "artist", "artist_name", KEY_ARTIST_NAME -> {
                "SELECT DISTINCT $KEY_ARTIST_NAME FROM $TABLE_ARTISTS WHERE $KEY_ARTIST_NAME IS NOT NULL AND $KEY_ARTIST_NAME != '' ORDER BY $KEY_ARTIST_NAME ASC"
            }
            KEY_GENRE, "genre", "album_genre" -> {
                "SELECT DISTINCT $KEY_GENRE FROM $TABLE_ALBUMS WHERE $KEY_GENRE IS NOT NULL AND $KEY_GENRE != '' ORDER BY $KEY_GENRE ASC"
            }
            KEY_YEAR, "year" -> {
                "SELECT DISTINCT $KEY_YEAR FROM $TABLE_ALBUMS WHERE $KEY_YEAR IS NOT NULL ORDER BY $KEY_YEAR ASC"
            }
            else -> null
        }

        if (query != null) {
            val cursor = db.rawQuery(query, null)
            cursor.use {
                if (it.moveToFirst()) {
                    do {
                        distinctList.add(it.getString(0))
                    } while (it.moveToNext())
                }
            }
        }
        return distinctList
    }

    // ===================================================================
    // ==================== LÓGICA PARA ARTISTAS =========================
    // ===================================================================

    // Helper function to insert an artist
    private fun addArtist(artist: Artista, db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put(KEY_ARTIST_NAME, artist.name)
            put(KEY_ARTIST_GENRE, artist.genre)
            put(KEY_ARTIST_COUNTRY, artist.country)
            put(KEY_ARTIST_DESCRIPTION, artist.description)
        }
        db.insert(TABLE_ARTISTS, null, values)
    }

    // Seed data for artists
    private fun seedArtistData(db: SQLiteDatabase) {
        addArtist(Artista(name = "Shakira", genre = "Latin Pop", country = "Colombia", description = "Cantante y compositora colombiana."), db)
        addArtist(Artista(name = "Dua Lipa", genre = "Pop", country = "Reino Unido", description = "Cantante y compositora británica."), db)
        addArtist(Artista(name = "Michael Jackson", genre = "Pop/R&B", country = "EE.UU.", description = "Rey del Pop."), db)
        addArtist(Artista(name = "Bad Bunny", genre = "Reggaeton", country = "Puerto Rico", description = "Artista de reggaeton y trap."), db)
        addArtist(Artista(name = "Beyoncé", genre = "Pop/R&B", country = "EE.UU.", description = "Cantante, compositora y actriz."), db)
    }

    /**
     * Retrieves all artists or filters them based on a search term, genre, and country.
     */
    fun getAllArtists(searchTerm: String = "", genreFilter: String = "", countryFilter: String = ""): List<Artista> {
        val artistList = ArrayList<Artista>()
        val db = this.readableDatabase

        var query = "SELECT * FROM $TABLE_ARTISTS"
        val selectionArgs = mutableListOf<String>()
        val conditions = mutableListOf<String>()

        if (searchTerm.isNotBlank()) {
            conditions.add("($KEY_ARTIST_NAME LIKE ? OR $KEY_ARTIST_DESCRIPTION LIKE ?)")
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
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val artist = Artista(
                        artistaId = it.getInt(it.getColumnIndexOrThrow(KEY_ARTIST_TABLE_ID)),
                        name = it.getString(it.getColumnIndexOrThrow(KEY_ARTIST_NAME)),
                        genre = it.getString(it.getColumnIndexOrThrow(KEY_ARTIST_GENRE)),
                        country = it.getString(it.getColumnIndexOrThrow(KEY_ARTIST_COUNTRY)),
                        description = it.getString(it.getColumnIndexOrThrow(KEY_ARTIST_DESCRIPTION))
                    )
                    artistList.add(artist)
                } while (it.moveToNext())
            }
        }
        return artistList
    }

    /**
     * Gets a list of distinct values from a column for Artists (e.g., Genres or Countries)
     */
    fun getDistinctArtistValues(columnName: String): List<String> {
        val distinctList = mutableListOf<String>()
        val db = this.readableDatabase

        val normalized = columnName.trim().lowercase()
        val column = when (normalized) {
            "genre", KEY_ARTIST_GENRE -> KEY_ARTIST_GENRE
            "country", KEY_ARTIST_COUNTRY -> KEY_ARTIST_COUNTRY
            "name", KEY_ARTIST_NAME -> KEY_ARTIST_NAME
            else -> null
        } ?: return distinctList

        val query = "SELECT DISTINCT $column FROM $TABLE_ARTISTS WHERE $column IS NOT NULL AND $column != '' ORDER BY $column ASC"
        val cursor = db.rawQuery(query, null)
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    distinctList.add(it.getString(0))
                } while (it.moveToNext())
            }
        }
        return distinctList
    }

    // ===================================================================
    // ==================== LÓGICA PARA CANCIONES (NUEVO) ================
    // ===================================================================

    // Helper function to insert a song
    private fun addCancion(cancion: Cancion, db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put(KEY_SONG_TITLE, cancion.title)
            put(KEY_SONG_ALBUM_ID, cancion.albumId)
            put(KEY_SONG_ARTIST_ID, cancion.artistId)
            put(KEY_SONG_GENRE, cancion.genre)
            put(KEY_SONG_DURATION, cancion.duration)
            put(KEY_SONG_URL, cancion.url)
        }
        db.insert(TABLE_SONGS, null, values)
    }

    // Seed data for songs (using existing Album/Artist IDs)
    private fun seedCancionData(db: SQLiteDatabase) {
        addCancion(Cancion(title = "Me Enamoré", albumId = 1, artistId = 1, genre = "Latin Pop", duration = 196, url = "https://youtu.be/sPTn0QEhxds?list=RDsPTn0QEhxds"), db)
        addCancion(Cancion(title = "Chantaje", albumId = 1, artistId = 1, genre = "Reggaeton", duration = 196, url = "https://youtu.be/6Mgqbai3fKo?list=RD6Mgqbai3fKo"), db)
        addCancion(Cancion(title = "Don't Start Now", albumId = 2, artistId = 2, genre = "Pop", duration = 183, url = "https://youtu.be/oygrmJFKYZY?list=RDoygrmJFKYZY"), db)
        addCancion(Cancion(title = "Physical", albumId = 2, artistId = 2, genre = "Pop", duration = 205, url = "https://youtu.be/9HDEHj2yzew?list=RD9HDEHj2yzew"), db)
        addCancion(Cancion(title = "Thriller", albumId = 3, artistId = 3, genre = "Pop/R&B", duration = 357, url = "https://youtu.be/4V90AmXnguw?list=RD4V90AmXnguw"), db)
        addCancion(Cancion(title = "Billie Jean", albumId = 3, artistId = 3, genre = "Pop/R&B", duration = 294, url = "https://youtu.be/Zi_XLOBDo_Y?list=RDZi_XLOBDo_Y"), db)
        addCancion(Cancion(title = "Moscow Mule", albumId = 4, artistId = 4, genre = "Reggaeton", duration = 243, url = "https://youtu.be/p38WgakuYDo?list=RDp38WgakuYDo"), db)
        addCancion(Cancion(title = "Titi Me Preguntó", albumId = 4, artistId = 4, genre = "Reggaeton", duration = 243, url = "https://youtu.be/Cr8K88UcO0s?list=RDCr8K88UcO0s"), db)
        addCancion(Cancion(title = "Formation", albumId = 5, artistId = 5, genre = "R&B", duration = 217, url = "https://youtu.be/WDZJPJV__bQ?list=RDWDZJPJV__bQ"), db)
    }

    /**
     * Retrieves all songs or filters them based on a search term, album title, and artist name.
     */
    fun getAllSongs(searchTerm: String = "", albumFilter: String = "", artistFilter: String = ""): List<Cancion> {
        val songList = ArrayList<Cancion>()
        val db = this.readableDatabase

        val selectColumns = "T1.*"
        var query = "SELECT $selectColumns FROM $TABLE_SONGS AS T1 " +
                "INNER JOIN $TABLE_ARTISTS AS T2 ON T1.$KEY_SONG_ARTIST_ID = T2.$KEY_ARTIST_TABLE_ID " +
                "INNER JOIN $TABLE_ALBUMS AS T3 ON T1.$KEY_SONG_ALBUM_ID = T3.$KEY_ID"

        val selectionArgs = mutableListOf<String>()
        val conditions = mutableListOf<String>()

        if (searchTerm.isNotBlank()) {
            conditions.add("(T1.$KEY_SONG_TITLE LIKE ? OR T2.$KEY_ARTIST_NAME LIKE ? OR T3.$KEY_TITLE LIKE ?)")
            selectionArgs.add("%$searchTerm%")
            selectionArgs.add("%$searchTerm%")
            selectionArgs.add("%$searchTerm%")
        }

        if (albumFilter.isNotBlank()) {
            conditions.add("T3.$KEY_TITLE = ?")
            selectionArgs.add(albumFilter)
        }

        if (artistFilter.isNotBlank()) {
            conditions.add("T2.$KEY_ARTIST_NAME = ?")
            selectionArgs.add(artistFilter)
        }

        if (conditions.isNotEmpty()) {
            query += " WHERE " + conditions.joinToString(" AND ")
        }

        val cursor = db.rawQuery(query, selectionArgs.toTypedArray())
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val song = Cancion(
                        songId = it.getInt(it.getColumnIndexOrThrow(KEY_SONG_ID)),
                        title = it.getString(it.getColumnIndexOrThrow(KEY_SONG_TITLE)),
                        albumId = it.getInt(it.getColumnIndexOrThrow(KEY_SONG_ALBUM_ID)),
                        artistId = it.getInt(it.getColumnIndexOrThrow(KEY_SONG_ARTIST_ID)),
                        genre = it.getString(it.getColumnIndexOrThrow(KEY_SONG_GENRE)),
                        duration = it.getInt(it.getColumnIndexOrThrow(KEY_SONG_DURATION)),
                        url = it.getString(it.getColumnIndexOrThrow(KEY_SONG_URL))
                    )
                    songList.add(song)
                } while (it.moveToNext())
            }
        }
        return songList
    }

    /**
     * Gets a list of distinct values from a column for Songs (e.g., Genres, Album Titles, Artist Names)
     */
    fun getDistinctSongValues(columnName: String): List<String> {
        val distinctList = mutableListOf<String>()
        val db = this.readableDatabase

        val normalized = columnName.trim().lowercase()
        val targetQuery: String? = when (normalized) {
            KEY_SONG_GENRE, "genre" -> "SELECT DISTINCT $KEY_SONG_GENRE FROM $TABLE_SONGS WHERE $KEY_SONG_GENRE IS NOT NULL AND $KEY_SONG_GENRE != '' ORDER BY $KEY_SONG_GENRE ASC"
            KEY_ARTIST_ID, "artist_id", "artist" -> {
                "SELECT DISTINCT T2.$KEY_ARTIST_NAME FROM $TABLE_SONGS AS T1 INNER JOIN $TABLE_ARTISTS AS T2 ON T1.$KEY_SONG_ARTIST_ID = T2.$KEY_ARTIST_TABLE_ID ORDER BY T2.$KEY_ARTIST_NAME ASC"
            }
            KEY_SONG_ALBUM_ID, "album_id", "album" -> {
                "SELECT DISTINCT T2.$KEY_TITLE FROM $TABLE_SONGS AS T1 INNER JOIN $TABLE_ALBUMS AS T2 ON T1.$KEY_SONG_ALBUM_ID = T2.$KEY_ID ORDER BY T2.$KEY_TITLE ASC"
            }
            else -> null
        }

        if (targetQuery != null) {
            val cursor = db.rawQuery(targetQuery, null)
            cursor.use {
                if (it.moveToFirst()) {
                    do {
                        distinctList.add(it.getString(0))
                    } while (it.moveToNext())
                }
            }
        }
        return distinctList
    }

    /**
     * Devuelve una lista de nombres de artista distintos (para poblar el Spinner).
     */
    fun getDistinctArtistNames(): List<String> {
        val list = mutableListOf<String>()
        val db = this.readableDatabase
        val query = "SELECT DISTINCT $KEY_ARTIST_NAME FROM $TABLE_ARTISTS WHERE $KEY_ARTIST_NAME IS NOT NULL AND $KEY_ARTIST_NAME != '' ORDER BY $KEY_ARTIST_NAME ASC"
        val cursor = db.rawQuery(query, null)
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    list.add(it.getString(0))
                } while (it.moveToNext())
            }
        }
        return list
    }

    /**
     * Devuelve una lista de títulos de álbum distintos (para poblar el Spinner).
     */
    fun getDistinctAlbumTitles(): List<String> {
        val list = mutableListOf<String>()
        val db = this.readableDatabase
        val query = "SELECT DISTINCT $KEY_TITLE FROM $TABLE_ALBUMS WHERE $KEY_TITLE IS NOT NULL AND $KEY_TITLE != '' ORDER BY $KEY_TITLE ASC"
        val cursor = db.rawQuery(query, null)
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    list.add(it.getString(0))
                } while (it.moveToNext())
            }
        }
        return list
    }

    /**
     * Devuelve la lista de canciones con los datos del artista y del álbum (usada por el RecyclerView).
     * Retorna List<SongWithDetails> donde SongWithDetails contiene el objeto Cancion + artistName + albumTitle.
     */
    fun getAllSongsWithDetails(searchTerm: String = "", artistFilter: String = "", albumFilter: String = ""): List<SongWithDetails> {
        val list = mutableListOf<SongWithDetails>()
        val db = this.readableDatabase

        val selectCols = "T1.$KEY_SONG_ID, T1.$KEY_SONG_TITLE, T1.$KEY_SONG_ALBUM_ID, T1.$KEY_SONG_ARTIST_ID, T1.$KEY_SONG_GENRE, T1.$KEY_SONG_DURATION, T1.$KEY_SONG_URL, T2.$KEY_ARTIST_NAME AS artist_name, T3.$KEY_TITLE AS album_title"
        var query = "SELECT $selectCols FROM $TABLE_SONGS AS T1 " +
                "LEFT JOIN $TABLE_ARTISTS AS T2 ON T1.$KEY_SONG_ARTIST_ID = T2.$KEY_ARTIST_TABLE_ID " +
                "LEFT JOIN $TABLE_ALBUMS AS T3 ON T1.$KEY_SONG_ALBUM_ID = T3.$KEY_ID"

        val args = mutableListOf<String>()
        val conditions = mutableListOf<String>()

        if (searchTerm.isNotBlank()) {
            conditions.add("(T1.$KEY_SONG_TITLE LIKE ? OR T2.$KEY_ARTIST_NAME LIKE ? OR T3.$KEY_TITLE LIKE ?)")
            args.add("%$searchTerm%")
            args.add("%$searchTerm%")
            args.add("%$searchTerm%")
        }

        if (artistFilter.isNotBlank()) {
            conditions.add("T2.$KEY_ARTIST_NAME = ?")
            args.add(artistFilter)
        }

        if (albumFilter.isNotBlank()) {
            conditions.add("T3.$KEY_TITLE = ?")
            args.add(albumFilter)
        }

        if (conditions.isNotEmpty()) {
            query += " WHERE " + conditions.joinToString(" AND ")
        }

        val cursor = db.rawQuery(query, args.toTypedArray())
        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val song = Cancion(
                        songId = it.getInt(it.getColumnIndexOrThrow(KEY_SONG_ID)),
                        title = it.getString(it.getColumnIndexOrThrow(KEY_SONG_TITLE)),
                        albumId = it.getInt(it.getColumnIndexOrThrow(KEY_SONG_ALBUM_ID)),
                        artistId = it.getInt(it.getColumnIndexOrThrow(KEY_SONG_ARTIST_ID)),
                        genre = it.getString(it.getColumnIndexOrThrow(KEY_SONG_GENRE)),
                        duration = it.getInt(it.getColumnIndexOrThrow(KEY_SONG_DURATION)),
                        url = it.getString(it.getColumnIndexOrThrow(KEY_SONG_URL))
                    )

                    val artistNameIndex = it.getColumnIndex("artist_name")
                    val albumTitleIndex = it.getColumnIndex("album_title")
                    val artistName = if (artistNameIndex >= 0) it.getString(artistNameIndex) ?: "" else ""
                    val albumTitle = if (albumTitleIndex >= 0) it.getString(albumTitleIndex) ?: "" else ""

                    list.add(SongWithDetails(song = song, artistName = artistName, albumTitle = albumTitle))
                } while (it.moveToNext())
            }
        }
        return list
    }

    /**
     * Obtiene detalles (nombre de artista y título de álbum) para una canción específica.
     * Retorna SongDetailsData? (null si no existe la canción).
     */
    fun getSongDetails(songId: Int): SongDetailsData? {
        val db = this.readableDatabase
        val query = """
            SELECT T2.$KEY_ARTIST_NAME AS artist_name, T3.$KEY_TITLE AS album_title
            FROM $TABLE_SONGS AS T1
            LEFT JOIN $TABLE_ARTISTS AS T2 ON T1.$KEY_SONG_ARTIST_ID = T2.$KEY_ARTIST_TABLE_ID
            LEFT JOIN $TABLE_ALBUMS AS T3 ON T1.$KEY_SONG_ALBUM_ID = T3.$KEY_ID
            WHERE T1.$KEY_SONG_ID = ?
            LIMIT 1
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(songId.toString()))
        cursor.use {
            if (it.moveToFirst()) {
                val artistNameIndex = it.getColumnIndex("artist_name")
                val albumTitleIndex = it.getColumnIndex("album_title")
                val artistName = if (artistNameIndex >= 0) it.getString(artistNameIndex) ?: "" else ""
                val albumTitle = if (albumTitleIndex >= 0) it.getString(albumTitleIndex) ?: "" else ""
                return SongDetailsData(artistName = artistName, albumTitle = albumTitle)
            }
        }
        return null
    }
}
