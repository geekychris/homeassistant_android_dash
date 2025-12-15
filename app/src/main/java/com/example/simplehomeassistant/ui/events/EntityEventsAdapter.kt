package com.example.simplehomeassistant.ui.events

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.simplehomeassistant.R
import com.example.simplehomeassistant.data.model.EntityEvent

class EntityEventsAdapter : RecyclerView.Adapter<EntityEventsAdapter.EventViewHolder>() {

    private var events = listOf<EntityEvent>()

    fun submitList(newEvents: List<EntityEvent>) {
        events = newEvents
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_entity_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount() = events.size

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val entityName: TextView = view.findViewById(R.id.entityName)
        private val entityId: TextView = view.findViewById(R.id.entityId)
        private val eventState: TextView = view.findViewById(R.id.eventState)
        private val eventTime: TextView = view.findViewById(R.id.eventTime)

        fun bind(event: EntityEvent) {
            entityName.text = event.name
            entityId.text = event.entityId
            eventState.text = event.state.uppercase()
            eventTime.text = event.getFormattedTime()

            // Color code the state
            val stateColor = when (event.state.lowercase()) {
                "on", "open", "unlocked", "home" -> android.graphics.Color.parseColor("#4CAF50") // Green
                "off", "closed", "locked", "away" -> android.graphics.Color.parseColor("#9E9E9E") // Gray
                "unavailable", "unknown" -> android.graphics.Color.parseColor("#F44336") // Red
                else -> android.graphics.Color.parseColor("#2196F3") // Blue
            }
            eventState.setTextColor(stateColor)
        }
    }
}
