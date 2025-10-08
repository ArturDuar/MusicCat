// File: SongsActivity.kt
package edu.udb.investigaciondsm2

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import edu.udb.investigaciondsm2.adapter.CancionAdapter
import edu.udb.investigaciondsm2.data.DatabaseHelper
import edu.udb.investigaciondsm2.databinding.ActivitySongsBinding
import edu.udb.investigaciondsm2.databinding.SongDetailsBinding
import edu.udb.investigaciondsm2.model.Cancion

class SongsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySongsBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var cancionAdapter: CancionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySongsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Canciones"

        dbHelper = DatabaseHelper(this)

        setupSpinners()
        setupRecyclerView()

        // Carga inicial
        loadSongs()

        binding.btnBuscar.setOnClickListener {
            loadSongs(
                searchTerm = binding.searchBar.text.toString(),
                artistFilter = binding.spinnerArtist.selectedItem.toString().takeIf { it != "Todos" } ?: "",
                genreFilter = binding.spinnerGenre.selectedItem.toString().takeIf { it != "Todos" } ?: "",
                albumFilter = binding.spinnerAlbum.selectedItem.toString().takeIf { it != "Todos" } ?: ""
            )
        }
    }

    private fun setupSpinners() {
        val artists = listOf("Todos") + dbHelper.getDistinctSongValues("artist_id")
        val genres = listOf("Todos") + dbHelper.getDistinctSongValues("genre")
        val albums = listOf("Todos") + dbHelper.getDistinctSongValues("album_id")

        // Artist Spinner
        ArrayAdapter(this, android.R.layout.simple_spinner_item, artists).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerArtist.adapter = adapter
        }

        // Genre Spinner
        ArrayAdapter(this, android.R.layout.simple_spinner_item, genres).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerGenre.adapter = adapter
        }

        // Album Spinner
        ArrayAdapter(this, android.R.layout.simple_spinner_item, albums).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerAlbum.adapter = adapter
        }
    }

    private fun setupRecyclerView() {
        cancionAdapter = CancionAdapter(this, emptyList()) { cancion ->
            showSongDetailsModal(cancion)
        }
        binding.rvSongs.layoutManager = LinearLayoutManager(this)
        binding.rvSongs.adapter = cancionAdapter
    }

    private fun loadSongs(searchTerm: String = "", artistFilter: String = "", genreFilter: String = "", albumFilter: String = "") {
        val canciones = dbHelper.getAllSongs(searchTerm, artistFilter, genreFilter, albumFilter)
        cancionAdapter.updateList(canciones)
    }

    private fun showSongDetailsModal(cancion: Cancion) {
        val dialogBinding = SongDetailsBinding.inflate(layoutInflater)

        dialogBinding.tvDetailTitle.text = cancion.title
        dialogBinding.tvDetailArtist.text = "Artista: ${cancion.artistName ?: "Desconocido"}"
        dialogBinding.tvDetailAlbum.text = "Álbum: ${cancion.albumName ?: "Desconocido"}"
        dialogBinding.tvDetailGenre.text = "Género: ${cancion.genre}"
        dialogBinding.tvDetailDuration.text = "Duración: ${cancion.getFormattedDuration()}"

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