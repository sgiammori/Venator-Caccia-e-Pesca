package it.mygroup.org.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ActivityType {
    HUNTING, FISHING
}

@Entity(tableName = "prey_items")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val preyName: String,         // Name of the animal/fish (e.g., "Wild Boar", "Trout")
    val activityType: ActivityType, // HUNTING or FISHING
    val quantity: Int = 1,        // Number of items caught
    val weight: Double = 0.0,     // Total weight in Kg (optional)
    val location: String = "",    // Hunting zone or fishing spot
    val day: String,
    val month: String,
    val year: String,
    val notes: String = "",       // Extra details (e.g., "Used a 12 gauge", "Sunny day")
    val timestamp: Long = System.currentTimeMillis()
)
