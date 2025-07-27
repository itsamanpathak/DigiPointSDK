# DigiPoint Android SDK

<div align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" alt="DigiPoint SDK Logo" width="120" height="120">
  <br>
  <strong>Android SDK for India's Digital Postal Index Number (DIGIPIN) System</strong>
  <br>
  <em>4m×4m precision geo-coding for India</em>
</div>

---

## 📋 Table of Contents

- [Overview](#overview)
- [Disclaimer](#disclaimer)
- [Features](#features)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Usage Guide](#usage-guide)
- [API Reference](#api-reference)
- [Examples](#examples)
- [Contributing](#contributing)
- [License](#license)

## 🌟 Overview

DigiPoint Android SDK is a Kotlin library for India's Digital Postal Index Number (DIGIPIN) system. Convert coordinates to 10-character codes and back with 4m×4m precision.

> **⚠️ Pre-release Version (v0.1.0)**: This is a beta release for testing and feedback. Please report any issues you encounter.

**Key Benefits:**
- ⚡ **High Precision**: 4m×4m grid accuracy
- 🗺️ **India-Focused**: Optimized for Indian boundaries
- 🛡️ **Validation**: Built-in coordinate and format checks
- 📱 **Android Native**: Kotlin with modern practices
- 🔧 **Easy to use**: Simple API

## ⚠️ Disclaimer

**This is NOT an official SDK from India Post.** This library is a community-maintained fork of the official JavaScript DIGIPIN SDK, adapted specifically for Android development. While it follows the same specifications and algorithms as the official implementation, it is not officially endorsed by India Post.

## ✨ Features

- **Coordinate Encoding**: Convert latitude/longitude to 10-character DIGIPIN codes
- **Code Decoding**: Convert DIGIPIN codes back to geographic coordinates
- **Boundary Validation**: Ensure coordinates are within Indian boundaries
- **Neighbor Discovery**: Find adjacent DIGIPIN codes
- **Radius Search**: Find all DIGIPIN codes within a specified radius
- **Maps Integration**: Generate Google Maps URLs for DIGIPIN locations
- **Precision Analysis**: Calculate area and precision information
- **Distance Calculation**: Compute distances between coordinates

## 📦 Installation

### Using JitPack (Recommended)

Add JitPack repository to your project-level `build.gradle.kts`:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency to your app-level `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.itsamanpathak:DigiPointSDK:0.1.0")
}
```

### Manual Installation

1. Clone this repository
2. Add the `digipointsdk` module to your project
3. Add the module dependency to your app

## 🚀 Quick Start

### Basic Setup

```kotlin
import com.amanpathak.digipointsdk.DigipointSDK
import com.amanpathak.digipointsdk.DigipinCoordinate

// Initialize the SDK
val sdk = DigipointSDK.Builder()
    .setValidationEnabled(true)
    .build()
```

### Encode Coordinates to DIGIPIN

```kotlin
// Example: New Delhi coordinates
val coordinate = DigipinCoordinate(28.6139, 77.2090)
val digipinCode = sdk.generateDigipin(coordinate)

println("DIGIPIN: ${digipinCode.getFormattedCode()}") // Output: 39J-438-P582
println("Center: ${digipinCode.centerCoordinate}") // Output: (28.6139, 77.2090)
```

### Generate Lat/Lon from DIGIPIN

```kotlin
val code = "39J438P582"
val digipinCode = sdk.generateLatLon(code)

println("Coordinates: ${digipinCode.centerCoordinate}")
println("Bounding Box: ${digipinCode.boundingBox}")
```

## 📖 Usage Guide

### Step 1: Initialize the SDK

```kotlin
val sdk = DigipointSDK.Builder()
    .setValidationEnabled(true)  // Enable validation (recommended)
    .setPrecisionLevel(10)       // Set precision level (default: 10)
    .build()
```

**Configuration Options:**
- `setValidationEnabled(boolean)`: Enable/disable coordinate and format validation
- `setPrecisionLevel(int)`: Set DIGIPIN precision level (1-10, default: 10)

### Step 2: Generate DIGIPIN from Coordinates

```kotlin
// Method 1: Using DigipinCoordinate object
val coordinate = DigipinCoordinate(28.6139, 77.2090)
val digipinCode = sdk.generateDigipin(coordinate)

// Method 2: Using latitude and longitude directly
val digipinCode = sdk.generateDigipin(28.6139, 77.2090)

// Get formatted code for display
val formattedCode = digipinCode.getFormattedCode() // "39J-438-P582"
```

### Step 3: Generate Lat/Lon from DIGIPIN

```kotlin
// Generate coordinates from a DIGIPIN code (with or without hyphens)
val digipinCode = sdk.generateLatLon("39J438P582")
// or
val digipinCode = sdk.generateLatLon("39J-438-P582")

// Access generated information
val center = digipinCode.centerCoordinate
val bounds = digipinCode.boundingBox
val area = sdk.calculateAreaSquareMeters(digipinCode)
```

### Step 4: Find Neighboring Codes

```kotlin
// Find adjacent DIGIPIN codes
val neighbors = sdk.getNeighbors("39J438P582", radius = 1)

// Find codes within a specific radius (in meters)
val nearbyCodes = sdk.findDigipointCodesInRadius(
    center = DigipinCoordinate(28.6139, 77.2090),
    radiusMeters = 100.0
)
```

### Step 5: Generate Maps URLs

```kotlin
// Create Google Maps URL for a DIGIPIN location
val mapsUrl = sdk.createMapsUrl(digipinCode)
// Output: "https://maps.google.com/?q=28.6139,77.2090"
```

### Step 6: Validation and Error Handling

```kotlin
try {
    // Check if coordinates are within Indian bounds
    val isValid = sdk.isWithinIndianBounds(coordinate)
    
    // Validate DIGIPIN code format
    val isValidCode = sdk.isValidDigipointCode("39J438P582")
    
    // Generate DIGIPIN with validation
    val digipinCode = sdk.generateDigipin(coordinate)
    
} catch (e: DigipointOutOfBoundsException) {
    // Handle coordinates outside Indian boundaries
    Log.e("DigiPoint", "Coordinates outside India: ${e.message}")
} catch (e: DigipointInvalidFormatException) {
    // Handle invalid DIGIPIN code format
    Log.e("DigiPoint", "Invalid code format: ${e.message}")
}
```

## 📚 API Reference

For a complete and organized reference of all public methods, see [PUBLIC_API.md](PUBLIC_API.md).

### Core Classes

#### `DigipointSDK`
Main entry point for all DIGIPIN operations.

**Public Methods:**
- `generateDigipin(latitude: Double, longitude: Double): DigipointCode`
- `generateLatLon(code: String): DigipointCode`
- `isWithinIndianBounds(coordinate: DigipinCoordinate): Boolean`
- `isValidDigipointCode(code: String): Boolean`
- `getNeighbors(code: String, radius: Int = 1): List<DigipointCode>`
- `findDigipointCodesInRadius(center: DigipinCoordinate, radiusMeters: Double): List<DigipointCode>`
- `createMapsUrl(digipointCode: DigipointCode): String`
- `getPrecisionDescription(digipointCode: DigipointCode): String`
- `calculateAreaSquareMeters(digipointCode: DigipointCode): Double`
- `calculateDistance(coord1: DigipinCoordinate, coord2: DigipinCoordinate): Double`

#### `DigipinCoordinate`
Represents a geographic coordinate.

**Properties:**
- `latitude: Double` (range: -90.0 to 90.0)
- `longitude: Double` (range: -180.0 to 180.0)

#### `DigipointCode`
Represents a decoded DIGIPIN code with location information.

**Properties:**
- `code: String` (10-character DIGIPIN code)
- `centerCoordinate: DigipinCoordinate`
- `boundingBox: DigipointBoundingBox`

**Methods:**
- `getFormattedCode(): String` (returns hyphenated format)

#### `DigipointBoundingBox`
Represents the geographic bounds of a DIGIPIN code.

**Properties:**
- `southwest: DigipinCoordinate`
- `northeast: DigipinCoordinate`

**Methods:**
- `center(): DigipinCoordinate`
- `contains(coordinate: DigipinCoordinate): Boolean`
- `width(): Double`
- `height(): Double`

### Exceptions

- `DigipointException`: Base exception for all DIGIPIN-related errors
- `DigipointOutOfBoundsException`: Thrown when coordinates are outside Indian boundaries
- `DigipointInvalidFormatException`: Thrown when DIGIPIN code format is invalid

## 💡 Examples

### Complete Example: Location Service

```kotlin
class LocationService {
    private val sdk = DigipointSDK.Builder()
        .setValidationEnabled(true)
        .build()
    
    fun getLocationInfo(latitude: Double, longitude: Double): LocationInfo {
        return try {
            val coordinate = DigipinCoordinate(latitude, longitude)
            val digipinCode = sdk.generateDigipin(coordinate)
            
            LocationInfo(
                digipinCode = digipinCode.getFormattedCode(),
                centerCoordinate = digipinCode.centerCoordinate,
                area = sdk.calculateAreaSquareMeters(digipinCode),
                neighbors = sdk.getNeighbors(digipinCode.code, radius = 1),
                mapsUrl = sdk.createMapsUrl(digipinCode)
            )
        } catch (e: DigipointOutOfBoundsException) {
            throw LocationException("Location outside India")
        }
    }
}

data class LocationInfo(
    val digipinCode: String,
    val centerCoordinate: DigipinCoordinate,
    val area: Double,
    val neighbors: List<DigipointCode>,
    val mapsUrl: String
)
```

### Example: Address Validation

```kotlin
fun validateAddress(latitude: Double, longitude: Double): ValidationResult {
    val sdk = DigipointSDK.Builder().build()
    
    return if (sdk.isWithinIndianBounds(DigipinCoordinate(latitude, longitude))) {
        val digipinCode = sdk.generateDigipin(latitude, longitude)
        ValidationResult.Success(digipinCode.getFormattedCode())
    } else {
        ValidationResult.Error("Address is outside India")
    }
}
```

### Example: Nearby Search

```kotlin
fun findNearbyLocations(
    centerLat: Double, 
    centerLon: Double, 
    radiusMeters: Double
): List<NearbyLocation> {
    val sdk = DigipointSDK.Builder().build()
    val center = DigipinCoordinate(centerLat, centerLon)
    
    return sdk.findDigipointCodesInRadius(center, radiusMeters)
        .map { digipinCode ->
            NearbyLocation(
                code = digipinCode.getFormattedCode(),
                coordinate = digipinCode.centerCoordinate,
                distance = sdk.calculateDistance(center, digipinCode.centerCoordinate)
            )
        }
        .sortedBy { it.distance }
}

data class NearbyLocation(
    val code: String,
    val coordinate: DigipinCoordinate,
    val distance: Double
)
```

## 🤝 Contributing

We welcome contributions! Please feel free to submit issues, feature requests, or pull requests.

### Development Setup

1. Clone the repository
2. Open in Android Studio
3. Build the project
4. Run tests: `./gradlew test`

### Code Style

- Follow Kotlin coding conventions
- Add unit tests for new features
- Update documentation for API changes

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🔗 Related Links

- [Official DIGIPIN Documentation](https://www.indiapost.gov.in/VAS/Pages/digipin.aspx)
- [India Post Official Website](https://www.indiapost.gov.in/)
- [Official GitHub Repository](https://github.com/CEPT-VZG/digipin)

---

<div align="center">
  <p>Made with ❤️ for the Indian developer community</p>
  <p><strong>Not affiliated with India Post</strong></p>
</div> 