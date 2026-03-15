package com.example.travelapp.data.model

enum class TripStatus(val label: String) {
    DRAFT("Bozza"),
    IN_PROGRESS("In corso"),
    COMPLETED("Completato")
}

fun computeTripStatus(endDate: Long?): TripStatus {
    if (endDate == null) return TripStatus.IN_PROGRESS
    return if (endDate <= System.currentTimeMillis()) TripStatus.COMPLETED else TripStatus.IN_PROGRESS
}
