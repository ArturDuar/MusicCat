// File: AlbumAdapter.kt
package edu.udb.investigaciondsm2.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.udb.investigaciondsm2.databinding.AlbumItemBinding
import edu.udb.investigaciondsm2.model.Album

class AlbumAdapter(
    private val context: Context, // Ya tienes el contexto, lo usaremos para temas
    private var albums: List<Album>,
    private val onItemClick: (Album) -> Unit
) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    class AlbumViewHolder(private val binding: AlbumItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(album: Album, onItemClick: (Album) -> Unit) {
            binding.tvAlbumTitle.text = album.title
            binding.tvAlbumArtist.text = "Artista: ${album.artistName ?: "Desconocido"}"
            binding.tvAlbumYear.text = "Año: ${album.year}"
            binding.tvAlbumGenre.text = "Género: ${album.genre}"

            binding.root.setOnClickListener {
                onItemClick(album)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        // CORRECCIÓN: Usamos el contexto del 'parent' (el RecyclerView), que ya hereda el tema
        // Esto asegura que la MaterialCardView pueda resolver sus atributos de tema.
        val inflater = LayoutInflater.from(parent.context)

        val binding = AlbumItemBinding.inflate(
            inflater,
            parent,
            false
        )
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(albums[position], onItemClick)
    }

    override fun getItemCount(): Int = albums.size

    fun updateList(newAlbums: List<Album>) {
        albums = newAlbums
        notifyDataSetChanged()
    }
}