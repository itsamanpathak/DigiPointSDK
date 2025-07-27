package com.amanpathak.digipointsdk

internal object LocationData {
    
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