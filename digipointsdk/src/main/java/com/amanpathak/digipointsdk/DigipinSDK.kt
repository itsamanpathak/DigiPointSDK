package com.amanpathak.digipointsdk

/**
 * Main SDK class for DIGIPIN operations.
 * 
 * Basic usage:
 * val sdk = DigipointSDK.Builder().build()
 * val code = sdk.encode(28.6139, 77.2090)
 */
class DigipointSDK private constructor(
    private val config: Config
) {
    private var lastWarning: String? = null
    
    class Builder {
        private var validationEnabled: Boolean = true
        private var precisionLevel: Int = DIGIPOINT_CODE_LENGTH
        
        fun setValidationEnabled(enabled: Boolean) = apply { validationEnabled = enabled }
        
        fun setPrecisionLevel(level: Int) = apply {
            val validation = Validation.validatePrecisionLevel(level)
            if (!validation.isValid) {
                throw DigipointException(validation.errorMessage ?: "Invalid precision level")
            }
            precisionLevel = level
        }
        
        fun build(): DigipointSDK {
            val config = Config(
                validationEnabled = validationEnabled,
                precisionLevel = precisionLevel
            )
            return DigipointSDK(config)
        }
    }
    
    companion object {
        const val VERSION = Constants.VERSION
        const val GRID_SIZE_METERS = Constants.GRID_SIZE_METERS
        const val DIGIPOINT_CODE_LENGTH = Constants.DIGIPOINT_CODE_LENGTH
        
        internal val SYMBOLS = Constants.SYMBOLS
        internal const val INDIA_MIN_LAT = Constants.INDIA_MIN_LAT
        internal const val INDIA_MAX_LAT = Constants.INDIA_MAX_LAT
        internal const val INDIA_MIN_LON = Constants.INDIA_MIN_LON
        internal const val INDIA_MAX_LON = Constants.INDIA_MAX_LON
        
        val INDIA_BOUNDS = DigipointBoundingBox(
            southwest = DigipinCoordinate(INDIA_MIN_LAT, INDIA_MIN_LON),
            northeast = DigipinCoordinate(INDIA_MAX_LAT, INDIA_MAX_LON)
        )
    }
    
    @Throws(DigipointOutOfBoundsException::class)
    fun encode(coordinate: DigipinCoordinate): DigipointCode {
        // validate bounds if enabled
        if (config.validationEnabled) {
            val validation = Validation.validateIndianBounds(coordinate)
            if (!validation.isValid) {
                throw DigipointOutOfBoundsException(coordinate, INDIA_BOUNDS)
            }
            lastWarning = validation.warningMessage
        }
        
        var latMin = INDIA_MIN_LAT
        var latMax = INDIA_MAX_LAT
        var lonMin = INDIA_MIN_LON
        var lonMax = INDIA_MAX_LON
        
        val codeBuilder = StringBuilder()
        
        repeat(config.precisionLevel) { _ ->
            val latDiv = (latMax - latMin) / 4
            val lonDiv = (lonMax - lonMin) / 4
            
            // row logic is reversed to match original algo
            var row = 3 - ((coordinate.latitude - latMin) / latDiv).toInt()
            var col = ((coordinate.longitude - lonMin) / lonDiv).toInt()
            
            // clamp values
            row = row.coerceIn(0, 3)
            col = col.coerceIn(0, 3)
            
            val symbol = SYMBOLS[row * 4 + col]
            codeBuilder.append(symbol)
            
            // update bounds for next iteration
            latMax = latMin + latDiv * (4 - row)
            latMin = latMin + latDiv * (3 - row)
            
            lonMin = lonMin + lonDiv * col
            lonMax = lonMin + lonDiv
        }
        
        val code = codeBuilder.toString()
        val (centerCoord, boundingBox) = decodeInternal(code)
        
        return DigipointCode(
            code = code,
            centerCoordinate = centerCoord,
            boundingBox = boundingBox
        )
    }
    
    @Throws(DigipointOutOfBoundsException::class)
    fun encode(latitude: Double, longitude: Double): DigipointCode {
        return encode(DigipinCoordinate(latitude, longitude))
    }
    
    @Throws(DigipointInvalidFormatException::class)
    fun decode(code: String): DigipointCode {
        // validate format if enabled
        if (config.validationEnabled) {
            val validation = Validation.validateDigipointCode(code)
            if (!validation.isValid) {
                throw DigipointInvalidFormatException(code, validation.errorMessage ?: "Invalid format")
            }
            lastWarning = validation.warningMessage
        }
        
        val (centerCoord, boundingBox) = decodeInternal(code)
        
        return DigipointCode(
            code = code,
            centerCoordinate = centerCoord,
            boundingBox = boundingBox
        )
    }
    
    fun isWithinIndianBounds(coordinate: DigipinCoordinate): Boolean {
        return INDIA_BOUNDS.contains(coordinate)
    }
    
    fun isValidDigipointCode(code: String): Boolean {
        return Validation.validateDigipointCode(code).isValid
    }
    
    fun getNeighbors(code: String, radius: Int = 1): List<DigipointCode> {
        // validate radius if needed
        if (config.validationEnabled) {
            val validation = Validation.validateRadius(radius)
            if (!validation.isValid) {
                lastWarning = null
                return emptyList()
            }
            lastWarning = validation.warningMessage
        }
        
        val centerDigipoint = decode(code)
        val neighbors = mutableListOf<DigipointCode>()
        
        // calculate grid size
        val gridSizeLat = (centerDigipoint.boundingBox.northeast.latitude - 
                          centerDigipoint.boundingBox.southwest.latitude)
        val gridSizeLon = (centerDigipoint.boundingBox.northeast.longitude - 
                          centerDigipoint.boundingBox.southwest.longitude)
        
        // find neighbors in grid
        for (latOffset in -radius..radius) {
            for (lonOffset in -radius..radius) {
                if (latOffset == 0 && lonOffset == 0) continue // skip center
                
                val neighborLat = centerDigipoint.centerCoordinate.latitude + (latOffset * gridSizeLat)
                val neighborLon = centerDigipoint.centerCoordinate.longitude + (lonOffset * gridSizeLon)
                
                try {
                    val neighborCoord = DigipinCoordinate(neighborLat, neighborLon)
                    if (isWithinIndianBounds(neighborCoord)) {
                        neighbors.add(encode(neighborCoord))
                    }
                } catch (e: Exception) {
                    // skip invalid coords
                }
            }
        }
        
        return neighbors
    }
    
    fun findDigipointCodesInRadius(
        center: DigipinCoordinate,
        radiusMeters: Double
    ): List<DigipointCode> {
        // validate radius
        if (config.validationEnabled) {
            val validation = Validation.validateDistanceRadius(radiusMeters)
            if (!validation.isValid) {
                lastWarning = null
                return emptyList()
            }
            lastWarning = validation.warningMessage
        }
        
        // get center digipoint
        val centerDigipoint = try {
            encode(center)
        } catch (e: DigipointOutOfBoundsException) {
            lastWarning = "Center coordinate is outside Indian bounds"
            return emptyList()
        }
        
        val gridSizeMeters = Utils.calculateGridSizeMeters(centerDigipoint)
        val gridRadius = kotlin.math.ceil(radiusMeters / gridSizeMeters).toInt()
        
        // limit radius for performance
        val safeGridRadius = gridRadius.coerceAtMost(100)
        if (safeGridRadius < gridRadius) {
            lastWarning = "Search radius limited to ${safeGridRadius * gridSizeMeters}m for performance"
        }
        
        val candidates = getNeighbors(centerDigipoint.code, safeGridRadius) + centerDigipoint
        
        return candidates.filter { candidate ->
            Utils.calculateDistance(center, candidate.centerCoordinate) <= radiusMeters
        }
    }
    
    fun createMapsUrl(digipointCode: DigipointCode): String = Utils.createMapsUrl(digipointCode)
    
    fun getPrecisionDescription(digipointCode: DigipointCode): String = Utils.getPrecisionDescription(digipointCode)
    
    fun calculateAreaSquareMeters(digipointCode: DigipointCode): Double = Utils.calculateAreaSquareMeters(digipointCode)
    
    fun calculateDistance(coord1: DigipinCoordinate, coord2: DigipinCoordinate): Double = Utils.calculateDistance(coord1, coord2)
    
    private fun decodeInternal(code: String): Pair<DigipinCoordinate, DigipointBoundingBox> {
        var latMin = INDIA_MIN_LAT
        var latMax = INDIA_MAX_LAT
        var lonMin = INDIA_MIN_LON
        var lonMax = INDIA_MAX_LON
        
        val cleanCode = code.replace("-", "")
        
        for (char in cleanCode) {
            var found = false
            var row = -1
            var col = -1
            
            // find char in grid
            for (r in 0..3) {
                for (c in 0..3) {
                    if (SYMBOLS[r * 4 + c] == char) {
                        row = r
                        col = c
                        found = true
                        break
                    }
                }
                if (found) break
            }
            
            if (!found) {
                throw DigipointInvalidFormatException(code, "bad char: $char")
            }
            
            val latDiv = (latMax - latMin) / 4
            val lonDiv = (lonMax - lonMin) / 4
            
            val lat1 = latMax - latDiv * (row + 1)
            val lat2 = latMax - latDiv * row
            val lon1 = lonMin + lonDiv * col
            val lon2 = lonMin + lonDiv * (col + 1)
            
            // Update bounding box for next level
            latMin = lat1
            latMax = lat2
            lonMin = lon1
            lonMax = lon2
        }
        
        val centerCoord = DigipinCoordinate(
            latitude = (latMin + latMax) / 2,
            longitude = (lonMin + lonMax) / 2
        )
        
        val boundingBox = DigipointBoundingBox(
            southwest = DigipinCoordinate(latMin, lonMin),
            northeast = DigipinCoordinate(latMax, lonMax)
        )
        
        return Pair(centerCoord, boundingBox)
    }
}

internal data class Config(
    val validationEnabled: Boolean = true,
    val precisionLevel: Int = DigipointSDK.DIGIPOINT_CODE_LENGTH
)

private fun Validation.ValidationResult.throwIfInvalid() {
    if (!isValid) {
        throw DigipointException(errorMessage ?: "Validation failed")
    }
} 