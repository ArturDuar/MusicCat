// File: CancionAdapter.kt
package edu.udb.investigaciondsm2.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.udb.investigaciondsm2.databinding.SongItemBinding
import edu.udb.investigaciondsm2.model.Cancion

class CancionAdapter(
    private val context: Context,
    private var canciones: List<Cancion>,
    private val onItemClick: (Cancion) -> Unit
) : RecyclerView.Adapter<CancionAdapter.CancionViewHolder>() {

    class CancionViewHolder(private val binding: SongItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cancion: Cancion, onItemClick: (Cancion) -> Unit) {
            binding.tvSongTitle.text = cancion.title
            binding.tvSongArtistAlbum.text = "${cancion.artistName ?: "Desconocido"} - ${cancion.albumName ?: "Desconocido"}"
            binding.tvSongGenre.text = "Género: ${cancion.genre}"
            binding.tvSongDuration.text = cancion.getFormattedDuration()

            binding.root.setOnClickListener {
                onItemClick(cancion)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CancionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SongItemBinding.inflate(
            inflater,
            parent,
            false
        )
        return CancionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CancionViewHolder, position: Int) {
        holder.bind(canciones[position], onItemClick)
    }

    override fun getItemCount(): Int = canciones.size

    fun updateList(newCanciones: List<Cancion>) {
        canciones = newCanciones
        notifyDataSetChanged()
    }
}