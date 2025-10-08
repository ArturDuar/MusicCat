// File: ArtistsActivity.kt
package edu.udb.investigaciondsm2

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import edu.udb.investigaciondsm2.adapter.ArtistaAdapter
import edu.udb.investigaciondsm2.data.DatabaseHelper
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
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Artistas"

        dbHelper = DatabaseHelper(this)

        setupSpinners()
        setupRecyclerView()

        loadArtists()

        binding.btnBuscar.setOnClickListener {
            loadArtists(
                searchTerm = binding.searchBar.text.toString(),
                genreFilter = binding.spinnerGenre.selectedItem.toString().takeIf { it != "Todos" } ?: "",
                countryFilter = binding.spinnerCountry.selectedItem.toString().takeIf { it != "Todos" } ?: ""
            )
        }
    }

    private fun setupSpinners() {
        // ✅ CORRECCIÓN 1: Usar las funciones específicas
        val genres = listOf("Todos") + dbHelper.getDistinctArtistGenres()
        val countries = listOf("Todos") + dbHelper.getDistinctArtistCountries()

        ArrayAdapter(this, android.R.layout.simple_spinner_item, genres).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerGenre.adapter = adapter
        }

        ArrayAdapter(this, android.R.layout.simple_spinner_item, countries).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCountry.adapter = adapter
        }
    }

    private fun setupRecyclerView() {
        // ✅ CORRECCIÓN 2: Usar el nuevo constructor del adaptador
        artistaAdapter = ArtistaAdapter(emptyList()) { artista ->
            showArtistDetailsModal(artista)
        }
        binding.rvArtists.layoutManager = LinearLayoutManager(this)
        binding.rvArtists.adapter = artistaAdapter
    }

    private fun loadArtists(searchTerm: String = "", genreFilter: String = "", countryFilter: String = "") {
        // ✅ CORRECCIÓN 3: Esta llamada ahora funcionará
        val artistas = dbHelper.getAllArtists(searchTerm, genreFilter, countryFilter)
        artistaAdapter.updateList(artistas)
    }

    private fun showArtistDetailsModal(artista: Artista) {
        val dialogBinding = ArtistDetailsBinding.inflate(layoutInflater)
        dialogBinding.tvArtistName.text = artista.name
        dialogBinding.tvArtistGenre.text = "Género: ${artista.genre}"
        dialogBinding.tvArtistCountry.text = "País: ${artista.country}"
        dialogBinding.tvArtistDescription.text = artista.description

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
