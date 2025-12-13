package com.example.simplehomeassistant.ui.tabs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.simplehomeassistant.R
import com.example.simplehomeassistant.data.model.Tab
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TabAdapter(
    private val onEditClick: (Tab) -> Unit,
    private val onDeleteClick: (Tab) -> Unit,
    private val onManageEntitiesClick: (Tab) -> Unit,
    private val getEntityCount: suspend (Long) -> Int
) : RecyclerView.Adapter<TabAdapter.TabViewHolder>() {

    private var tabs = listOf<Tab>()

    fun submitList(newTabs: List<Tab>) {
        tabs = newTabs
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tab, parent, false)
        return TabViewHolder(view)
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        holder.bind(tabs[position])
    }

    override fun getItemCount() = tabs.size

    inner class TabViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tabNameText: TextView = view.findViewById(R.id.tabNameText)
        private val entityCountText: TextView = view.findViewById(R.id.entityCountText)
        private val manageButton: MaterialButton = view.findViewById(R.id.manageEntitiesButton)
        private val editButton: ImageButton = view.findViewById(R.id.editButton)
        private val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)

        fun bind(tab: Tab) {
            tabNameText.text = tab.name

            // Load entity count asynchronously
            CoroutineScope(Dispatchers.Main).launch {
                val count = getEntityCount(tab.id)
                entityCountText.text = "$count device${if (count == 1) "" else "s"}"
            }

            manageButton.setOnClickListener {
                onManageEntitiesClick(tab)
            }

            editButton.setOnClickListener {
                onEditClick(tab)
            }

            deleteButton.setOnClickListener {
                onDeleteClick(tab)
            }
        }
    }
}
