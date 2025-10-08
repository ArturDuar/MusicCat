package edu.udb.investigaciondsm2.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.udb.investigaciondsm2.databinding.SongItemBinding
import edu.udb.investigaciondsm2.model.Cancion

class CancionAdapter(
    private var canciones: List<Cancion>,
    private val onItemClick: (Cancion) -> Unit
) : RecyclerView.Adapter<CancionAdapter.CancionViewHolder>() {

    class CancionViewHolder(private val binding: SongItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cancion: Cancion, onItemClick: (Cancion) -> Unit) {
            binding.tvSongTitle.text = cancion.title
            // Mostramos el nombre del artista, que es más útil para el usuario
            binding.tvArtistName.text = cancion.artistName ?: "Artista desconocido"
            binding.tvSongDuration.text = formatDuration(cancion.duration)

            binding.root.setOnClickListener {
                onItemClick(cancion)
            }
        }

        private fun formatDuration(seconds: Int): String {
            val mins = seconds / 60
            val secs = seconds % 60
            return String.format("%02d:%02d", mins, secs)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CancionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SongItemBinding.inflate(inflater, parent, false)
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
