package edu.udb.investigaciondsm2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    lateinit var btnAlbums: MaterialCardView
    lateinit var btnSongs: MaterialCardView
    lateinit var btnArtists: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
        menu()
    }

    fun menu(){
        btnAlbums = findViewById(R.id.cardAlbums)
        btnSongs = findViewById(R.id.cardSongs)
        btnArtists = findViewById(R.id.cardArtists)

        btnAlbums.setOnClickListener {
            val intent = Intent(this, AlbumsActivity::class.java)
            startActivity(intent)
        }

        btnSongs.setOnClickListener {
            val intent = Intent(this, SongsActivity::class.java)
            startActivity(intent)
        }

        btnArtists.setOnClickListener {
            val intent = Intent(this, ArtistsActivity::class.java)
            startActivity(intent)
        }


    }
}