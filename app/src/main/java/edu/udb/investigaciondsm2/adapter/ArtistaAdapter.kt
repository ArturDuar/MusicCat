// File: adapter/ArtistaAdapter.kt
package edu.udb.investigaciondsm2.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.udb.investigaciondsm2.databinding.ArtistItemBinding
import edu.udb.investigaciondsm2.model.Artista

class ArtistaAdapter(
    private var artistas: List<Artista>,
    private val onItemClick: (Artista) -> Unit
) : RecyclerView.Adapter<ArtistaAdapter.ArtistaViewHolder>() {

    class ArtistaViewHolder(private val binding: ArtistItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(artista: Artista, onItemClick: (Artista) -> Unit) {
            binding.tvArtistName.text = artista.name
            binding.tvArtistInfo.text = "${artista.genre} - ${artista.country}"
            binding.root.setOnClickListener { onItemClick(artista) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistaViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ArtistItemBinding.inflate(inflater, parent, false)
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

