// File: ArtistsActivity.kt
package edu.udb.investigaciondsm2

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import edu.udb.investigaciondsm2.adapter.ArtistaAdapter
import edu.udb.investigaciondsm2.data.DatabaseHelper
// RUTAS DE BINDING CORRECTAS: PAQUETE_BASE.databinding.NOMBRE_XML_CAMELCASE
import edu.udb.investigaciondsm2.databinding.ActivityArtistsBinding
import edu.udb.investigaciondsm2.model.Artista
import edu.udb.investigaciondsm2.databinding.ArtistDetailsBinding

class ArtistsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArtistsBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var artistaAdapter: ArtistaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityArtistsBinding.inflate(layoutInflater)
        setContentView(binding.root) // Usamos binding.root para obtener la vista raíz

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Artistas"

        dbHelper = DatabaseHelper(this)

        setupSpinners()
        setupRecyclerView()

        // Carga inicial
        loadArtists()

        binding.btnBuscar.setOnClickListener { // Acceso directo al Button con id="btn_buscar"
            loadArtists(
                searchTerm = binding.searchBar.text.toString(),
                genreFilter = binding.spinnerGenre.selectedItem.toString().takeIf { it != "Todos" } ?: "",
                countryFilter = binding.spinnerCountry.selectedItem.toString().takeIf { it != "Todos" } ?: ""
            )
        }
    }

    private fun setupSpinners() {
        val genres = listOf("Todos") + dbHelper.getDistinctArtistValues("genre")
        val countries = listOf("Todos") + dbHelper.getDistinctArtistValues("country")

        // Genre Spinner
        ArrayAdapter(this, android.R.layout.simple_spinner_item, genres).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerGenre.adapter = adapter
        }

        // Country Spinner
        ArrayAdapter(this, android.R.layout.simple_spinner_item, countries).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCountry.adapter = adapter
        }
    }

    private fun setupRecyclerView() {
        artistaAdapter = ArtistaAdapter(this, emptyList()) { artista ->
            showArtistDetailsModal(artista)
        }
        // Acceso directo a rvArtists
        binding.rvArtists.layoutManager = LinearLayoutManager(this)
        binding.rvArtists.adapter = artistaAdapter
    }

    private fun loadArtists(searchTerm: String = "", genreFilter: String = "", countryFilter: String = "") {
        val artistas = dbHelper.getAllArtists(searchTerm, genreFilter, countryFilter)
        artistaAdapter.updateList(artistas)
    }

    private fun showArtistDetailsModal(artista: Artista) {
        // Usamos ArtistDetailsBinding para el layout de la modal
        val dialogBinding = ArtistDetailsBinding.inflate(layoutInflater)

        // Acceso directo a los TextViews de artist_details.xml
        dialogBinding.tvArtistName.text = artista.name
        dialogBinding.tvArtistGenre.text = "Género: ${artista.genre}"
        dialogBinding.tvArtistCountry.text = "País: ${artista.country}"
        dialogBinding.tvArtistDescription.text = "Descripción: ${artista.description}"

        // Corregido: AlertDialog.Builder usa .setView() y .setTitle()
        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setTitle("Detalles del Artista")
            .setPositiveButton("Cerrar", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}