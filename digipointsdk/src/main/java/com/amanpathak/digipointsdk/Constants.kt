package com.amanpathak.digipointsdk

internal object Constants {
    const val VERSION = "1.0.0"
    const val GRID_SIZE_METERS = 4.0
    const val DIGIPOINT_CODE_LENGTH = 10
    
    // DIGIPOINT grid layout
    val DIGIPOINT_GRID = arrayOf(
        arrayOf('F', 'C', '9', '8'),
        arrayOf('J', '3', '2', '7'),
        arrayOf('K', '4', '5', '6'),
        arrayOf('L', 'M', 'P', 'T')
    )
    
    // flattened symbols for indexing
    val SYMBOLS = DIGIPOINT_GRID.flatMap { it.toList() }
    const val DIGIPOINT_PATTERN = "[FC98J327K456LMPT]{10}"
    
    // Indian boundaries
    const val INDIA_MIN_LAT = 2.5
    const val INDIA_MAX_LAT = 38.5
    const val INDIA_MIN_LON = 63.5
    const val INDIA_MAX_LON = 99.5
    
    object ErrorMessages {
        const val INVALID_DIGIPOINT_LENGTH = "DigiPin must be exactly 10 characters"
        const val INVALID_DIGIPOINT_CHARACTERS = "DigiPin contains invalid characters"
        const val INVALID_LATITUDE = "Latitude must be between -90 and 90"
        const val INVALID_LONGITUDE = "Longitude must be between -180 and 180"
        const val COORDINATE_OUT_OF_BOUNDS = "Coordinate is outside Indian geographical bounds"
        const val NEGATIVE_RADIUS = "Radius must be positive"
        const val EMPTY_COORDINATE_LIST = "Coordinate list cannot be empty"
    }
    
    object IndianCities {
        data class CityInfo(
            val name: String,
            val state: String,
            val coordinate: DigipinCoordinate
        )
        
        val ALL_CITIES = listOf(
            CityInfo("Delhi", "Delhi", DigipinCoordinate(28.6139, 77.2090)),
            CityInfo("Mumbai", "Maharashtra", DigipinCoordinate(19.0760, 72.8777)),
            CityInfo("Bangalore", "Karnataka", DigipinCoordinate(12.9716, 77.5946)),
            CityInfo("Chennai", "Tamil Nadu", DigipinCoordinate(13.0827, 80.2707))
        )
        
        fun findCityByName(name: String): CityInfo? {
            return ALL_CITIES.find { it.name.equals(name, ignoreCase = true) }
        }
    }
    
    object Landmarks {
        data class LandmarkInfo(
            val name: String,
            val type: String,
            val coordinate: DigipinCoordinate
        )
        
        val ALL_LANDMARKS = listOf(
            LandmarkInfo("Taj Mahal", "Monument", DigipinCoordinate(27.1751, 78.0421)),
            LandmarkInfo("Gateway of India", "Monument", DigipinCoordinate(18.9217, 72.8347)),
            LandmarkInfo("India Gate", "Monument", DigipinCoordinate(28.6129, 77.2295)),
            LandmarkInfo("Qutub Minar", "Monument", DigipinCoordinate(28.5245, 77.1855))
        )
    }
} 
