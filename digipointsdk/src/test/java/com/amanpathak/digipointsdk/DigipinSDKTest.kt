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
    fun testEncodeValidCoordinate() {
        // Delhi coordinates
        val coordinate = DigipinCoordinate(28.6139, 77.2090)
        val result = sdk.encode(coordinate)
        
        assertNotNull(result)
        assertEquals(10, result.code.length)
        assertTrue(result.code.all { it in DigipointSDK.SYMBOLS })
    }
    
    @Test
    fun testEncodeWithLatLon() {
        val result = sdk.encode(28.6139, 77.2090)
        
        assertNotNull(result)
        assertEquals(10, result.code.length)
    }
    
    @Test(expected = DigipointOutOfBoundsException::class)
    fun testEncodeOutOfBounds() {
        // Coordinates outside India
        sdk.encode(0.0, 0.0)
    }
    
    @Test
    fun testDecodeValidCode() {
        val coordinate = DigipinCoordinate(28.6139, 77.2090)
        val encoded = sdk.encode(coordinate)
        val decoded = sdk.decode(encoded.code)
        
        assertNotNull(decoded)
        assertEquals(encoded.code, decoded.code)
        
        // Check if decoded coordinate is close to original (within reasonable precision)
        val distance = sdk.calculateDistance(coordinate, decoded.centerCoordinate)
        assertTrue("Distance should be small", distance < 1000) // Within 1km
    }
    
    @Test(expected = DigipointInvalidFormatException::class)
    fun testDecodeInvalidLength() {
        sdk.decode("123")
    }
    
    @Test(expected = DigipointInvalidFormatException::class)
    fun testDecodeInvalidCharacters() {
        sdk.decode("1234567890")
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
        val encoded = sdk.encode(coordinate)
        
        assertTrue(sdk.isValidDigipointCode(encoded.code))
        assertFalse(sdk.isValidDigipointCode("123"))
        assertFalse(sdk.isValidDigipointCode("1234567890"))
        assertFalse(sdk.isValidDigipointCode(""))
    }
    
    @Test
    fun testGetNeighbors() {
        val coordinate = DigipinCoordinate(28.6139, 77.2090)
        val encoded = sdk.encode(coordinate)
        val neighbors = sdk.getNeighbors(encoded.code)
        
        assertNotNull(neighbors)
        assertTrue(neighbors.isNotEmpty())
        
        // All neighbors should be valid DIGIPOINT codes
        neighbors.forEach { neighbor ->
            assertTrue(sdk.isValidDigipointCode(neighbor.code))
            assertTrue(sdk.isWithinIndianBounds(neighbor.centerCoordinate))
        }
    }
    
    @Test
    fun testGetNeighborsWithRadius() {
        val coordinate = DigipinCoordinate(28.6139, 77.2090)
        val encoded = sdk.encode(coordinate)
        
        val neighbors1 = sdk.getNeighbors(encoded.code, 1)
        val neighbors2 = sdk.getNeighbors(encoded.code, 2)
        
        assertTrue(neighbors2.size >= neighbors1.size)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun testGetNeighborsInvalidRadius() {
        val coordinate = DigipinCoordinate(28.6139, 77.2090)
        val encoded = sdk.encode(coordinate)
        sdk.getNeighbors(encoded.code, 0)
    }
    
    @Test
    fun testEncodeDecode() {
        val originalCoordinate = DigipinCoordinate(28.6139, 77.2090)
        val encoded = sdk.encode(originalCoordinate)
        val decoded = sdk.decode(encoded.code)
        
        // The decoded coordinate should be the same as in the encoded object
        assertEquals(encoded.centerCoordinate.latitude, decoded.centerCoordinate.latitude, 0.0001)
        assertEquals(encoded.centerCoordinate.longitude, decoded.centerCoordinate.longitude, 0.0001)
    }
    
    @Test
    fun testBoundingBox() {
        val coordinate = DigipinCoordinate(28.6139, 77.2090)
        val encoded = sdk.encode(coordinate)
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
            val encoded = sdk.encode(coordinate)
            val decoded = sdk.decode(encoded.code)
            
            assertNotNull(encoded)
            assertNotNull(decoded)
            assertEquals(encoded.code, decoded.code)
        }
    }
    
    @Test
    fun testConstants() {
        assertEquals("1.0.0", DigipointSDK.VERSION)
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
        
        // Check approximate Indian bounds
        assertEquals(6.0, bounds.southwest.latitude, 0.1)
        assertEquals(66.0, bounds.southwest.longitude, 0.1)
        assertEquals(38.0, bounds.northeast.latitude, 0.1)
        assertEquals(100.0, bounds.northeast.longitude, 0.1)
    }
} 