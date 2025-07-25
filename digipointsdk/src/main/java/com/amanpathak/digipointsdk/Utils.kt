package com.amanpathak.digipointsdk

import kotlin.math.*

internal object Utils {
    private const val EARTH_RADIUS_METERS = 6371000.0
    
    fun calculateDistance(coord1: DigipointCoordinate, coord2: DigipointCoordinate): Double {
        val lat1Rad = Math.toRadians(coord1.latitude)
        val lat2Rad = Math.toRadians(coord2.latitude)
        val deltaLatRad = Math.toRadians(coord2.latitude - coord1.latitude)
        val deltaLonRad = Math.toRadians(coord2.longitude - coord1.longitude)
        
        val a = sin(deltaLatRad / 2).pow(2) + 
                cos(lat1Rad) * cos(lat2Rad) * sin(deltaLonRad / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return EARTH_RADIUS_METERS * c
    }
    
    fun calculateGridSizeMeters(digipinCode: DigipointCode): Double {
        val boundingBox = digipinCode.boundingBox
        val latDistance = calculateDistance(
            DigipointCoordinate(boundingBox.southwest.latitude, boundingBox.center().longitude),
            DigipointCoordinate(boundingBox.northeast.latitude, boundingBox.center().longitude)
        )
        val lonDistance = calculateDistance(
            DigipointCoordinate(boundingBox.center().latitude, boundingBox.southwest.longitude),
            DigipointCoordinate(boundingBox.center().latitude, boundingBox.northeast.longitude)
        )
        
        return (latDistance + lonDistance) / 2
    }
    
    fun createMapsUrl(digipinCode: DigipointCode): String {
        val coord = digipinCode.centerCoordinate
        return "https://www.google.com/maps?q=${coord.latitude},${coord.longitude}"
    }
    
    fun getPrecisionDescription(digipinCode: DigipointCode): String {
        val gridSizeMeters = calculateGridSizeMeters(digipinCode)
        
        return when {
            gridSizeMeters < 5 -> "Building level precision (~${gridSizeMeters.format(1)}m)"
            gridSizeMeters < 50 -> "Street level precision (~${gridSizeMeters.format(0)}m)"
            gridSizeMeters < 500 -> "Neighborhood level precision (~${gridSizeMeters.format(0)}m)"
            gridSizeMeters < 5000 -> "District level precision (~${(gridSizeMeters/1000).format(1)}km)"
            else -> "Regional level precision (~${(gridSizeMeters/1000).format(0)}km)"
        }
    }
    
    fun calculateAreaSquareMeters(digipinCode: DigipointCode): Double {
        val boundingBox = digipinCode.boundingBox
        
        val latDistance = calculateDistance(
            DigipointCoordinate(boundingBox.southwest.latitude, boundingBox.center().longitude),
            DigipointCoordinate(boundingBox.northeast.latitude, boundingBox.center().longitude)
        )
        
        val lonDistance = calculateDistance(
            DigipointCoordinate(boundingBox.center().latitude, boundingBox.southwest.longitude),
            DigipointCoordinate(boundingBox.center().latitude, boundingBox.northeast.longitude)
        )
        
        return latDistance * lonDistance
    }
    
    private fun Double.format(decimals: Int): String {
        return "%.${decimals}f".format(this)
    }
} 