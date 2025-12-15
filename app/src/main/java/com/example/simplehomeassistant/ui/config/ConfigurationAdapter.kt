package com.example.simplehomeassistant.ui.config

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.simplehomeassistant.R
import com.example.simplehomeassistant.data.model.Configuration
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class ConfigurationAdapter(
    private val onActivate: (Configuration) -> Unit,
    private val onDelete: (Configuration) -> Unit,
    private val onEdit: (Configuration) -> Unit,
    private val onUrlPreferenceChange: (Configuration, Boolean) -> Unit
) : RecyclerView.Adapter<ConfigurationAdapter.ConfigurationViewHolder>() {

    private var configurations = listOf<Configuration>()
    private var activeConfigId: Long? = null

    fun submitList(newConfigurations: List<Configuration>, activeId: Long?) {
        configurations = newConfigurations
        activeConfigId = activeId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConfigurationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_configuration, parent, false)
        return ConfigurationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConfigurationViewHolder, position: Int) {
        holder.bind(configurations[position], configurations[position].id == activeConfigId)
    }

    override fun getItemCount() = configurations.size

    inner class ConfigurationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameText: TextView = view.findViewById(R.id.configName)
        private val internalUrlText: TextView = view.findViewById(R.id.configInternalUrl)
        private val externalUrlText: TextView = view.findViewById(R.id.configExternalUrl)
        private val activeChip: Chip = view.findViewById(R.id.activeChip)
        private val activateButton: MaterialButton = view.findViewById(R.id.activateButton)
        private val editButton: MaterialButton = view.findViewById(R.id.editButton)
        private val deleteButton: MaterialButton = view.findViewById(R.id.deleteButton)
        private val urlModeLabel: TextView = view.findViewById(R.id.urlModeLabel)
        private val urlModeSwitch: com.google.android.material.switchmaterial.SwitchMaterial =
            view.findViewById(R.id.urlModeSwitch)

        fun bind(configuration: Configuration, isActive: Boolean) {
            nameText.text = configuration.name
            internalUrlText.text = "Internal: ${configuration.internalUrl}"
            externalUrlText.text = "External: ${configuration.externalUrl}"

            activeChip.visibility = if (isActive) View.VISIBLE else View.GONE
            activateButton.visibility = if (!isActive) View.VISIBLE else View.GONE

            // Show URL mode switch only for active configuration
            urlModeLabel.visibility = if (isActive) View.VISIBLE else View.GONE
            urlModeSwitch.visibility = if (isActive) View.VISIBLE else View.GONE

            // Set switch state based on configuration preference
            urlModeSwitch.isChecked = configuration.preferInternalUrl
            urlModeSwitch.text = if (configuration.preferInternalUrl) "Internal" else "External"

            // Handle switch changes
            urlModeSwitch.setOnCheckedChangeListener { _, isChecked ->
                urlModeSwitch.text = if (isChecked) "Internal" else "External"
                if (isChecked != configuration.preferInternalUrl) {
                    onUrlPreferenceChange(configuration, isChecked)
                }
            }

            activateButton.setOnClickListener {
                onActivate(configuration)
            }

            editButton.setOnClickListener {
                onEdit(configuration)
            }

            deleteButton.setOnClickListener {
                onDelete(configuration)
            }
        }
    }
}
