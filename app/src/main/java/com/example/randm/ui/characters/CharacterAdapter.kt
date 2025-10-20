package com.example.randm.ui.characters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.randm.data.models.Character as ModelCharacter
import com.example.randm.R

class CharacterAdapter(
    private val onItemClick: (ModelCharacter) -> Unit
) : ListAdapter<ModelCharacter, CharacterAdapter.CharacterViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_character, parent, false)
        return CharacterViewHolder(view)
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CharacterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivAvatar: ImageView = itemView.findViewById(R.id.ivAvatar)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvStatusSpecies: TextView = itemView.findViewById(R.id.tvStatusSpecies)
        private val tvGender: TextView = itemView.findViewById(R.id.tvGender)

        fun bind(character: ModelCharacter) {
            Glide.with(itemView)
                .load(character.image)
                .circleCrop()
                .into(ivAvatar)

            tvName.text = character.name
            tvStatusSpecies.text = "${character.status} - ${character.species}"
            tvGender.text = character.gender

            itemView.setOnClickListener {
                onItemClick(character)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ModelCharacter>() {
            override fun areItemsTheSame(oldItem: ModelCharacter, newItem: ModelCharacter): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ModelCharacter, newItem: ModelCharacter): Boolean {
                return oldItem == newItem
            }
        }
    }
}