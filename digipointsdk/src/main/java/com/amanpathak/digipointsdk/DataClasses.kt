package com.amanpathak.digipointsdk

data class DigipinCoordinate(
    val latitude: Double,
    val longitude: Double
) {
    init {
        require(latitude in -90.0..90.0) { 
            "Latitude must be between -90 and 90, got $latitude" 
        }
        require(longitude in -180.0..180.0) { 
            "Longitude must be between -180 and 180, got $longitude" 
        }
    }
    
    override fun toString(): String {
        return "($latitude, $longitude)"
    }
}

data class DigipointCode(
    val code: String,
    val centerCoordinate: DigipinCoordinate,
    val boundingBox: DigipointBoundingBox
) {
    init {
        require(code.length == 10) { 
            "DIGIPOINT code must be exactly 10 characters, got ${code.length}" 
        }
    }
    
    /**
     * Returns the DIGIPIN formatted with hyphens for display (e.g., "39J-438-P582")
     */
    fun getFormattedCode(): String {
        return if (code.length == 10) {
            "${code.substring(0, 3)}-${code.substring(3, 6)}-${code.substring(6, 10)}"
        } else {
            code
        }
    }
    
    override fun toString(): String {
        return "Digipin(code='$code', center=$centerCoordinate)"
    }
}

data class DigipointBoundingBox(
    val southwest: DigipinCoordinate,
    val northeast: DigipinCoordinate
) {
    init {
        require(southwest.latitude <= northeast.latitude) {
            "Southwest latitude must be <= northeast latitude"
        }
        require(southwest.longitude <= northeast.longitude) {
            "Southwest longitude must be <= northeast longitude"
        }
    }
    
    fun center(): DigipinCoordinate {
        val centerLat = (southwest.latitude + northeast.latitude) / 2
        val centerLon = (southwest.longitude + northeast.longitude) / 2
        return DigipinCoordinate(centerLat, centerLon)
    }
    
    fun contains(coordinate: DigipinCoordinate): Boolean {
        return coordinate.latitude >= southwest.latitude &&
               coordinate.latitude <= northeast.latitude &&
               coordinate.longitude >= southwest.longitude &&
               coordinate.longitude <= northeast.longitude
    }
    
    fun width(): Double = northeast.longitude - southwest.longitude
    
    fun height(): Double = northeast.latitude - southwest.latitude
    
    override fun toString(): String {
        return "BoundingBox(SW=$southwest, NE=$northeast)"
    }
}

open class DigipointException(message: String, cause: Throwable? = null) : Exception(message, cause)

class DigipointOutOfBoundsException(
    coordinate: DigipinCoordinate,
    bounds: DigipointBoundingBox
) : DigipointException(
    "Coordinate $coordinate is outside valid bounds $bounds"
)

class DigipointInvalidFormatException(
    code: String,
    reason: String
) : DigipointException(
    "Invalid DIGIPOINT code format '$code': $reason"
) 