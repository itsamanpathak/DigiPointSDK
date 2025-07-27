package com.amanpathak.digipointsdk

/**
 * Result class for safe error handling without exceptions
 */
sealed class DigipointResult<out T> {
    data class Success<T>(val data: T) : DigipointResult<T>()
    data class Error(val message: String, val code: String? = null) : DigipointResult<Nothing>()
}

data class DigipinCoordinate(
    val latitude: Double,
    val longitude: Double
) {
    override fun toString(): String {
        return "($latitude, $longitude)"
    }
    
    companion object {
        /** Create coordinate with validation */
        fun create(latitude: Double, longitude: Double): DigipointResult<DigipinCoordinate> {
            return when {
                latitude < -90.0 || latitude > 90.0 -> 
                    DigipointResult.Error("Latitude must be between -90 and 90, got $latitude", "INVALID_LATITUDE")
                longitude < -180.0 || longitude > 180.0 -> 
                    DigipointResult.Error("Longitude must be between -180 and 180, got $longitude", "INVALID_LONGITUDE")
                else -> DigipointResult.Success(DigipinCoordinate(latitude, longitude))
            }
        }
    }
}

data class DigipointCode(
    val digipin: String,
    val centerCoordinate: DigipinCoordinate,
    val boundingBox: DigipointBoundingBox
) {
    fun getFormattedCode(): String {
        return if (digipin.length == 10) {
            "${digipin.substring(0, 3)}-${digipin.substring(3, 6)}-${digipin.substring(6, 10)}"
        } else {
            digipin
        }
    }
    
    override fun toString(): String {
        return "Digipin(code='$digipin', center=$centerCoordinate)"
    }
    
    companion object {
        /** Create DIGIPIN code with validation */
        fun create(digipin: String, centerCoordinate: DigipinCoordinate, boundingBox: DigipointBoundingBox): DigipointResult<DigipointCode> {
            return when {
                digipin.length != 10 -> 
                    DigipointResult.Error("DIGIPOINT code must be exactly 10 characters, got ${digipin.length}", "INVALID_LENGTH")
                !digipin.all { it in GridConstants.SYMBOLS } -> 
                    DigipointResult.Error("DIGIPOINT contains invalid characters", "INVALID_CHARACTERS")
                else -> DigipointResult.Success(DigipointCode(digipin, centerCoordinate, boundingBox))
            }
        }
    }
}

data class DigipointBoundingBox(
    val southwest: DigipinCoordinate,
    val northeast: DigipinCoordinate
) {
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
    
    companion object {
        /** Create bounding box with validation */
        fun create(southwest: DigipinCoordinate, northeast: DigipinCoordinate): DigipointResult<DigipointBoundingBox> {
            return when {
                southwest.latitude > northeast.latitude -> 
                    DigipointResult.Error("Southwest latitude must be <= northeast latitude", "INVALID_BOUNDS")
                southwest.longitude > northeast.longitude -> 
                    DigipointResult.Error("Southwest longitude must be <= northeast longitude", "INVALID_BOUNDS")
                else -> DigipointResult.Success(DigipointBoundingBox(southwest, northeast))
            }
        }
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