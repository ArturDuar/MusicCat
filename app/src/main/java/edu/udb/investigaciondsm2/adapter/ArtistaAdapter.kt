// File: ArtistaAdapter.kt
package edu.udb.investigaciondsm2.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.udb.investigaciondsm2.databinding.ArtistItemBinding
import edu.udb.investigaciondsm2.model.Artista

class ArtistaAdapter(
    private val context: Context,
    private var artistas: List<Artista>,
    private val onItemClick: (Artista) -> Unit
) : RecyclerView.Adapter<ArtistaAdapter.ArtistaViewHolder>() {

    class ArtistaViewHolder(private val binding: ArtistItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(artista: Artista, onItemClick: (Artista) -> Unit) {
            binding.tvArtistName.text = artista.name
            binding.tvArtistGenre.text = "Género: ${artista.genre}"
            binding.tvArtistCountry.text = "País: ${artista.country}"
            // Opcional: Mostrar una parte de la descripción
            binding.tvArtistDescription.text = artista.description.take(60) + if (artista.description.length > 60) "..." else ""

            binding.root.setOnClickListener {
                onItemClick(artista)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistaViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ArtistItemBinding.inflate(
            inflater,
            parent,
            false
        )
        return ArtistaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtistaViewHolder, position: Int) {
        holder.bind(artistas[position], onItemClick)
    }

    override fun getItemCount(): Int = artistas.size

    fun updateList(newArtistas: List<Artista>) {
        artistas = newArtistas
        notifyDataSetChanged()
    }
}