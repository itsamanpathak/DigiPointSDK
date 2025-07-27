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
    fun testGenerateDigipinWithLatLon() {
        val result = sdk.generateDigipin(28.6139, 77.2090)
        
        assertTrue(result is DigipointResult.Success)
        val digipinCode = (result as DigipointResult.Success).data
        assertEquals(10, digipinCode.digipin.length)
    }
    
    @Test
    fun testGenerateDigipinOutOfBounds() {
        // Coordinates outside India
        val result = sdk.generateDigipin(0.0, 0.0)
        assertTrue(result is DigipointResult.Error)
        val error = result as DigipointResult.Error
        assertTrue(error.message.contains("outside India") || error.message.contains("bounds"))
    }
    
    @Test
    fun testGenerateLatLonValidCode() {
        val coordinate = DigipinCoordinate(28.6139, 77.2090)
        val encodedResult = sdk.generateDigipin(coordinate.latitude, coordinate.longitude)
        assertTrue(encodedResult is DigipointResult.Success)
        val encoded = (encodedResult as DigipointResult.Success).data
        
        val decodedResult = sdk.generateLatLon(encoded.digipin)
        assertTrue(decodedResult is DigipointResult.Success)
        val decoded = (decodedResult as DigipointResult.Success).data
        
        assertEquals(encoded.digipin, decoded.digipin)
        
        // Check if decoded coordinate is close to original (within reasonable precision)
        val distanceResult = sdk.calculateDistance(coordinate, decoded.centerCoordinate)
        assertTrue(distanceResult is DigipointResult.Success)
        val distance = (distanceResult as DigipointResult.Success).data
        assertTrue("Distance should be small", distance < 1000) // Within 1km
    }
    
    @Test
    fun testGenerateLatLonInvalidLength() {
        val result = sdk.generateLatLon("123")
        assertTrue(result is DigipointResult.Error)
        val error = result as DigipointResult.Error
        assertTrue(error.message.contains("length") || error.message.contains("10"))
    }
    
    @Test
    fun testGenerateLatLonInvalidCharacters() {
        val result = sdk.generateLatLon("1234567890")
        assertTrue(result is DigipointResult.Error)
        val error = result as DigipointResult.Error
        assertTrue(error.message.contains("invalid") || error.message.contains("character"))
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
        val encodedResult = sdk.generateDigipin(coordinate.latitude, coordinate.longitude)
        assertTrue(encodedResult is DigipointResult.Success)
        val encoded = (encodedResult as DigipointResult.Success).data
        
        assertTrue(sdk.isValidDigipointCode(encoded.digipin))
        assertFalse(sdk.isValidDigipointCode("123"))
        assertFalse(sdk.isValidDigipointCode("1234567890"))
        assertFalse(sdk.isValidDigipointCode(""))
    }
    
    @Test
    fun testGetNeighbors() {
        val coordinate = DigipinCoordinate(28.6139, 77.2090)
        val encodedResult = sdk.generateDigipin(coordinate.latitude, coordinate.longitude)
        assertTrue(encodedResult is DigipointResult.Success)
        val encoded = (encodedResult as DigipointResult.Success).data
        
        val neighborsResult = sdk.getNeighbors(encoded.digipin)
        assertTrue(neighborsResult is DigipointResult.Success)
        val neighbors = (neighborsResult as DigipointResult.Success).data
        
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
        val encodedResult = sdk.generateDigipin(coordinate.latitude, coordinate.longitude)
        assertTrue(encodedResult is DigipointResult.Success)
        val encoded = (encodedResult as DigipointResult.Success).data
        
        val neighbors1Result = sdk.getNeighbors(encoded.digipin, 1)
        val neighbors2Result = sdk.getNeighbors(encoded.digipin, 2)
        
        assertTrue(neighbors1Result is DigipointResult.Success)
        assertTrue(neighbors2Result is DigipointResult.Success)
        
        val neighbors1 = (neighbors1Result as DigipointResult.Success).data
        val neighbors2 = (neighbors2Result as DigipointResult.Success).data
        
        assertTrue(neighbors2.size >= neighbors1.size)
    }
    
    @Test
    fun testGetNeighborsInvalidRadius() {
        val coordinate = DigipinCoordinate(28.6139, 77.2090)
        val encodedResult = sdk.generateDigipin(coordinate.latitude, coordinate.longitude)
        assertTrue(encodedResult is DigipointResult.Success)
        val encoded = (encodedResult as DigipointResult.Success).data
        
        val neighborsResult = sdk.getNeighbors(encoded.digipin, 0)
        assertTrue(neighborsResult is DigipointResult.Error)
        val error = neighborsResult as DigipointResult.Error
        assertTrue("Should return error for invalid radius", error.message.contains("radius") || error.message.contains("0"))
    }
    
    @Test
    fun testGenerateDigipinGenerateLatLon() {
        val originalCoordinate = DigipinCoordinate(28.6139, 77.2090)
        val encodedResult = sdk.generateDigipin(originalCoordinate.latitude, originalCoordinate.longitude)
        assertTrue(encodedResult is DigipointResult.Success)
        val encoded = (encodedResult as DigipointResult.Success).data
        
        val decodedResult = sdk.generateLatLon(encoded.digipin)
        assertTrue(decodedResult is DigipointResult.Success)
        val decoded = (decodedResult as DigipointResult.Success).data
        
        // The decoded coordinate should be the same as in the encoded object
        assertEquals(encoded.centerCoordinate.latitude, decoded.centerCoordinate.latitude, 0.0001)
        assertEquals(encoded.centerCoordinate.longitude, decoded.centerCoordinate.longitude, 0.0001)
    }
    
    @Test
    fun testBoundingBox() {
        val coordinate = DigipinCoordinate(28.6139, 77.2090)
        val encodedResult = sdk.generateDigipin(coordinate.latitude, coordinate.longitude)
        assertTrue(encodedResult is DigipointResult.Success)
        val encoded = (encodedResult as DigipointResult.Success).data
        
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
            val encodedResult = sdk.generateDigipin(coordinate.latitude, coordinate.longitude)
            assertTrue(encodedResult is DigipointResult.Success)
            val encoded = (encodedResult as DigipointResult.Success).data
            
            val decodedResult = sdk.generateLatLon(encoded.digipin)
            assertTrue(decodedResult is DigipointResult.Success)
            val decoded = (decodedResult as DigipointResult.Success).data
            
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