// File: AlbumsActivity.kt
package edu.udb.investigaciondsm2

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import edu.udb.investigaciondsm2.adapter.AlbumAdapter
import edu.udb.investigaciondsm2.data.DatabaseHelper
import edu.udb.investigaciondsm2.databinding.ActivityAlbumsBinding
import edu.udb.investigaciondsm2.model.Album
import edu.udb.investigaciondsm2.databinding.AlbumDetailsBinding

class AlbumsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlbumsBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var albumAdapter: AlbumAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlbumsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Álbumes"

        dbHelper = DatabaseHelper(this)

        setupSpinners()
        setupRecyclerView()

        loadAlbums()

        binding.btnBuscar.setOnClickListener {
            loadAlbums(
                searchTerm = binding.searchBar.text.toString(),
                artistFilter = binding.spinnerArtist.selectedItem.toString().takeIf { it != "Todos" } ?: "",
                genreFilter = binding.spinnerGenre.selectedItem.toString().takeIf { it != "Todos" } ?: ""
            )
        }
    }

    private fun setupSpinners() {
        val artists = listOf("Todos") + dbHelper.getDistinctArtistNames()

        // ✅ CORRECCIÓN 1: Llamar a la función correcta
        val genres = listOf("Todos") + dbHelper.getDistinctAlbumGenres()

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
    }

    private fun setupRecyclerView() {
        // ✅ CORRECCIÓN 2: El adaptador solo necesita la lista y el callback
        albumAdapter = AlbumAdapter(emptyList()) { album ->
            showAlbumDetailsModal(album)
        }
        binding.rvAlbums.layoutManager = LinearLayoutManager(this)
        binding.rvAlbums.adapter = albumAdapter
    }

    private fun loadAlbums(searchTerm: String = "", artistFilter: String = "", genreFilter: String = "") {
        val albums = dbHelper.getAllAlbums(searchTerm, artistFilter, genreFilter)
        albumAdapter.updateList(albums)
    }

    private fun showAlbumDetailsModal(album: Album) {
        val dialogBinding = AlbumDetailsBinding.inflate(layoutInflater)

        dialogBinding.tvAlbumTitle.text = album.title
        dialogBinding.tvAlbumArtist.text = "Artista: ${album.artistName ?: "Desconocido"}"
        dialogBinding.tvAlbumYear.text = "Año: ${album.year}"
        dialogBinding.tvAlbumGenre.text = "Género: ${album.genre}"

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setTitle("Detalles del Álbum")
            .setPositiveButton("Cerrar", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
