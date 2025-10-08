// File: SongAdapter.kt
package edu.udb.investigaciondsm2.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.udb.investigaciondsm2.databinding.SongItemBinding
import edu.udb.investigaciondsm2.model.SongWithDetails
import edu.udb.investigaciondsm2.model.Cancion

class SongAdapter(
    private var songs: List<SongWithDetails>,
    private val onItemClick: (Cancion) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    class SongViewHolder(private val binding: SongItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(songDetails: SongWithDetails, onItemClick: (Cancion) -> Unit) {
            val song = songDetails.song

            // Asigna los datos a las vistas del ítem
            binding.tvSongTitle.text = song.title

            // Combina Artista y Álbum usando los detalles
            binding.tvSongArtistAlbum.text = "${songDetails.artistName} - ${songDetails.albumTitle}"

            binding.tvSongGenre.text = "Género: ${song.genre}"

            // Formatea la duración (asumiendo que 'duration' está en segundos)
            val minutes = song.duration / 60
            val seconds = song.duration % 60
            binding.tvSongDuration.text = String.format("%d:%02d", minutes, seconds)

            // Configura el clic en la tarjeta
            binding.root.setOnClickListener {
                onItemClick(song)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        // Usando View Binding para el layout song_item.xml
        val binding = SongItemBinding.inflate(inflater, parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position], onItemClick)
    }

    override fun getItemCount(): Int = songs.size

    fun updateList(newSongs: List<SongWithDetails>) {
        songs = newSongs
        notifyDataSetChanged()
    }
}