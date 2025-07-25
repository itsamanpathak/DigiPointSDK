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
        val coord1 = DigipointCoordinate(28.6139, 77.2090) // Delhi
        val coord2 = DigipointCoordinate(19.0760, 72.8777) // Mumbai
        
        val distance = sdk.calculateDistance(coord1, coord2)
        
        assertTrue("Distance should be positive", distance > 0)
        assertTrue("Distance should be reasonable", distance < 2000000) // Less than 2000km
    }
    
    @Test
    fun testCalculateDistanceSamePoint() {
        val coord = DigipointCoordinate(28.6139, 77.2090)
        val distance = sdk.calculateDistance(coord, coord)
        
        assertEquals("Distance to same point should be 0", 0.0, distance, 0.1)
    }
    
    @Test
    fun testCalculateGridSizeMeters() {
        val coordinate = DigipointCoordinate(28.6139, 77.2090)
        val digipinCode = sdk.encode(coordinate)
        
        val gridSize = sdk.calculateAreaSquareMeters(digipinCode)
        
        assertTrue("Grid size should be positive", gridSize > 0)
        assertTrue("Grid size should be reasonable", gridSize < 1000000) // Less than 1km²
    }
    
    @Test
    fun testCreateMapsUrl() {
        val coordinate = DigipointCoordinate(28.6139, 77.2090)
        val digipinCode = sdk.encode(coordinate)
        
        val url = sdk.createMapsUrl(digipinCode)
        
        assertTrue("URL should contain Google Maps", url.contains("google.com/maps"))
        assertTrue("URL should contain coordinates", url.contains("28.6139"))
        assertTrue("URL should contain coordinates", url.contains("77.2090"))
    }
    
    @Test
    fun testGetPrecisionDescription() {
        val coordinate = DigipointCoordinate(28.6139, 77.2090)
        val digipinCode = sdk.encode(coordinate)
        
        val description = sdk.getPrecisionDescription(digipinCode)
        
        assertNotNull("Description should not be null", description)
        assertTrue("Description should not be empty", description.isNotEmpty())
        assertTrue("Description should contain precision info", description.contains("precision"))
    }
    
    @Test
    fun testCalculateAreaSquareMeters() {
        val coordinate = DigipointCoordinate(28.6139, 77.2090)
        val digipinCode = sdk.encode(coordinate)
        
        val area = sdk.calculateAreaSquareMeters(digipinCode)
        
        assertTrue("Area should be positive", area > 0)
        assertTrue("Area should be reasonable", area < 1000000) // Less than 1km²
    }
    
    @Test
    fun testFindDigipointCodesInRadius() {
        val center = DigipointCoordinate(28.6139, 77.2090) // Delhi
        val radius = 1000.0 // 1km
        
        val codes = sdk.findDigipointCodesInRadius(center, radius)
        
        assertNotNull("Codes should not be null", codes)
        assertTrue("Should find at least one code (center)", codes.isNotEmpty())
        
        // All codes should be within the radius
        codes.forEach { code ->
            val distance = sdk.calculateDistance(center, code.centerCoordinate)
            assertTrue("Code should be within radius", distance <= radius)
        }
    }
    
    @Test
    fun testFindDigipointCodesInRadiusSmallRadius() {
        val center = DigipointCoordinate(28.6139, 77.2090)
        val radius = 10.0 // Very small radius
        
        val codes = sdk.findDigipointCodesInRadius(center, radius)
        
        assertNotNull("Codes should not be null", codes)
        // May be empty for very small radius, which is acceptable
    }
    
    @Test
    fun testFindDigipointCodesInRadiusLargeRadius() {
        val center = DigipointCoordinate(28.6139, 77.2090)
        val radius = 10000.0 // 10km
        
        val codes = sdk.findDigipointCodesInRadius(center, radius)
        
        assertNotNull("Codes should not be null", codes)
        assertTrue("Should find multiple codes for large radius", codes.size > 1)
    }
} 