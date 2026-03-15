package com.example.travelapp.data.model

enum class TripStatus(val label: String) {
    DRAFT("Bozza"),
    PLANNED("Pianificato"),
    IN_PROGRESS("In corso"),
    COMPLETED("Completato")
}

fun computeTripStatus(startDate: Long, endDate: Long?): TripStatus {
    val now = System.currentTimeMillis()
    if (startDate > now) return TripStatus.PLANNED
    if (endDate == null || endDate > now) return TripStatus.IN_PROGRESS
    return TripStatus.COMPLETED
}
