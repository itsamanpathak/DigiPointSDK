# DigiPointSDK Public API Reference

This document provides a clear overview of all public methods available in the DigiPointSDK.

## 📋 Public API Overview

The DigiPointSDK is organized into clear sections to make it easy to track and understand the public methods:

### Core Methods
These are the primary methods for converting between coordinates and DIGIPIN codes:

- `generateDigipin(latitude: Double, longitude: Double): DigipointCode`
- `generateLatLon(digipin: String): DigipointCode`

### Validation Methods
Methods for validating coordinates and DIGIPIN codes:

- `isWithinIndianBounds(coordinate: DigipinCoordinate): Boolean`
- `isValidDigipointCode(digipin: String): Boolean`

### Utility Methods
Helper methods for common operations:

- `getNeighbors(digipin: String, radius: Int = 1): List<DigipointCode>`
- `findDigipointCodesInRadius(center: DigipinCoordinate, radiusMeters: Double): List<DigipointCode>`
- `createMapsUrl(digipointCode: DigipointCode): String`
- `getPrecisionDescription(digipointCode: DigipointCode): String`
- `calculateAreaSquareMeters(digipointCode: DigipointCode): Double`
- `calculateDistance(coord1: DigipinCoordinate, coord2: DigipinCoordinate): Double`


## Detailed Method Documentation

### Core Methods

#### `generateDigipin(latitude: Double, longitude: Double): DigipointCode`
Generate a DIGIPIN code from latitude and longitude coordinates.

**Parameters:**
- `latitude`: Latitude coordinate (-90.0 to 90.0)
- `longitude`: Longitude coordinate (-180.0 to 180.0)

**Returns:** `DigipointCode` containing the generated code and location information

**Throws:** `DigipointOutOfBoundsException` if coordinates are outside Indian bounds

**Example:**
```kotlin
val sdk = DigipointSDK.Builder().build()
val digipinCode = sdk.generateDigipin(28.6139, 77.2090)
println(digipinCode.getFormattedCode()) // "39J-438-P582"
```

#### `generateLatLon(digipin: String): DigipointCode`
Generate latitude/longitude coordinates from a DIGIPIN code.

**Parameters:**
- `digipin`: DIGIPIN code (10 characters, with or without hyphens)

**Returns:** `DigipointCode` containing the decoded coordinates and bounding box

**Throws:** `DigipointInvalidFormatException` if code format is invalid

**Example:**
```kotlin
val sdk = DigipointSDK.Builder().build()
val coordinates = sdk.generateLatLon("39J438P582")
println("Lat: ${coordinates.centerCoordinate.latitude}")
println("Lon: ${coordinates.centerCoordinate.longitude}")
```

### Validation Methods

#### `isWithinIndianBounds(coordinate: DigipinCoordinate): Boolean`
Check if coordinates are within Indian geographical bounds.

**Parameters:**
- `coordinate`: The coordinate to check

**Returns:** `true` if within Indian bounds, `false` otherwise

**Example:**
```kotlin
val coordinate = DigipinCoordinate(28.6139, 77.2090)
val isInIndia = sdk.isWithinIndianBounds(coordinate) // true
```

#### `isValidDigipointCode(digipin: String): Boolean`
Validate DIGIPIN code format.

**Parameters:**
- `digipin`: The DIGIPIN code to validate

**Returns:** `true` if valid format, `false` otherwise

**Example:**
```kotlin
val isValid = sdk.isValidDigipointCode("39J438P582") // true
val isInvalid = sdk.isValidDigipointCode("1234567890") // false
```

### Utility Methods

#### `getNeighbors(digipin: String, radius: Int = 1): List<DigipointCode>`
Get neighboring DIGIPIN codes around a given code.

**Parameters:**
- `digipin`: The center DIGIPIN code
- `radius`: The radius of neighbors to find (default: 1)

**Returns:** List of neighboring `DigipointCode` objects

**Example:**
```kotlin
val neighbors = sdk.getNeighbors("39J438P582", radius = 2)
println("Found ${neighbors.size} neighbors")
```

#### `findDigipointCodesInRadius(center: DigipinCoordinate, radiusMeters: Double): List<DigipointCode>`
Find all DIGIPIN codes within a specified radius in meters.

**Parameters:**
- `center`: The center coordinate
- `radiusMeters`: The radius in meters to search

**Returns:** List of `DigipointCode` objects within the radius

**Example:**
```kotlin
val center = DigipinCoordinate(28.6139, 77.2090)
val nearbyCodes = sdk.findDigipointCodesInRadius(center, 1000.0) // 1km radius
```

#### `createMapsUrl(digipointCode: DigipointCode): String`
Create Google Maps URL for a DIGIPIN location.

**Parameters:**
- `digipointCode`: The `DigipointCode` to create URL for

**Returns:** Google Maps URL string

**Example:**
```kotlin
val url = sdk.createMapsUrl(digipinCode)
// Returns: "https://www.google.com/maps?q=28.6139,77.2090"
```

#### `getPrecisionDescription(digipointCode: DigipointCode): String`
Get human-readable precision description for a DIGIPIN code.

**Parameters:**
- `digipointCode`: The `DigipointCode` to get description for

**Returns:** Precision description string

**Example:**
```kotlin
val description = sdk.getPrecisionDescription(digipinCode)
// Returns: "Building level precision (~4.0m)"
```

#### `calculateAreaSquareMeters(digipointCode: DigipointCode): Double`
Calculate the area in square meters for a DIGIPIN code.

**Parameters:**
- `digipointCode`: The `DigipointCode` to calculate area for

**Returns:** Area in square meters

**Example:**
```kotlin
val area = sdk.calculateAreaSquareMeters(digipinCode)
println("Area: ${area} square meters")
```

#### `calculateDistance(coord1: DigipinCoordinate, coord2: DigipinCoordinate): Double`
Calculate distance between two coordinates in meters.

**Parameters:**
- `coord1`: First coordinate
- `coord2`: Second coordinate

**Returns:** Distance in meters

**Example:**
```kotlin
val distance = sdk.calculateDistance(coord1, coord2)
println("Distance: ${distance} meters")
```

## 🏗️ SDK Initialization

```kotlin
val sdk = DigipointSDK.Builder()
    .setValidationEnabled(true)  // Enable validation (recommended)
    .setPrecisionLevel(10)       // Set precision level (default: 10)
    .build()
```

## 📊 Constants

- `DigipointSDK.VERSION`: SDK version string
- `DigipointSDK.GRID_SIZE_METERS`: Grid size in meters
- `DigipointSDK.DIGIPOINT_CODE_LENGTH`: Length of DIGIPIN codes
- `DigipointSDK.INDIA_BOUNDS`: Indian geographical bounds

## 🎯 Best Practices

1. **Always use validation**: Enable validation in the SDK builder for better error handling
2. **Handle exceptions**: Wrap calls in try-catch blocks for `DigipointOutOfBoundsException` and `DigipointInvalidFormatException`
3. **Use appropriate precision**: Higher precision levels provide more accurate results but may not be necessary for all use cases
4. **Check bounds**: Use `isWithinIndianBounds()` before generating DIGIPIN codes for coordinates that might be outside India

## 📝 Notes

- All public methods are clearly documented with KDoc comments
- The SDK is organized into logical sections for easy maintenance
- Internal implementation details are kept private
- The API is designed to be intuitive and self-documenting 