package com.amanpathak.digipointsdk

/**
 * Represents a city with its DIGIPOINT location.
 */
data class City(
    val name: String,
    val state: String,
    val coordinate: DigipinCoordinate
)

/**
 * Represents a landmark with its DIGIPOINT location.
 */
data class Landmark(
    val name: String,
    val type: String,
    val coordinate: DigipinCoordinate
) 