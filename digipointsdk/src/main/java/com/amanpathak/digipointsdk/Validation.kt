package com.amanpathak.digipointsdk

import java.util.regex.Pattern

/**
 * Internal validation utilities for DIGIPOINT operations.
 * 
 * This object provides validation methods for coordinates, DIGIPOINT codes,
 * and other input parameters with detailed error messages.
 */
internal object Validation {
    
    private val DIGIPOINT_PATTERN = Pattern.compile("[FC98J327K456LMPT]{10}")
    
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null,
        val warningMessage: String? = null
    )
    
    /**
     * Validates a DIGIPOINT code format and characters.
     * 
     * @param code The DIGIPOINT code to validate
     * @return ValidationResult containing success status and error message if any
     */
    fun validateDigipointCode(code: String): ValidationResult {
        if (code.isBlank()) {
            return ValidationResult(
                isValid = false,
                errorMessage = "DIGIPOINT code cannot be empty"
            )
        }
        
        if (code.length != Constants.DIGIPOINT_CODE_LENGTH) {
            return ValidationResult(
                isValid = false,
                errorMessage = "${Constants.ErrorMessages.INVALID_DIGIPOINT_LENGTH}, got ${code.length}"
            )
        }
        
        if (!DIGIPOINT_PATTERN.matcher(code).matches()) {
            val invalidChars = code.filter { it !in Constants.SYMBOLS }
            return ValidationResult(
                isValid = false,
                errorMessage = "${Constants.ErrorMessages.INVALID_DIGIPOINT_CHARACTERS}: ${invalidChars.toSet().joinToString()}"
            )
        }
        
        return ValidationResult(isValid = true)
    }
    
    /**
     * Validates geographical coordinates.
     * 
     * @param latitude The latitude value
     * @param longitude The longitude value
     * @return ValidationResult containing success status and error message if any
     */
    fun validateCoordinates(latitude: Double, longitude: Double): ValidationResult {
        if (latitude < -90.0 || latitude > 90.0) {
            return ValidationResult(
                isValid = false,
                errorMessage = "${Constants.ErrorMessages.INVALID_LATITUDE}, got $latitude"
            )
        }
        
        if (longitude < -180.0 || longitude > 180.0) {
            return ValidationResult(
                isValid = false,
                errorMessage = "${Constants.ErrorMessages.INVALID_LONGITUDE}, got $longitude"
            )
        }
        
        return ValidationResult(isValid = true)
    }
    
    /**
     * Validates if coordinates are within Indian bounds.
     * 
     * @param coordinate The coordinate to validate
     * @return ValidationResult containing success status and error message if any
     */
    fun validateIndianBounds(coordinate: DigipointCoordinate): ValidationResult {
        val coordValidation = validateCoordinates(coordinate.latitude, coordinate.longitude)
        if (!coordValidation.isValid) {
            return coordValidation
        }
        
        if (!isWithinIndianBounds(coordinate)) {
            return ValidationResult(
                isValid = false,
                errorMessage = "${Constants.ErrorMessages.COORDINATE_OUT_OF_BOUNDS}: $coordinate"
            )
        }
        
        // Check if coordinate is near the boundary
        val buffer = 0.1 // ~11km buffer
        if (coordinate.latitude > Constants.INDIA_MAX_LAT - buffer ||
            coordinate.latitude < Constants.INDIA_MIN_LAT + buffer ||
            coordinate.longitude > Constants.INDIA_MAX_LON - buffer ||
            coordinate.longitude < Constants.INDIA_MIN_LON + buffer) {
            return ValidationResult(
                isValid = true,
                warningMessage = "Coordinate is near Indian geographical boundary, some nearby DIGIPOINTs might be unavailable"
            )
        }
        
        return ValidationResult(isValid = true)
    }
    
    /**
     * Validates radius value for neighbor search.
     * 
     * @param radius The radius value to validate
     * @return ValidationResult containing success status and error message if any
     */
    fun validateRadius(radius: Int): ValidationResult {
        if (radius <= 0) {
            return ValidationResult(
                isValid = false,
                errorMessage = "${Constants.ErrorMessages.NEGATIVE_RADIUS}, got $radius"
            )
        }
        
        if (radius > 100) {
            return ValidationResult(
                isValid = true,
                warningMessage = "Radius will be limited to maximum 100 grid cells for performance reasons"
            )
        }
        
        return ValidationResult(isValid = true)
    }
    
    /**
     * Validates distance radius in meters.
     * 
     * @param radiusMeters The radius in meters to validate
     * @return ValidationResult containing success status and error message if any
     */
    fun validateDistanceRadius(radiusMeters: Double): ValidationResult {
        if (radiusMeters <= 0.0) {
            return ValidationResult(
                isValid = false,
                errorMessage = "${Constants.ErrorMessages.NEGATIVE_RADIUS} in meters, got $radiusMeters"
            )
        }
        
        if (radiusMeters > 1000000.0) { // 1000 km limit
            return ValidationResult(
                isValid = true,
                warningMessage = "Very large radius may result in incomplete results due to grid cell limits"
            )
        }
        
        if (radiusMeters < Constants.GRID_SIZE_METERS) {
            return ValidationResult(
                isValid = true,
                warningMessage = "Radius smaller than grid size (${Constants.GRID_SIZE_METERS}m) may not find any results"
            )
        }
        
        return ValidationResult(isValid = true)
    }
    
    /**
     * Validates a list of coordinates.
     * 
     * @param coordinates The list of coordinates to validate
     * @return ValidationResult containing success status and error message if any
     */
    fun validateCoordinateList(coordinates: List<DigipointCoordinate>): ValidationResult {
        if (coordinates.isEmpty()) {
            return ValidationResult(false, Constants.ErrorMessages.EMPTY_COORDINATE_LIST)
        }
        
        coordinates.forEachIndexed { index, coord ->
            val validation = validateCoordinates(coord.latitude, coord.longitude)
            if (!validation.isValid) {
                return ValidationResult(false, "Invalid coordinate at index $index: ${validation.errorMessage}")
            }
        }
        
        return ValidationResult(true, null)
    }
    
    /**
     * Validates precision level.
     * 
     * @param precision The precision level to validate
     * @return ValidationResult containing success status and error message if any
     */
    fun validatePrecisionLevel(precision: Int): ValidationResult {
        if (precision < 1 || precision > Constants.DIGIPOINT_CODE_LENGTH) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Precision must be between 1 and ${Constants.DIGIPOINT_CODE_LENGTH}, got $precision"
            )
        }
        
        if (precision < 8) {
            return ValidationResult(
                isValid = true,
                warningMessage = "Low precision level will result in large grid areas"
            )
        }
        
        return ValidationResult(isValid = true)
    }
    
    /**
     * Checks if coordinate is within Indian geographical bounds.
     */
    private fun isWithinIndianBounds(coordinate: DigipointCoordinate): Boolean {
        return coordinate.latitude >= Constants.INDIA_MIN_LAT &&
               coordinate.latitude <= Constants.INDIA_MAX_LAT &&
               coordinate.longitude >= Constants.INDIA_MIN_LON &&
               coordinate.longitude <= Constants.INDIA_MAX_LON
    }
    
    /**
     * Extension function to validate DIGIPOINT code and throw exception if invalid.
     */
    @Throws(DigipointInvalidFormatException::class)
    fun String.validateAndThrow() {
        val result = validateDigipointCode(this)
        if (!result.isValid) {
            throw DigipointInvalidFormatException(this, result.errorMessage ?: "Invalid format")
        }
    }
    
    /**
     * Extension function to validate coordinate and throw exception if invalid.
     */
    @Throws(DigipointOutOfBoundsException::class)
    fun DigipointCoordinate.validateIndianBoundsAndThrow() {
        val result = validateIndianBounds(this)
        if (!result.isValid) {
            throw DigipointOutOfBoundsException(this, DigipointSDK.INDIA_BOUNDS)
        }
    }
} 