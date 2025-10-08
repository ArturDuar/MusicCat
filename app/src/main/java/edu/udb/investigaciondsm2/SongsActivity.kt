package edu.udb.investigaciondsm2

import android.os.Bundle
import android.widget.ArrayAdapter
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

    // 1. Declaraciones
    private lateinit var binding: ActivitySongsBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var songAdapter: SongAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar View Binding
        binding = ActivitySongsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración de la ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Canciones"

        // Inicializar Database Helper
        dbHelper = DatabaseHelper(this)

        // Inicializar componentes de la interfaz
        setupSpinners()
        setupRecyclerView()

        // e: ...:44:17 Unresolved reference 'btnBuscar'. -> CORREGIDO
        binding.btnBuscar.setOnClickListener {
            val searchTerm = binding.searchBar.text.toString()
            val artistFilter = binding.spinnerArtist.selectedItem.toString().takeIf { it != "Artista" } ?: ""
            val albumFilter = binding.spinnerAlbum.selectedItem.toString().takeIf { it != "Álbum" } ?: ""
            loadSongs(searchTerm, artistFilter, albumFilter)
        }

        // Carga inicial
        loadSongs()
    }

    private fun setupSpinners() {
        // e: ...:51:32 Unresolved reference 'getDistinctArtistNames'. -> CORREGIDO CON LA FUNCIÓN A IMPLEMENTAR
        val artists = dbHelper.getDistinctArtistNames().toMutableList()
        artists.add(0, "Artista") // Opción predeterminada

        // e: ...:52:31 Unresolved reference 'getDistinctAlbumTitles'. -> CORREGIDO CON LA FUNCIÓN A IMPLEMENTAR
        val albums = dbHelper.getDistinctAlbumTitles().toMutableList()
        albums.add(0, "Álbum") // Opción predeterminada

        // Adapter para Artistas
        val artistAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            artists
        )
        artistAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerArtist.adapter = artistAdapter

        // Adapter para Álbumes
        val albumAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            albums
        )
        albumAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAlbum.adapter = albumAdapter
    }

    private fun setupRecyclerView() {
        // Inicializa el adaptador con una lista vacía y un listener
        songAdapter = SongAdapter(emptyList()) { song ->
            showSongDetailsModal(song)
        }

        // e: ...:78:17 Unresolved reference 'rvSongs'. -> CORREGIDO
        binding.rvSongs.layoutManager = LinearLayoutManager(this)
        // e: ...:79:17 Unresolved reference 'rvSongs'. -> CORREGIDO
        binding.rvSongs.adapter = songAdapter
    }

    private fun loadSongs(searchTerm: String = "", artistNameFilter: String = "", albumTitleFilter: String = "") {
        // e: ...:97:41 Unresolved reference 'getAllSongsWithDetails'. -> CORREGIDO CON LA FUNCIÓN A IMPLEMENTAR
        // Obtener la lista de canciones con detalles del DB Helper
        val songsWithDetails: List<SongWithDetails> = dbHelper.getAllSongsWithDetails(searchTerm, artistNameFilter, albumTitleFilter)

        // Actualizar el RecyclerView
        songAdapter.updateList(songsWithDetails)
    }

    // Modal de Detalles
    private fun showSongDetailsModal(song: Cancion) {
        // e: ...:106:32 Unresolved reference 'getSongDetails'. -> CORREGIDO CON LA FUNCIÓN A IMPLEMENTAR
        // Obtenemos los detalles adicionales de la base de datos
        val details = dbHelper.getSongDetails(song.songId)

        // Usamos View Binding para el layout de la modal (song_details.xml)
        val dialogBinding = SongDetailsBinding.inflate(layoutInflater)

        // Rellenar los datos en el layout de la modal
        dialogBinding.tvDetailTitle.text = song.title

        // Usamos los detalles obtenidos del JOIN
        dialogBinding.tvDetailArtist.text = "Artista: ${details?.artistName ?: "Desconocido"}"
        dialogBinding.tvDetailAlbum.text = "Álbum: ${details?.albumTitle ?: "Desconocido"}"

        dialogBinding.tvDetailGenre.text = "Género: ${song.genre}"

        // Formatear duración (asumiendo que duration está en segundos)
        val minutes = song.duration / 60
        val seconds = song.duration % 60
        dialogBinding.tvDetailDuration.text = "Duración: ${String.format("%d:%02d", minutes, seconds)}"


        // Mostrar el modal
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