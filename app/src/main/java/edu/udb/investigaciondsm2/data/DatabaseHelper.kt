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
        private const val DATABASE_VERSION = 4 // Incrementado para asegurar la actualización
        private const val DATABASE_NAME = "MusicCatDB"

        // Tablas
        private const val TABLE_ALBUMS = "albums"
        private const val TABLE_ARTISTS = "artists"
        private const val TABLE_SONGS = "songs"

        // Columnas Comunes
        private const val KEY_ID = "id"
        private const val KEY_TITLE = "title"
        private const val KEY_ARTIST_ID = "artist_id"
        private const val KEY_ALBUM_ID = "album_id"
        private const val KEY_GENRE = "genre"

        // Columnas de Artistas
        private const val KEY_ARTIST_NAME = "name"
        private const val KEY_ARTIST_COUNTRY = "country"
        private const val KEY_ARTIST_DESCRIPTION = "description"

        // Columnas de Álbumes
        private const val KEY_YEAR = "year"

        // Columnas de Canciones
        private const val KEY_SONG_DURATION = "duration"
        private const val KEY_SONG_URL = "url"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_ARTISTS_TABLE = ("CREATE TABLE $TABLE_ARTISTS("
                + "$KEY_ID INTEGER PRIMARY KEY,"
                + "$KEY_ARTIST_NAME TEXT NOT NULL,"
                + "$KEY_GENRE TEXT,"
                + "$KEY_ARTIST_COUNTRY TEXT,"
                + "$KEY_ARTIST_DESCRIPTION TEXT" + ")")
        db.execSQL(CREATE_ARTISTS_TABLE)

        val CREATE_ALBUMS_TABLE = ("CREATE TABLE $TABLE_ALBUMS("
                + "$KEY_ID INTEGER PRIMARY KEY,"
                + "$KEY_TITLE TEXT,"
                + "$KEY_ARTIST_ID INTEGER,"
                + "$KEY_YEAR INTEGER,"
                + "$KEY_GENRE TEXT" + ")")
        db.execSQL(CREATE_ALBUMS_TABLE)

        val CREATE_SONGS_TABLE = ("CREATE TABLE $TABLE_SONGS("
                + "$KEY_ID INTEGER PRIMARY KEY,"
                + "$KEY_TITLE TEXT NOT NULL,"
                + "$KEY_ALBUM_ID INTEGER,"
                + "$KEY_ARTIST_ID INTEGER,"
                + "$KEY_GENRE TEXT,"
                + "$KEY_SONG_DURATION INTEGER,"
                + "$KEY_SONG_URL TEXT" + ")")
        db.execSQL(CREATE_SONGS_TABLE)

        seedData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SONGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ALBUMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ARTISTS")
        onCreate(db)
    }

    // ================== Lógica para Artistas ==================

    private fun addArtist(artist: Artista, db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put(KEY_ARTIST_NAME, artist.name)
            put(KEY_GENRE, artist.genre)
            put(KEY_ARTIST_COUNTRY, artist.country)
            put(KEY_ARTIST_DESCRIPTION, artist.description)
        }
        db.insert(TABLE_ARTISTS, null, values)
    }

    fun getAllArtists(searchTerm: String = "", genreFilter: String = "", countryFilter: String = ""): List<Artista> {
        val artistList = ArrayList<Artista>()
        val db = this.readableDatabase

        val queryBuilder = StringBuilder("SELECT * FROM $TABLE_ARTISTS")
        val selectionArgs = mutableListOf<String>()
        val conditions = mutableListOf<String>()

        if (searchTerm.isNotBlank()) {
            conditions.add("$KEY_ARTIST_NAME LIKE ?")
            selectionArgs.add("%$searchTerm%")
        }

        if (genreFilter.isNotBlank() && genreFilter != "Todos") {
            conditions.add("$KEY_GENRE = ?")
            selectionArgs.add(genreFilter)
        }

        if (countryFilter.isNotBlank() && countryFilter != "Todos") {
            conditions.add("$KEY_ARTIST_COUNTRY = ?")
            selectionArgs.add(countryFilter)
        }

        if (conditions.isNotEmpty()) {
            queryBuilder.append(" WHERE ").append(conditions.joinToString(" AND "))
        }

        val cursor = db.rawQuery(queryBuilder.toString(), selectionArgs.toTypedArray())

        if (cursor.moveToFirst()) {
            do {
                val artista = Artista(
                    artistId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ARTIST_NAME)),
                    genre = cursor.getString(cursor.getColumnIndexOrThrow(KEY_GENRE)),
                    country = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ARTIST_COUNTRY)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ARTIST_DESCRIPTION))
                )
                artistList.add(artista)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return artistList
    }

    fun getDistinctArtistNames(): List<String> {
        val distinctList = mutableListOf<String>()
        val db = this.readableDatabase
        val query = "SELECT DISTINCT $KEY_ARTIST_NAME FROM $TABLE_ARTISTS ORDER BY $KEY_ARTIST_NAME ASC"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                distinctList.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return distinctList
    }

    fun getDistinctArtistGenres(): List<String> {
        val distinctList = mutableListOf<String>()
        val db = this.readableDatabase
        val query = "SELECT DISTINCT $KEY_GENRE FROM $TABLE_ARTISTS WHERE $KEY_GENRE IS NOT NULL AND $KEY_GENRE != '' ORDER BY $KEY_GENRE ASC"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                distinctList.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return distinctList
    }

    fun getDistinctArtistCountries(): List<String> {
        val distinctList = mutableListOf<String>()
        val db = this.readableDatabase
        val query = "SELECT DISTINCT $KEY_ARTIST_COUNTRY FROM $TABLE_ARTISTS WHERE $KEY_ARTIST_COUNTRY IS NOT NULL AND $KEY_ARTIST_COUNTRY != '' ORDER BY $KEY_ARTIST_COUNTRY ASC"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                distinctList.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return distinctList
    }


    // ================== Lógica para Álbumes ==================

    private fun addAlbum(album: Album, db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put(KEY_TITLE, album.title)
            put(KEY_ARTIST_ID, album.artistId)
            put(KEY_YEAR, album.year)
            put(KEY_GENRE, album.genre)
        }
        db.insert(TABLE_ALBUMS, null, values)
    }

    fun getAllAlbums(searchTerm: String = "", artistFilter: String = "", genreFilter: String = ""): List<Album> {
        val albumList = ArrayList<Album>()
        val db = this.readableDatabase

        val queryBuilder = StringBuilder(
            "SELECT a.$KEY_ID, a.$KEY_TITLE, a.$KEY_YEAR, a.$KEY_GENRE, ar.$KEY_ARTIST_NAME, a.$KEY_ARTIST_ID " +
                    "FROM $TABLE_ALBUMS a INNER JOIN $TABLE_ARTISTS ar ON a.$KEY_ARTIST_ID = ar.$KEY_ID"
        )

        val selectionArgs = mutableListOf<String>()
        val conditions = mutableListOf<String>()

        if (searchTerm.isNotBlank()) {
            conditions.add("a.$KEY_TITLE LIKE ?")
            selectionArgs.add("%$searchTerm%")
        }

        if (genreFilter.isNotBlank() && genreFilter != "Todos") {
            conditions.add("a.$KEY_GENRE = ?")
            selectionArgs.add(genreFilter)
        }

        if (artistFilter.isNotBlank() && artistFilter != "Todos") {
            conditions.add("ar.$KEY_ARTIST_NAME = ?")
            selectionArgs.add(artistFilter)
        }

        if (conditions.isNotEmpty()) {
            queryBuilder.append(" WHERE ").append(conditions.joinToString(" AND "))
        }

        val cursor = db.rawQuery(queryBuilder.toString(), selectionArgs.toTypedArray())

        if (cursor.moveToFirst()) {
            do {
                val album = Album(
                    albumId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE)),
                    artistId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ARTIST_ID)),
                    year = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_YEAR)),
                    genre = cursor.getString(cursor.getColumnIndexOrThrow(KEY_GENRE)),
                    artistName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ARTIST_NAME))
                )
                albumList.add(album)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return albumList
    }

    fun getDistinctAlbumGenres(): List<String> {
        val distinctList = mutableListOf<String>()
        val db = this.readableDatabase
        val query = "SELECT DISTINCT $KEY_GENRE FROM $TABLE_ALBUMS WHERE $KEY_GENRE IS NOT NULL AND $KEY_GENRE != '' ORDER BY $KEY_GENRE ASC"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                distinctList.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return distinctList
    }

    // ================== Lógica para Canciones ==================

    private fun addSong(song: Cancion, db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put(KEY_TITLE, song.title)
            put(KEY_ALBUM_ID, song.albumId)
            put(KEY_ARTIST_ID, song.artistId)
            put(KEY_GENRE, song.genre)
            put(KEY_SONG_DURATION, song.duration)
            put(KEY_SONG_URL, song.url)
        }
        db.insert(TABLE_SONGS, null, values)
    }

    fun getAllSongs(searchTerm: String = "", genreFilter: String = "", artistFilter: String = ""): List<Cancion> {
        val songList = ArrayList<Cancion>()
        val db = this.readableDatabase

        val queryBuilder = StringBuilder(
            "SELECT s.$KEY_ID, s.$KEY_TITLE, s.$KEY_GENRE, s.$KEY_SONG_DURATION, s.$KEY_SONG_URL, s.$KEY_ALBUM_ID, s.$KEY_ARTIST_ID, " +
                    "al.$KEY_TITLE AS album_title, ar.$KEY_ARTIST_NAME AS artist_name " +
                    "FROM $TABLE_SONGS s " +
                    "INNER JOIN $TABLE_ARTISTS ar ON s.$KEY_ARTIST_ID = ar.$KEY_ID " +
                    "INNER JOIN $TABLE_ALBUMS al ON s.$KEY_ALBUM_ID = al.$KEY_ID"
        )

        val selectionArgs = mutableListOf<String>()
        val conditions = mutableListOf<String>()

        if (searchTerm.isNotBlank()) {
            conditions.add("s.$KEY_TITLE LIKE ?")
            selectionArgs.add("%$searchTerm%")
        }

        if (genreFilter.isNotBlank() && genreFilter != "Todos") {
            conditions.add("s.$KEY_GENRE = ?")
            selectionArgs.add(genreFilter)
        }

        if (artistFilter.isNotBlank() && artistFilter != "Todos") {
            conditions.add("ar.$KEY_ARTIST_NAME = ?")
            selectionArgs.add(artistFilter)
        }

        if (conditions.isNotEmpty()) {
            queryBuilder.append(" WHERE ").append(conditions.joinToString(" AND "))
        }

        val cursor = db.rawQuery(queryBuilder.toString(), selectionArgs.toTypedArray())

        if (cursor.moveToFirst()) {
            do {
                val song = Cancion(
                    songId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE)),
                    albumId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ALBUM_ID)),
                    artistId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ARTIST_ID)),
                    genre = cursor.getString(cursor.getColumnIndexOrThrow(KEY_GENRE)),
                    duration = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SONG_DURATION)),
                    url = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SONG_URL)),
                    artistName = cursor.getString(cursor.getColumnIndexOrThrow("artist_name")),
                    albumTitle = cursor.getString(cursor.getColumnIndexOrThrow("album_title"))
                )
                songList.add(song)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return songList
    }

    fun getDistinctSongGenres(): List<String> {
        val distinctList = mutableListOf<String>()
        val db = this.readableDatabase
        val query = "SELECT DISTINCT $KEY_GENRE FROM $TABLE_SONGS WHERE $KEY_GENRE IS NOT NULL AND $KEY_GENRE != '' ORDER BY $KEY_GENRE ASC"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                distinctList.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return distinctList
    }

    // ================== Datos de Prueba ==================

    private fun seedData(db: SQLiteDatabase) {
        addArtist(Artista(name = "Shakira", genre = "Latin Pop", country = "Colombia", description = "Cantante y compositora colombiana."), db)
        addArtist(Artista(name = "Dua Lipa", genre = "Pop", country = "Reino Unido", description = "Cantante y compositora británica."), db)
        addArtist(Artista(name = "Michael Jackson", genre = "Pop/R&B", country = "EE.UU.", description = "Rey del Pop."), db)
        addArtist(Artista(name = "Bad Bunny", genre = "Reggaeton", country = "Puerto Rico", description = "Artista de reggaeton y trap."), db)
        addArtist(Artista(name = "Beyoncé", genre = "Pop/R&B", country = "EE.UU.", description = "Cantante, compositora y actriz."), db)

        addAlbum(Album(title = "El Dorado", artistId = 1, year = 2017, genre = "Latin Pop"), db)
        addAlbum(Album(title = "Future Nostalgia", artistId = 2, year = 2020, genre = "Pop"), db)
        addAlbum(Album(title = "Thriller", artistId = 3, year = 1982, genre = "Pop/R&B"), db)
        addAlbum(Album(title = "Un Verano Sin Ti", artistId = 4, year = 2022, genre = "Reggaeton"), db)
        addAlbum(Album(title = "Lemonade", artistId = 5, year = 2016, genre = "Pop/R&B"), db)

        addSong(Cancion(title = "Chantaje", albumId = 1, artistId = 1, genre = "Latin Pop", duration = 196, url = "url1"), db)
        addSong(Cancion(title = "Don't Start Now", albumId = 2, artistId = 2, genre = "Pop", duration = 183, url = "url2"), db)
        addSong(Cancion(title = "Billie Jean", albumId = 3, artistId = 3, genre = "Pop/R&B", duration = 294, url = "url3"), db)
        addSong(Cancion(title = "Tití Me Preguntó", albumId = 4, artistId = 4, genre = "Reggaeton", duration = 208, url = "url4"), db)
        addSong(Cancion(title = "Formation", albumId = 5, artistId = 5, genre = "Pop/R&B", duration = 222, url = "url5"), db)
    }
}
