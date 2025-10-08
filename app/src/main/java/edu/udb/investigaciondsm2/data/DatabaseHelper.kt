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
        addAlbum(Album(title = "25", artistId = 6, year = 2015, genre = "Pop/Soul", artistName = "Adele"), db)
        addAlbum(Album(title = "÷ (Divide)", artistId = 7, year = 2017, genre = "Pop", artistName = "Ed Sheeran"), db)
        addAlbum(Album(title = "1989", artistId = 8, year = 2014, genre = "Pop", artistName = "Taylor Swift"), db)
        addAlbum(Album(title = "24K Magic", artistId = 9, year = 2016, genre = "Funk/Pop", artistName = "Bruno Mars"), db)
        addAlbum(Album(title = "After Hours", artistId = 10, year = 2020, genre = "R&B/Pop", artistName = "The Weeknd"), db)
        addAlbum(Album(title = "A Head Full of Dreams", artistId = 11, year = 2015, genre = "Pop Rock", artistName = "Coldplay"), db)
        addAlbum(Album(title = "Chromatica", artistId = 12, year = 2020, genre = "Pop", artistName = "Lady Gaga"), db)
        addAlbum(Album(title = "Evolve", artistId = 13, year = 2017, genre = "Pop Rock", artistName = "Imagine Dragons"), db)
        addAlbum(Album(title = "Motomami", artistId = 14, year = 2022, genre = "Flamenco/Pop", artistName = "Rosalía"), db)
        addAlbum(Album(title = "Mañana Será Bonito", artistId = 15, year = 2023, genre = "Reggaeton", artistName = "Karol G"), db)
        addAlbum(Album(title = "Happier Than Ever", artistId = 16, year = 2021, genre = "Pop Alternativo", artistName = "Billie Eilish"), db)
        addAlbum(Album(title = "Hollywood's Bleeding", artistId = 17, year = 2019, genre = "Pop/Rap", artistName = "Post Malone"), db)
        addAlbum(Album(title = "Scorpion", artistId = 18, year = 2018, genre = "Hip-Hop/R&B", artistName = "Drake"), db)
        addAlbum(Album(title = "Positions", artistId = 19, year = 2020, genre = "Pop/R&B", artistName = "Ariana Grande"), db)
        addAlbum(Album(title = "Fine Line", artistId = 20, year = 2019, genre = "Pop/Rock", artistName = "Harry Styles"), db)
        addAlbum(Album(title = "This Is Acting", artistId = 21, year = 2016, genre = "Pop", artistName = "Sia"), db)
        addAlbum(Album(title = "Camila", artistId = 22, year = 2018, genre = "Pop/Latin", artistName = "Camila Cabello"), db)
        addAlbum(Album(title = "Vida", artistId = 23, year = 2019, genre = "Latin Pop", artistName = "Luis Fonsi"), db)
        addAlbum(Album(title = "Rare", artistId = 24, year = 2020, genre = "Pop", artistName = "Selena Gomez"), db)
        addAlbum(Album(title = "Endless Summer Vacation", artistId = 25, year = 2023, genre = "Pop/Rock", artistName = "Miley Cyrus"), db)

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
        addArtist(Artista(name = "Adele", genre = "Pop/Soul", country = "Reino Unido", description = "Cantante y compositora británica ganadora de múltiples premios Grammy."), db)
        addArtist(Artista(name = "Ed Sheeran", genre = "Pop", country = "Reino Unido", description = "Cantautor británico reconocido por sus letras románticas y acústicas."), db)
        addArtist(Artista(name = "Taylor Swift", genre = "Pop/Country", country = "EE.UU.", description = "Cantautora estadounidense que mezcla pop, country y folk."), db)
        addArtist(Artista(name = "Bruno Mars", genre = "Pop/Funk", country = "EE.UU.", description = "Cantante y productor con gran influencia del soul y el funk."), db)
        addArtist(Artista(name = "The Weeknd", genre = "R&B/Pop", country = "Canadá", description = "Artista canadiense conocido por su estilo oscuro y melódico."), db)
        addArtist(Artista(name = "Coldplay", genre = "Rock Alternativo", country = "Reino Unido", description = "Banda británica de rock alternativo con influencias pop."), db)
        addArtist(Artista(name = "Lady Gaga", genre = "Pop", country = "EE.UU.", description = "Cantante y actriz reconocida por su estilo excéntrico e innovador."), db)
        addArtist(Artista(name = "Imagine Dragons", genre = "Pop Rock", country = "EE.UU.", description = "Banda estadounidense con éxitos de rock alternativo y pop."), db)
        addArtist(Artista(name = "Rosalía", genre = "Flamenco/Pop", country = "España", description = "Cantante española que fusiona flamenco con reggaetón y pop."), db)
        addArtist(Artista(name = "Karol G", genre = "Reggaeton", country = "Colombia", description = "Cantante colombiana reconocida por sus éxitos urbanos y colaboraciones."), db)
        addArtist(Artista(name = "Billie Eilish", genre = "Pop Alternativo", country = "EE.UU.", description = "Artista estadounidense con estilo experimental y melancólico."), db)
        addArtist(Artista(name = "Post Malone", genre = "Hip-Hop/Pop", country = "EE.UU.", description = "Rapero y cantante con mezcla de hip-hop, pop y rock."), db)
        addArtist(Artista(name = "Drake", genre = "Hip-Hop/R&B", country = "Canadá", description = "Rapero y cantante canadiense con influencia pop."), db)
        addArtist(Artista(name = "Ariana Grande", genre = "Pop/R&B", country = "EE.UU.", description = "Cantante estadounidense con voz potente y múltiples éxitos globales."), db)
        addArtist(Artista(name = "Harry Styles", genre = "Pop/Rock", country = "Reino Unido", description = "Ex integrante de One Direction, cantante y actor británico."), db)
        addArtist(Artista(name = "Sia", genre = "Pop", country = "Australia", description = "Cantautora australiana reconocida por su potente voz y composiciones emotivas."), db)
        addArtist(Artista(name = "Camila Cabello", genre = "Pop/Latin", country = "Cuba", description = "Cantante cubano-estadounidense, ex integrante de Fifth Harmony."), db)
        addArtist(Artista(name = "Luis Fonsi", genre = "Latin Pop", country = "Puerto Rico", description = "Cantante puertorriqueño conocido por 'Despacito'."), db)
        addArtist(Artista(name = "Selena Gomez", genre = "Pop", country = "EE.UU.", description = "Cantante y actriz con una mezcla de pop y dance."), db)
        addArtist(Artista(name = "Miley Cyrus", genre = "Pop/Rock", country = "EE.UU.", description = "Cantante y actriz estadounidense con estilo versátil."), db)

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
        addCancion(Cancion(title = "Hello", albumId = 6, artistId = 6, genre = "Pop/Soul", duration = 295, url = "https://youtu.be/YQHsXMglC9A"), db)
        addCancion(Cancion(title = "Send My Love", albumId = 6, artistId = 6, genre = "Pop", duration = 223, url = "https://youtu.be/fk4BbF7B29w"), db)

        addCancion(Cancion(title = "Shape of You", albumId = 7, artistId = 7, genre = "Pop", duration = 234, url = "https://youtu.be/JGwWNGJdvx8"), db)
        addCancion(Cancion(title = "Perfect", albumId = 7, artistId = 7, genre = "Pop", duration = 263, url = "https://youtu.be/2Vv-BfVoq4g"), db)

        addCancion(Cancion(title = "Blank Space", albumId = 8, artistId = 8, genre = "Pop", duration = 231, url = "https://youtu.be/e-ORhEE9VVg"), db)
        addCancion(Cancion(title = "Style", albumId = 8, artistId = 8, genre = "Pop", duration = 231, url = "https://youtu.be/-CmadmM5cOk"), db)

        addCancion(Cancion(title = "24K Magic", albumId = 9, artistId = 9, genre = "Funk", duration = 227, url = "https://youtu.be/UqyT8IEBkvY"), db)
        addCancion(Cancion(title = "That's What I Like", albumId = 9, artistId = 9, genre = "Pop", duration = 187, url = "https://youtu.be/PMivT7MJ41M"), db)

        addCancion(Cancion(title = "Blinding Lights", albumId = 10, artistId = 10, genre = "Synth-Pop", duration = 200, url = "https://youtu.be/fHI8X4OXluQ"), db)
        addCancion(Cancion(title = "Save Your Tears", albumId = 10, artistId = 10, genre = "R&B", duration = 215, url = "https://youtu.be/XXYlFuWEuKI"), db)

        addCancion(Cancion(title = "Adventure of a Lifetime", albumId = 11, artistId = 11, genre = "Pop Rock", duration = 260, url = "https://youtu.be/QtXby3twMmI"), db)
        addCancion(Cancion(title = "Hymn for the Weekend", albumId = 11, artistId = 11, genre = "Pop", duration = 258, url = "https://youtu.be/YykjpeuMNEk"), db)

        addCancion(Cancion(title = "Rain On Me", albumId = 12, artistId = 12, genre = "Pop", duration = 182, url = "https://youtu.be/AoAm4om0wTs"), db)
        addCancion(Cancion(title = "Stupid Love", albumId = 12, artistId = 12, genre = "Pop", duration = 193, url = "https://youtu.be/5L6xyaeiV58"), db)

        addCancion(Cancion(title = "Believer", albumId = 13, artistId = 13, genre = "Rock", duration = 204, url = "https://youtu.be/7wtfhZwyrcc"), db)
        addCancion(Cancion(title = "Thunder", albumId = 13, artistId = 13, genre = "Pop Rock", duration = 187, url = "https://youtu.be/fKopy74weus"), db)

        addCancion(Cancion(title = "Despechá", albumId = 14, artistId = 14, genre = "Pop/Reggaeton", duration = 186, url = "https://youtu.be/RA3H3kK7b_M"), db)
        addCancion(Cancion(title = "Saoko", albumId = 14, artistId = 14, genre = "Flamenco/Pop", duration = 164, url = "https://youtu.be/_9a5g-6r7VQ"), db)

        addCancion(Cancion(title = "Provenza", albumId = 15, artistId = 15, genre = "Reggaeton", duration = 210, url = "https://youtu.be/4D9W8HBp3PQ"), db)
        addCancion(Cancion(title = "TQG", albumId = 15, artistId = 15, genre = "Reggaeton", duration = 195, url = "https://youtu.be/jZGpkLElSu8"), db)

        addCancion(Cancion(title = "Happier Than Ever", albumId = 16, artistId = 16, genre = "Pop Alternativo", duration = 298, url = "https://youtu.be/5GJWxDKyk3A"), db)
        addCancion(Cancion(title = "Therefore I Am", albumId = 16, artistId = 16, genre = "Pop", duration = 174, url = "https://youtu.be/5GJWxDKyk3A"), db)

        addCancion(Cancion(title = "Circles", albumId = 17, artistId = 17, genre = "Pop", duration = 215, url = "https://youtu.be/wXhTHyIgQ_U"), db)
        addCancion(Cancion(title = "Wow.", albumId = 17, artistId = 17, genre = "Hip-Hop", duration = 150, url = "https://youtu.be/Dwzk-XZxZ4k"), db)

        addCancion(Cancion(title = "God’s Plan", albumId = 18, artistId = 18, genre = "Hip-Hop", duration = 198, url = "https://youtu.be/xpVfcZ0ZcFM"), db)
        addCancion(Cancion(title = "In My Feelings", albumId = 18, artistId = 18, genre = "Hip-Hop", duration = 218, url = "https://youtu.be/drS9a99gX_A"), db)

        addCancion(Cancion(title = "Positions", albumId = 19, artistId = 19, genre = "Pop/R&B", duration = 172, url = "https://youtu.be/tcYodQoapMg"), db)
        addCancion(Cancion(title = "34+35", albumId = 19, artistId = 19, genre = "Pop", duration = 211, url = "https://youtu.be/B6_iQvaIjXw"), db)

        addCancion(Cancion(title = "Adore You", albumId = 20, artistId = 20, genre = "Pop", duration = 207, url = "https://youtu.be/VF-r5TtlT9w"), db)
        addCancion(Cancion(title = "Watermelon Sugar", albumId = 20, artistId = 20, genre = "Pop/Rock", duration = 174, url = "https://youtu.be/E07s5ZYygMg"), db)

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
