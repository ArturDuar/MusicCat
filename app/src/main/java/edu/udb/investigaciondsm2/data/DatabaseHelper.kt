// File: DatabaseHelper.kt
package edu.udb.investigaciondsm2.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import edu.udb.investigaciondsm2.model.Album

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "MusicCatDB"

        // Album Table Details
        private const val TABLE_ALBUMS = "albums"
        private const val KEY_ID = "album_id"
        private const val KEY_TITLE = "title"
        private const val KEY_ARTIST_ID = "artist_id" // Linking to an Artist table later
        private const val KEY_YEAR = "year"
        private const val KEY_GENRE = "genre"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_ALBUMS_TABLE = ("CREATE TABLE $TABLE_ALBUMS("
                + "$KEY_ID INTEGER PRIMARY KEY,"
                + "$KEY_TITLE TEXT,"
                + "$KEY_ARTIST_ID INTEGER,"
                + "$KEY_YEAR INTEGER,"
                + "$KEY_GENRE TEXT" + ")")
        db.execSQL(CREATE_ALBUMS_TABLE)

        // Seed initial data for demonstration
        seedData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ALBUMS")
        // Create tables again
        onCreate(db)
    }

    // Helper function to insert data
    private fun addAlbum(album: Album, db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put(KEY_TITLE, album.title)
            put(KEY_ARTIST_ID, album.artistId)
            put(KEY_YEAR, album.year)
            put(KEY_GENRE, album.genre)
        }
        db.insert(TABLE_ALBUMS, null, values)
    }

    // Seed data with some initial records
    private fun seedData(db: SQLiteDatabase) {
        addAlbum(Album(title = "El Dorado", artistId = 1, year = 2017, genre = "Latin Pop", artistName = "Shakira"), db)
        addAlbum(Album(title = "Future Nostalgia", artistId = 2, year = 2020, genre = "Pop", artistName = "Dua Lipa"), db)
        addAlbum(Album(title = "Thriller", artistId = 3, year = 1982, genre = "Pop/R&B", artistName = "Michael Jackson"), db)
        addAlbum(Album(title = "Un Verano Sin Ti", artistId = 4, year = 2022, genre = "Reggaeton", artistName = "Bad Bunny"), db)
        addAlbum(Album(title = "Lemonade", artistId = 5, year = 2016, genre = "Pop/R&B", artistName = "Beyoncé"), db)
    }

    /**
     * Retrieves all albums or filters them based on a search term, artist, and genre.
     * Includes a mock artist name for display since we don't have an Artists table yet.
     */
    fun getAllAlbums(searchTerm: String = "", artistFilter: String = "", genreFilter: String = ""): List<Album> {
        val albumList = ArrayList<Album>()
        val db = this.readableDatabase

        // We use a simplified map for artist names since the Artist table isn't implemented.
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

        // 1. Keyword Search (Title or Mock Artist Name)
        if (searchTerm.isNotBlank()) {
            // Find artist IDs for the search term, assuming the search can be by artist name
            val artistIdsForSearch = mockArtistNames.filterValues {
                it.contains(searchTerm, ignoreCase = true)
            }.keys.joinToString()

            conditions.add("$KEY_TITLE LIKE ? OR $KEY_ARTIST_ID IN ($artistIdsForSearch)")
            selectionArgs.add("%$searchTerm%")
        }

        // 2. Artist Filter (Requires getting the mock Artist ID from name - simplified)
        if (artistFilter.isNotBlank()) {
            val artistId = mockArtistNames.entries.find { it.value == artistFilter }?.key
            if (artistId != null) {
                conditions.add("$KEY_ARTIST_ID = ?")
                selectionArgs.add(artistId.toString())
            }
        }

        // 3. Genre Filter
        if (genreFilter.isNotBlank()) {
            conditions.add("$KEY_GENRE = ?")
            selectionArgs.add(genreFilter)
        }

        // Construct the WHERE clause
        if (conditions.isNotEmpty()) {
            query += " WHERE " + conditions.joinToString(" AND ")
        }

        // Execute Query
        val cursor = db.rawQuery(query, selectionArgs.toTypedArray())

        if (cursor.moveToFirst()) {
            do {
                val album = Album(
                    albumId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE)),
                    artistId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ARTIST_ID)),
                    year = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_YEAR)),
                    genre = cursor.getString(cursor.getColumnIndexOrThrow(KEY_GENRE)),
                    // Retrieve mock artist name for display
                    artistName = mockArtistNames[cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ARTIST_ID))]
                )
                albumList.add(album)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return albumList
    }

    /**
     * Gets a list of distinct values from a column (e.g., Genres or Mock Artist Names)
     */
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
}