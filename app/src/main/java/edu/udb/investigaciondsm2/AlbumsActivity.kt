// File: AlbumsActivity.kt
package edu.udb.investigaciondsm2

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import edu.udb.investigaciondsm2.adapter.AlbumAdapter
import edu.udb.investigaciondsm2.data.DatabaseHelper
// RUTAS DE BINDING CORRECTAS: PAQUETE_BASE.databinding.NOMBRE_XML_CAMELCASE
import edu.udb.investigaciondsm2.databinding.ActivityAlbumsBinding
import edu.udb.investigaciondsm2.model.Album
import edu.udb.investigaciondsm2.databinding.AlbumDetailsBinding

class AlbumsActivity : AppCompatActivity() {

    // 1. Instancia de Binding para activity_albums.xml
    private lateinit var binding: ActivityAlbumsBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var albumAdapter: AlbumAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. Inicializar el binding e inflar la vista
        binding = ActivityAlbumsBinding.inflate(layoutInflater)
        setContentView(binding.root) // Usamos binding.root para obtener la vista raíz

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Álbumes"

        dbHelper = DatabaseHelper(this)

        setupSpinners()
        setupRecyclerView()

        // Carga inicial
        loadAlbums()

        // 3. Acceso simplificado a todas las vistas por su ID
        // El botón btn_buscar está dentro de filter_row, pero con un ID,
        // View Binding lo expone directamente en la clase ActivityAlbumsBinding.
        binding.btnBuscar.setOnClickListener { // Acceso directo al Button con id="btn_buscar"
            loadAlbums(
                searchTerm = binding.searchBar.text.toString(),
                artistFilter = binding.spinnerArtist.selectedItem.toString().takeIf { it != "Todos" } ?: "",
                genreFilter = binding.spinnerGenre.selectedItem.toString().takeIf { it != "Todos" } ?: ""
            )
        }
    }

    private fun setupSpinners() {
        val artists = listOf("Todos") + dbHelper.getDistinctValues("artist_id")
        val genres = listOf("Todos") + dbHelper.getDistinctValues("genre")

        // Artist Spinner (Acceso directo a spinnerArtist)
        ArrayAdapter(this, android.R.layout.simple_spinner_item, artists).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerArtist.adapter = adapter
        }

        // Genre Spinner (Acceso directo a spinnerGenre)
        ArrayAdapter(this, android.R.layout.simple_spinner_item, genres).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerGenre.adapter = adapter
        }
    }

    private fun setupRecyclerView() {
        albumAdapter = AlbumAdapter(this, emptyList()) { album ->
            showAlbumDetailsModal(album)
        }
        // Acceso directo a rvAlbums
        binding.rvAlbums.layoutManager = LinearLayoutManager(this)
        binding.rvAlbums.adapter = albumAdapter
    }

    private fun loadAlbums(searchTerm: String = "", artistFilter: String = "", genreFilter: String = "") {
        val albums = dbHelper.getAllAlbums(searchTerm, artistFilter, genreFilter)
        albumAdapter.updateList(albums)
    }

    private fun showAlbumDetailsModal(album: Album) {
        // Usamos AlbumDetailsBinding para el layout de la modal
        val dialogBinding = AlbumDetailsBinding.inflate(layoutInflater)

        // Acceso directo a los TextViews de album_details.xml
        dialogBinding.tvAlbumTitle.text = album.title
        dialogBinding.tvAlbumArtist.text = "Artista: ${album.artistName ?: "Desconocido"}"
        dialogBinding.tvAlbumYear.text = "Año: ${album.year}"
        dialogBinding.tvAlbumGenre.text = "Género: ${album.genre}"

        // Corregido: AlertDialog.Builder usa .setView() y .setTitle()
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