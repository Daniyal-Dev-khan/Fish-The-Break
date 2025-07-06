package com.cp.fishthebreak.models.trolling

import androidx.room.Embedded
import androidx.room.Relation

data class TrillingWithPoints (
    @Embedded val trolling: Trolling,
    @Relation(
        parentColumn = "id",
        entityColumn = "trollingId"
    )
    val points: List<TrollingPoint> // <-- This is a one-to-many relationship, since each trolling has many points, hence returning a List here
)