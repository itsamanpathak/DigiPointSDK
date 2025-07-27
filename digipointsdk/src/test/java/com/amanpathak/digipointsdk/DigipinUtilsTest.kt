package com.amanpathak.digipointsdk

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class DigipointUtilsTest {
    
    private lateinit var sdk: DigipointSDK
    
    @Before
    fun setUp() {
        sdk = DigipointSDK.Builder().build()
    }
    
    @Test
    fun testCalculateDistance() {
        val coord1 = DigipinCoordinate(28.6139, 77.2090) // Delhi
        val coord2 = DigipinCoordinate(19.0760, 72.8777) // Mumbai
        
        val distanceResult = sdk.calculateDistance(coord1, coord2)
        assertTrue(distanceResult is DigipointResult.Success)
        val distance = (distanceResult as DigipointResult.Success).data
        
        assertTrue("Distance should be positive", distance > 0)
        assertTrue("Distance should be reasonable", distance < 2000000) // Less than 2000km
    }
    
    @Test
    fun testCalculateDistanceSamePoint() {
        val coord = DigipinCoordinate(28.6139, 77.2090)
        val distanceResult = sdk.calculateDistance(coord, coord)
        assertTrue(distanceResult is DigipointResult.Success)
        val distance = (distanceResult as DigipointResult.Success).data
        
        assertEquals("Distance to same point should be 0", 0.0, distance, 0.1)
    }
    
    @Test
    fun testCalculateGridSizeMeters() {
        val coordinate = DigipinCoordinate(28.6139, 77.2090)
        val digipinResult = sdk.generateDigipin(coordinate.latitude, coordinate.longitude)
        assertTrue(digipinResult is DigipointResult.Success)
        val digipinCode = (digipinResult as DigipointResult.Success).data
        
        val gridSizeResult = sdk.calculateAreaSquareMeters(digipinCode)
        assertTrue(gridSizeResult is DigipointResult.Success)
        val gridSize = (gridSizeResult as DigipointResult.Success).data
        
        assertTrue("Grid size should be positive", gridSize > 0)
        assertTrue("Grid size should be reasonable", gridSize < 1000000) // Less than 1km²
    }
    
    @Test
    fun testCreateMapsUrl() {
        val coordinate = DigipinCoordinate(28.6139, 77.2090)
        val digipinResult = sdk.generateDigipin(coordinate.latitude, coordinate.longitude)
        assertTrue(digipinResult is DigipointResult.Success)
        val digipinCode = (digipinResult as DigipointResult.Success).data
        
        val urlResult = sdk.createGoogleMapsUrl(digipinCode)
        assertTrue(urlResult is DigipointResult.Success)
        val url = (urlResult as DigipointResult.Success).data
        
        assertTrue("URL should contain Google Maps", url.contains("www.google.com/maps"))
        // The coordinates might be formatted differently, so let's check for the pattern
        assertTrue("URL should contain latitude", url.contains("28.") || url.contains("28,"))
        assertTrue("URL should contain longitude", url.contains("77.") || url.contains("77,"))
    }
    
    @Test
    fun testGetPrecisionDescription() {
        val coordinate = DigipinCoordinate(28.6139, 77.2090)
        val digipinResult = sdk.generateDigipin(coordinate.latitude, coordinate.longitude)
        assertTrue(digipinResult is DigipointResult.Success)
        val digipinCode = (digipinResult as DigipointResult.Success).data
        
        val descriptionResult = sdk.getPrecisionDescription(digipinCode)
        assertTrue(descriptionResult is DigipointResult.Success)
        val description = (descriptionResult as DigipointResult.Success).data
        
        assertNotNull("Description should not be null", description)
        assertTrue("Description should not be empty", description.isNotEmpty())
        assertTrue("Description should contain precision info", description.contains("precision"))
    }
    
    @Test
    fun testCalculateAreaSquareMeters() {
        val coordinate = DigipinCoordinate(28.6139, 77.2090)
        val digipinResult = sdk.generateDigipin(coordinate.latitude, coordinate.longitude)
        assertTrue(digipinResult is DigipointResult.Success)
        val digipinCode = (digipinResult as DigipointResult.Success).data
        
        val areaResult = sdk.calculateAreaSquareMeters(digipinCode)
        assertTrue(areaResult is DigipointResult.Success)
        val area = (areaResult as DigipointResult.Success).data
        
        assertTrue("Area should be positive", area > 0)
        assertTrue("Area should be reasonable", area < 1000000) // Less than 1km²
    }
    
    @Test
    fun testFindDigipointCodesInRadius() {
        val center = DigipinCoordinate(28.6139, 77.2090) // Delhi
        val radius = 1000.0 // 1km
        
        val codesResult = sdk.findDigipointCodesInRadius(center, radius)
        assertTrue(codesResult is DigipointResult.Success)
        val codes = (codesResult as DigipointResult.Success).data
        
        assertNotNull("Codes should not be null", codes)
        assertTrue("Should find at least one code (center)", codes.isNotEmpty())
        
        // All codes should be within the radius
        codes.forEach { code ->
            val distanceResult = sdk.calculateDistance(center, code.centerCoordinate)
            assertTrue(distanceResult is DigipointResult.Success)
            val distance = (distanceResult as DigipointResult.Success).data
            assertTrue("Code should be within radius", distance <= radius)
        }
    }
    
    @Test
    fun testFindDigipointCodesInRadiusSmallRadius() {
        val center = DigipinCoordinate(28.6139, 77.2090)
        val radius = 10.0 // Very small radius
        
        val codesResult = sdk.findDigipointCodesInRadius(center, radius)
        assertTrue(codesResult is DigipointResult.Success)
        val codes = (codesResult as DigipointResult.Success).data
        
        assertNotNull("Codes should not be null", codes)
        // May be empty for very small radius, which is acceptable
    }
    
    @Test
    fun testFindDigipointCodesInRadiusLargeRadius() {
        val center = DigipinCoordinate(28.6139, 77.2090)
        val radius = 10000.0 // 10km
        
        val codesResult = sdk.findDigipointCodesInRadius(center, radius)
        assertTrue(codesResult is DigipointResult.Success)
        val codes = (codesResult as DigipointResult.Success).data
        
        assertNotNull("Codes should not be null", codes)
        assertTrue("Should find multiple codes for large radius", codes.size > 1)
    }
} 