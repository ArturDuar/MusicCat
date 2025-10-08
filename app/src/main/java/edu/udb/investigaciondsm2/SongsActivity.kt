package edu.udb.investigaciondsm2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import edu.udb.investigaciondsm2.adapter.SongAdapter
import edu.udb.investigaciondsm2.data.DatabaseHelper
import edu.udb.investigaciondsm2.databinding.ActivitySongsBinding
import edu.udb.investigaciondsm2.databinding.SongDetailsBinding
import edu.udb.investigaciondsm2.model.Cancion
import edu.udb.investigaciondsm2.model.SongWithDetails
import edu.udb.investigaciondsm2.model.SongDetailsData

class SongsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySongsBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var songAdapter: SongAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySongsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Canciones"

        dbHelper = DatabaseHelper(this)

        setupSpinners()
        setupRecyclerView()

        loadSongs()

        // Configuración de la búsqueda
        binding.btnBuscar.setOnClickListener {
            val searchTerm = binding.searchBar.text.toString()
            val artistFilter = binding.spinnerArtist.selectedItem.toString().takeIf { it != "Todos" } ?: ""
            val albumFilter = binding.spinnerAlbum.selectedItem.toString().takeIf { it != "Todos" } ?: ""
            loadSongs(searchTerm, artistFilter, albumFilter)
        }
    }

    private fun setupSpinners() {
        val allArtists = dbHelper.getDistinctArtistNames().toMutableList()
        allArtists.add(0, "Todos")
        val artistAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, allArtists)
        binding.spinnerArtist.adapter = artistAdapter

        val allAlbums = dbHelper.getDistinctAlbumTitles().toMutableList()
        allAlbums.add(0, "Todos")
        val albumAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, allAlbums)
        binding.spinnerAlbum.adapter = albumAdapter
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(emptyList()) { song ->
            showSongDetailsModal(song)
        }
        binding.rvSongs.layoutManager = LinearLayoutManager(this)
        binding.rvSongs.adapter = songAdapter
    }

    private fun loadSongs(searchTerm: String = "", artistNameFilter: String = "", albumTitleFilter: String = "") {
        val songsWithDetails = dbHelper.getAllSongsWithDetails(searchTerm, artistNameFilter, albumTitleFilter)
        songAdapter.updateList(songsWithDetails)
    }

    /**
     * **NUEVA FUNCIÓN:** Convierte segundos a formato "M:SS".
     */
    private fun formatDuration(durationSeconds: Int): String {
        val minutes = durationSeconds / 60
        val seconds = durationSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun showSongDetailsModal(song: Cancion) {
        val details = dbHelper.getSongDetails(song.songId)
        val dialogBinding = SongDetailsBinding.inflate(layoutInflater)

        // Rellenar los datos en el layout de la modal
        dialogBinding.tvDetailTitle.text = song.title
        dialogBinding.tvDetailArtist.text = "Artista: ${details?.artistName ?: "Desconocido"}"
        dialogBinding.tvDetailAlbum.text = "Álbum: ${details?.albumTitle ?: "Desconocido"}"
        dialogBinding.tvDetailGenre.text = "Género: ${song.genre}"

        // **LÍNEA CORREGIDA (Cerca de la Línea 125):** // Se utiliza la referencia 'tv_detail_duration' y se le asigna un String (resuelve los 3 errores).
        dialogBinding.tvDetailDuration.text = "Duración: ${formatDuration(song.duration)}"

        val playButton = dialogBinding.btnPlaySong
        playButton.text = "REPRODUCIR URL"

        playButton.setOnClickListener {
            val url = song.url
            if (url.startsWith("http://") || url.startsWith("https://")) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "No se pudo abrir el enlace: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "El enlace de reproducción no está disponible.", Toast.LENGTH_SHORT).show()
            }
        }

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setTitle("Detalles de la Canción")
            .setPositiveButton("Cerrar", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}