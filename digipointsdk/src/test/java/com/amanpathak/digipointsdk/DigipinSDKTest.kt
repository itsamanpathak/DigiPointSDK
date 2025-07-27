package com.amanpathak.digipointsdk

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class DigipointSDKTest {
    
    private lateinit var sdk: DigipointSDK
    
    @Before
    fun setUp() {
        sdk = DigipointSDK.Builder().build()
    }
    
    @Test
    fun testGenerateDigipinValidCoordinate() {
        // Delhi coordinates
        val coordinate = DigipinCoordinate(28.6139, 77.2090)
        val result = sdk.generateDigipin(coordinate)
        
        assertNotNull(result)
        assertEquals(10, result.digipin.length)
        assertTrue(result.digipin.all { it in DigipointSDK.SYMBOLS })
    }
    
    @Test
    fun testGenerateDigipinWithLatLon() {
        val result = sdk.generateDigipin(28.6139, 77.2090)
        
        assertNotNull(result)
        assertEquals(10, result.digipin.length)
    }
    
    @Test(expected = DigipointOutOfBoundsException::class)
    fun testGenerateDigipinOutOfBounds() {
        // Coordinates outside India
        sdk.generateDigipin(0.0, 0.0)
    }
    
    @Test
    fun testGenerateLatLonValidCode() {
        val coordinate = DigipinCoordinate(28.6139, 77.2090)
        val encoded = sdk.generateDigipin(coordinate)
        val decoded = sdk.generateLatLon(encoded.digipin)
        
        assertNotNull(decoded)
        assertEquals(encoded.digipin, decoded.digipin)
        
        // Check if decoded coordinate is close to original (within reasonable precision)
        val distance = sdk.calculateDistance(coordinate, decoded.centerCoordinate)
        assertTrue("Distance should be small", distance < 1000) // Within 1km
    }
    
    @Test(expected = DigipointInvalidFormatException::class)
    fun testGenerateLatLonInvalidLength() {
        sdk.generateLatLon("123")
    }
    
    @Test(expected = DigipointInvalidFormatException::class)
    fun testGenerateLatLonInvalidCharacters() {
        sdk.generateLatLon("1234567890")
    }
    
    @Test
    fun testIsWithinIndianBounds() {
        // Valid Indian coordinates
        assertTrue(sdk.isWithinIndianBounds(DigipinCoordinate(28.6139, 77.2090))) // Delhi
        assertTrue(sdk.isWithinIndianBounds(DigipinCoordinate(19.0760, 72.8777))) // Mumbai
        assertTrue(sdk.isWithinIndianBounds(DigipinCoordinate(13.0827, 80.2707))) // Chennai
        
        // Invalid coordinates
        assertFalse(sdk.isWithinIndianBounds(DigipinCoordinate(0.0, 0.0))) // Africa
        assertFalse(sdk.isWithinIndianBounds(DigipinCoordinate(40.7128, -74.0060))) // New York
    }
    
    @Test
    fun testIsValidDigipointCode() {
        val coordinate = DigipinCoordinate(28.6139, 77.2090)
        val encoded = sdk.generateDigipin(coordinate)
        
        assertTrue(sdk.isValidDigipointCode(encoded.digipin))
        assertFalse(sdk.isValidDigipointCode("123"))
        assertFalse(sdk.isValidDigipointCode("1234567890"))
        assertFalse(sdk.isValidDigipointCode(""))
    }
    
    @Test
    fun testGetNeighbors() {
        val coordinate = DigipinCoordinate(28.6139, 77.2090)
        val encoded = sdk.generateDigipin(coordinate)
        val neighbors = sdk.getNeighbors(encoded.digipin)
        
        assertNotNull(neighbors)
        assertTrue(neighbors.isNotEmpty())
        
        // All neighbors should be valid DIGIPOINT codes
        neighbors.forEach { neighbor ->
            assertTrue(sdk.isValidDigipointCode(neighbor.digipin))
            assertTrue(sdk.isWithinIndianBounds(neighbor.centerCoordinate))
        }
    }
    
    @Test
    fun testGetNeighborsWithRadius() {
        val coordinate = DigipinCoordinate(28.6139, 77.2090)
        val encoded = sdk.generateDigipin(coordinate)
        
        val neighbors1 = sdk.getNeighbors(encoded.digipin, 1)
        val neighbors2 = sdk.getNeighbors(encoded.digipin, 2)
        
        assertTrue(neighbors2.size >= neighbors1.size)
    }
    
    @Test
    fun testGetNeighborsInvalidRadius() {
        val coordinate = DigipinCoordinate(28.6139, 77.2090)
        val encoded = sdk.generateDigipin(coordinate)
        val neighbors = sdk.getNeighbors(encoded.digipin, 0)
        
        // Should return empty list for invalid radius
        assertTrue("Should return empty list for invalid radius", neighbors.isEmpty())
    }
    
    @Test
    fun testGenerateDigipinGenerateLatLon() {
        val originalCoordinate = DigipinCoordinate(28.6139, 77.2090)
        val encoded = sdk.generateDigipin(originalCoordinate)
        val decoded = sdk.generateLatLon(encoded.digipin)
        
        // The decoded coordinate should be the same as in the encoded object
        assertEquals(encoded.centerCoordinate.latitude, decoded.centerCoordinate.latitude, 0.0001)
        assertEquals(encoded.centerCoordinate.longitude, decoded.centerCoordinate.longitude, 0.0001)
    }
    
    @Test
    fun testBoundingBox() {
        val coordinate = DigipinCoordinate(28.6139, 77.2090)
        val encoded = sdk.generateDigipin(coordinate)
        val boundingBox = encoded.boundingBox
        
        // Center should be within bounding box
        assertTrue(boundingBox.contains(encoded.centerCoordinate))
        
        // Original coordinate should be within bounding box
        assertTrue(boundingBox.contains(coordinate))
        
        // Southwest should be less than northeast
        assertTrue(boundingBox.southwest.latitude < boundingBox.northeast.latitude)
        assertTrue(boundingBox.southwest.longitude < boundingBox.northeast.longitude)
    }
    
    @Test
    fun testMultipleCoordinates() {
        val coordinates = listOf(
            DigipinCoordinate(28.6139, 77.2090), // Delhi
            DigipinCoordinate(19.0760, 72.8777), // Mumbai
            DigipinCoordinate(13.0827, 80.2707), // Chennai
            DigipinCoordinate(22.5726, 88.3639)  // Kolkata
        )
        
        coordinates.forEach { coordinate ->
            val encoded = sdk.generateDigipin(coordinate)
            val decoded = sdk.generateLatLon(encoded.digipin)
            
            assertNotNull(encoded)
            assertNotNull(decoded)
            assertEquals(encoded.digipin, decoded.digipin)
        }
    }
    
    @Test
    fun testConstants() {
        assertEquals("0.1.0", DigipointSDK.VERSION)
        assertEquals(4.0, DigipointSDK.GRID_SIZE_METERS, 0.0)
        assertEquals(10, DigipointSDK.DIGIPOINT_CODE_LENGTH)
        assertEquals(16, DigipointSDK.SYMBOLS.size)
    }
    
    @Test
    fun testSymbolsUniqueness() {
        val symbols = DigipointSDK.SYMBOLS
        assertEquals(symbols.size, symbols.distinct().size)
    }
    
    @Test
    fun testIndiaBounds() {
        val bounds = DigipointSDK.INDIA_BOUNDS
        
        assertTrue(bounds.southwest.latitude < bounds.northeast.latitude)
        assertTrue(bounds.southwest.longitude < bounds.northeast.longitude)
        
        // Check actual Indian bounds from Constants
        assertEquals(2.5, bounds.southwest.latitude, 0.1)
        assertEquals(63.5, bounds.southwest.longitude, 0.1)
        assertEquals(38.5, bounds.northeast.latitude, 0.1)
        assertEquals(99.5, bounds.northeast.longitude, 0.1)
    }
} 