// File: adapter/AlbumAdapter.kt
package edu.udb.investigaciondsm2.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.udb.investigaciondsm2.databinding.AlbumItemBinding // Asumiendo que tienes un album_item.xml
import edu.udb.investigaciondsm2.model.Album

class AlbumAdapter(
    // ✅ CORRECCIÓN 3: Ajustar el constructor
    private var albums: List<Album>,
    private val onItemClick: (Album) -> Unit
) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    class AlbumViewHolder(private val binding: AlbumItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(album: Album, onItemClick: (Album) -> Unit) {
            binding.tvAlbumTitle.text = album.title
            binding.tvAlbumArtist.text = album.artistName ?: "Artista Desconocido"
            binding.tvAlbumYear.text = album.year.toString()

            binding.root.setOnClickListener {
                onItemClick(album)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        // Asegúrate de que tu layout se llame album_item.xml
        val binding = AlbumItemBinding.inflate(inflater, parent, false)
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
