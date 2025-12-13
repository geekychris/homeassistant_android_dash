package com.example.simplehomeassistant.ui.entityselection

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.simplehomeassistant.R
import com.example.simplehomeassistant.data.model.HAEntity

class EntitySelectionAdapter(
    private val onEntityToggle: (String) -> Unit
) : RecyclerView.Adapter<EntitySelectionAdapter.EntityViewHolder>() {

    private var entities = listOf<HAEntity>()
    private var selectedIds = setOf<String>()

    fun submitList(newEntities: List<HAEntity>, selected: Set<String>) {
        entities = newEntities
        selectedIds = selected
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selectable_entity, parent, false)
        return EntityViewHolder(view)
    }

    override fun onBindViewHolder(holder: EntityViewHolder, position: Int) {
        holder.bind(entities[position], entities[position].entityId in selectedIds)
    }

    override fun getItemCount() = entities.size

    inner class EntityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val checkbox: CheckBox = view.findViewById(R.id.entityCheckbox)
        private val nameText: TextView = view.findViewById(R.id.entityName)
        private val idText: TextView = view.findViewById(R.id.entityId)
        private val typeText: TextView = view.findViewById(R.id.entityType)

        fun bind(entity: HAEntity, isSelected: Boolean) {
            nameText.text = entity.name
            idText.text = entity.entityId
            typeText.text = entity.domain.uppercase()

            checkbox.setOnCheckedChangeListener(null)
            checkbox.isChecked = isSelected

            checkbox.setOnCheckedChangeListener { _, _ ->
                onEntityToggle(entity.entityId)
            }

            itemView.setOnClickListener {
                checkbox.isChecked = !checkbox.isChecked
            }
        }
    }
}
