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

        setupRecyclerView()
        setupSpinners()
        loadSongs()

        binding.btnBuscar.setOnClickListener {
            loadSongs(
                searchTerm = binding.searchBar.text.toString(),
                genreFilter = binding.spinnerGenre.selectedItem.toString(),
                artistFilter = binding.spinnerArtist.selectedItem.toString()
            )
        }
    }

    private fun setupSpinners() {
        val genres = listOf("Todos") + dbHelper.getDistinctSongGenres()
        val artists = listOf("Todos") + dbHelper.getDistinctArtistNames()

        ArrayAdapter(this, android.R.layout.simple_spinner_item, genres).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerGenre.adapter = adapter
        }

        ArrayAdapter(this, android.R.layout.simple_spinner_item, artists).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerArtist.adapter = adapter
        }
    }

    private fun setupRecyclerView() {
        cancionAdapter = CancionAdapter(emptyList()) { cancion ->
            showSongDetailsModal(cancion)
        }
        binding.rvSongs.layoutManager = LinearLayoutManager(this)
        binding.rvSongs.adapter = cancionAdapter
    }

    private fun loadSongs(searchTerm: String = "", genreFilter: String = "", artistFilter: String = "") {
        val canciones = dbHelper.getAllSongs(searchTerm, genreFilter, artistFilter)
        cancionAdapter.updateList(canciones)
    }

    private fun showSongDetailsModal(cancion: Cancion) {
        val dialogBinding = SongDetailsBinding.inflate(layoutInflater)

        dialogBinding.tvSongTitle.text = cancion.title
        dialogBinding.tvArtistName.text = "Artista: ${cancion.artistName}"
        dialogBinding.tvAlbumTitle.text = "Álbum: ${cancion.albumTitle}"
        dialogBinding.tvSongGenre.text = "Género: ${cancion.genre}"
        dialogBinding.tvSongDuration.text = "Duración: ${formatDuration(cancion.duration)}"

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setTitle("Detalles de la Canción")
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun formatDuration(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", mins, secs)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
