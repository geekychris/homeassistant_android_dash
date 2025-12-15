package com.example.simplehomeassistant.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.simplehomeassistant.R
import com.example.simplehomeassistant.data.model.HAEntity

class EntityAdapter(
    private val onEntityAction: (HAEntity, EntityAction) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var entities = listOf<HAEntity>()

    fun submitList(newEntities: List<HAEntity>) {
        val oldEntities = entities
        entities = newEntities

        // Only notify if the entity IDs changed (not just their states)
        // This prevents reordering when only states change
        if (oldEntities.map { it.entityId } != newEntities.map { it.entityId }) {
            notifyDataSetChanged()
        } else {
            // Just update the items without changing positions
            notifyItemRangeChanged(0, entities.size)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val entity = entities[position]
        return when (entity.domain) {
            "switch" -> VIEW_TYPE_SWITCH
            "light" -> VIEW_TYPE_LIGHT
            "climate" -> VIEW_TYPE_CLIMATE
            "sensor", "binary_sensor" -> VIEW_TYPE_SENSOR
            else -> VIEW_TYPE_GENERIC
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SWITCH -> SwitchViewHolder(
                inflater.inflate(R.layout.item_entity_switch, parent, false)
            )

            VIEW_TYPE_LIGHT -> LightViewHolder(
                inflater.inflate(R.layout.item_entity_light, parent, false)
            )

            VIEW_TYPE_CLIMATE -> ClimateViewHolder(
                inflater.inflate(R.layout.item_entity_climate, parent, false)
            )

            VIEW_TYPE_SENSOR -> SensorViewHolder(
                inflater.inflate(R.layout.item_entity_sensor, parent, false)
            )

            else -> GenericViewHolder(
                inflater.inflate(R.layout.item_entity_generic, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val entity = entities[position]
        when (holder) {
            is SwitchViewHolder -> holder.bind(entity, onEntityAction)
            is LightViewHolder -> holder.bind(entity, onEntityAction)
            is ClimateViewHolder -> holder.bind(entity, onEntityAction)
            is SensorViewHolder -> holder.bind(entity)
            is GenericViewHolder -> holder.bind(entity, onEntityAction)
        }
    }

    override fun getItemCount() = entities.size

    fun getEntityAt(position: Int): HAEntity? {
        return entities.getOrNull(position)
    }

    // ViewHolders
    class SwitchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameText: TextView = view.findViewById(R.id.entityName)
        private val stateText: TextView = view.findViewById(R.id.entityState)
        private val switchToggle: Switch = view.findViewById(R.id.entitySwitch)

        fun bind(entity: HAEntity, onAction: (HAEntity, EntityAction) -> Unit) {
            nameText.text = entity.name
            stateText.text = entity.state

            switchToggle.setOnCheckedChangeListener(null)
            switchToggle.isChecked = entity.state == "on"

            switchToggle.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    onAction(entity, EntityAction.TurnOn)
                } else {
                    onAction(entity, EntityAction.TurnOff)
                }
            }
        }
    }

    class LightViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameText: TextView = view.findViewById(R.id.entityName)
        private val stateText: TextView = view.findViewById(R.id.entityState)
        private val switchToggle: Switch = view.findViewById(R.id.entitySwitch)
        private val brightnessSeekBar: SeekBar = view.findViewById(R.id.brightnessSeekBar)
        private val brightnessLabel: TextView = view.findViewById(R.id.brightnessLabel)

        fun bind(entity: HAEntity, onAction: (HAEntity, EntityAction) -> Unit) {
            nameText.text = entity.name
            stateText.text = entity.state

            switchToggle.setOnCheckedChangeListener(null)
            switchToggle.isChecked = entity.state == "on"

            val brightness = entity.attributes.brightness ?: 0
            brightnessSeekBar.progress = (brightness * 100f / 255f).toInt()
            brightnessLabel.text = "Brightness: ${brightnessSeekBar.progress}%"

            brightnessSeekBar.visibility = if (entity.state == "on") View.VISIBLE else View.GONE
            brightnessLabel.visibility = if (entity.state == "on") View.VISIBLE else View.GONE

            switchToggle.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    onAction(entity, EntityAction.TurnOn)
                } else {
                    onAction(entity, EntityAction.TurnOff)
                }
            }

            brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    brightnessLabel.text = "Brightness: $progress%"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val brightness = ((seekBar?.progress ?: 0) * 255f / 100f).toInt()
                    onAction(entity, EntityAction.SetBrightness(brightness))
                }
            })
        }
    }

    class ClimateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameText: TextView = view.findViewById(R.id.entityName)
        private val currentTempText: TextView = view.findViewById(R.id.currentTemperature)
        private val targetTempText: TextView = view.findViewById(R.id.targetTemperature)
        private val modeSpinner: Spinner = view.findViewById(R.id.modeSpinner)
        private val tempUpButton: Button = view.findViewById(R.id.tempUpButton)
        private val tempDownButton: Button = view.findViewById(R.id.tempDownButton)

        fun bind(entity: HAEntity, onAction: (HAEntity, EntityAction) -> Unit) {
            nameText.text = entity.name

            val currentTemp = entity.attributes.currentTemperature
            val targetTemp = entity.attributes.temperature

            currentTempText.text = "Current: ${currentTemp ?: "--"}°"
            targetTempText.text = "Target: ${targetTemp ?: "--"}°"

            // Setup mode spinner
            val modes = entity.attributes.hvacModes ?: listOf("off", "heat", "cool", "auto")
            val spinnerAdapter =
                ArrayAdapter(itemView.context, android.R.layout.simple_spinner_item, modes)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            modeSpinner.adapter = spinnerAdapter

            val currentMode = entity.attributes.hvacMode ?: "off"
            val modeIndex = modes.indexOf(currentMode)
            if (modeIndex >= 0) {
                modeSpinner.setSelection(modeIndex)
            }

            modeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    v: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedMode = modes[position]
                    if (selectedMode != currentMode) {
                        onAction(entity, EntityAction.SetClimateMode(selectedMode))
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            tempUpButton.setOnClickListener {
                val newTemp = (targetTemp ?: 20f) + 0.5f
                onAction(entity, EntityAction.SetTemperature(newTemp))
            }

            tempDownButton.setOnClickListener {
                val newTemp = (targetTemp ?: 20f) - 0.5f
                onAction(entity, EntityAction.SetTemperature(newTemp))
            }

            // Make target temperature clickable for manual input
            targetTempText.setOnClickListener {
                showTemperatureInputDialog(entity, targetTemp ?: 20f, onAction)
            }
        }

        private fun showTemperatureInputDialog(
            entity: HAEntity,
            currentTemp: Float,
            onAction: (HAEntity, EntityAction) -> Unit
        ) {
            val context = itemView.context
            val editText = EditText(context).apply {
                inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL or
                        android.text.InputType.TYPE_NUMBER_FLAG_SIGNED
                setText(currentTemp.toString())
                hint = "Enter temperature"
                selectAll()
            }

            val dialog = AlertDialog.Builder(context)
                .setTitle("Set Temperature")
                .setMessage("Enter the target temperature:")
                .setView(editText)
                .setPositiveButton("Set") { _, _ ->
                    val input = editText.text.toString()
                    try {
                        val newTemp = input.toFloat()
                        // Validate reasonable temperature range (e.g., 5-40°C or 40-105°F)
                        if (newTemp in 5f..105f) {
                            onAction(entity, EntityAction.SetTemperature(newTemp))
                        } else {
                            Toast.makeText(
                                context,
                                "Temperature must be between 5 and 105",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: NumberFormatException) {
                        Toast.makeText(
                            context,
                            "Invalid temperature value",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .create()

            dialog.show()
            // Focus and show keyboard
            editText.requestFocus()
            val imm = context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                    as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(editText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }
    }

    class SensorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameText: TextView = view.findViewById(R.id.entityName)
        private val valueText: TextView = view.findViewById(R.id.entityValue)
        private val unitText: TextView = view.findViewById(R.id.entityUnit)

        fun bind(entity: HAEntity) {
            nameText.text = entity.name
            valueText.text = entity.state

            val unit = entity.attributes.unitOfMeasurement ?: ""
            unitText.text = unit
            unitText.visibility = if (unit.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }

    class GenericViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameText: TextView = view.findViewById(R.id.entityName)
        private val stateText: TextView = view.findViewById(R.id.entityState)
        private val actionButton: Button = view.findViewById(R.id.actionButton)

        fun bind(entity: HAEntity, onAction: (HAEntity, EntityAction) -> Unit) {
            nameText.text = entity.name
            stateText.text = entity.state

            if (entity.isControllable()) {
                actionButton.visibility = View.VISIBLE
                actionButton.text = "Toggle"
                actionButton.setOnClickListener {
                    onAction(entity, EntityAction.Toggle)
                }
            } else {
                actionButton.visibility = View.GONE
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_SWITCH = 0
        private const val VIEW_TYPE_LIGHT = 1
        private const val VIEW_TYPE_CLIMATE = 2
        private const val VIEW_TYPE_SENSOR = 3
        private const val VIEW_TYPE_GENERIC = 4
    }
}
