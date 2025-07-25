package com.amanpathak.digipointsdk.demo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amanpathak.digipointsdk.DigipointSDK
import com.amanpathak.digipointsdk.DigipointCoordinate
import com.amanpathak.digipointsdk.demo.ui.theme.DigipointTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DigipointTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DigipointConverterScreen()
                }
            }
        }
    }
}

fun copyToClipboard(context: Context, text: String, label: String = "Digipoint") {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
}

fun isValidLatitude(lat: String): Boolean {
    return try {
        val value = lat.toDouble()
        value >= -90.0 && value <= 90.0
    } catch (e: NumberFormatException) {
        false
    }
}

fun isValidLongitude(lon: String): Boolean {
    return try {
        val value = lon.toDouble()
        value >= -180.0 && value <= 180.0
    } catch (e: NumberFormatException) {
        false
    }
}

fun isValidDigipoint(digipoint: String): Boolean {
    val cleanDigipoint = digipoint.replace("-", "")
    return cleanDigipoint.length == 10 && cleanDigipoint.all { it in "FC98J327K456LMPT" }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigipointConverterScreen() {
    val sdk = remember {
        DigipointSDK.Builder()
            .setValidationEnabled(true)
            .setPrecisionLevel(10)
            .build()
    }
    val context = LocalContext.current
    var digipointInput by remember { mutableStateOf("") }
    var latInput by remember { mutableStateOf("") }
    var lonInput by remember { mutableStateOf("") }
    var decodedLatLon by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var encodedDigipoint by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Digipoint Demo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            "Easily convert between Digipoint and Latitude/Longitude",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Digipoint to Lat/Lon
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Digipoint → Lat/Lon", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                
                OutlinedTextField(
                    value = digipointInput,
                    onValueChange = { digipointInput = it.uppercase() },
                    label = { Text("Enter Digipoint (e.g., 39J-438-P582)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = digipointInput.isNotBlank() && !isValidDigipoint(digipointInput),
                    supportingText = if (digipointInput.isNotBlank() && !isValidDigipoint(digipointInput)) {
                        { Text("Invalid Digipoint format", color = MaterialTheme.colorScheme.error) }
                    } else null
                )
                
                Button(
                    onClick = {
                        try {
                            val decoded = sdk.decode(digipointInput)
                            decodedLatLon = Pair(decoded.centerCoordinate.latitude, decoded.centerCoordinate.longitude)
                        } catch (e: Exception) {
                            decodedLatLon = null
                            Toast.makeText(context, "Decode Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    },
                    enabled = digipointInput.isNotBlank() && isValidDigipoint(digipointInput),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Convert to Lat/Lon")
                }
                
                decodedLatLon?.let { (lat, lon) ->
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Latitude: $lat", fontWeight = FontWeight.Medium)
                        Text("Longitude: $lon", fontWeight = FontWeight.Medium)
                        OutlinedButton(
                            onClick = {
                                val url = sdk.createMapsUrl(sdk.decode(digipointInput))
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("View on Map")
                        }
                    }
                }
            }
        }
        
        // Lat/Lon to Digipoint
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Lat/Lon → Digipoint", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                
                OutlinedTextField(
                    value = latInput,
                    onValueChange = { latInput = it },
                    label = { Text("Latitude (-90 to 90)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = latInput.isNotBlank() && !isValidLatitude(latInput),
                    supportingText = if (latInput.isNotBlank() && !isValidLatitude(latInput)) {
                        { Text("Invalid latitude (-90 to 90)", color = MaterialTheme.colorScheme.error) }
                    } else null
                )
                
                OutlinedTextField(
                    value = lonInput,
                    onValueChange = { lonInput = it },
                    label = { Text("Longitude (-180 to 180)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = lonInput.isNotBlank() && !isValidLongitude(lonInput),
                    supportingText = if (lonInput.isNotBlank() && !isValidLongitude(lonInput)) {
                        { Text("Invalid longitude (-180 to 180)", color = MaterialTheme.colorScheme.error) }
                    } else null
                )
                
                Button(
                    onClick = {
                        try {
                            val lat = latInput.toDouble()
                            val lon = lonInput.toDouble()
                            val code = sdk.encode(DigipointCoordinate(lat, lon))
                            encodedDigipoint = code.code
                        } catch (e: Exception) {
                            encodedDigipoint = null
                            Toast.makeText(context, "Encode Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    },
                    enabled = latInput.isNotBlank() && lonInput.isNotBlank() && 
                             isValidLatitude(latInput) && isValidLongitude(lonInput),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.SwapHoriz, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Convert to Digipoint")
                }
                
                encodedDigipoint?.let { code ->
                    val formattedCode = try {
                        sdk.decode(code).getFormattedCode()
                    } catch (e: Exception) {
                        "${code.substring(0, 3)}-${code.substring(3, 6)}-${code.substring(6, 10)}"
                    }
                    
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Digipoint: $formattedCode", fontWeight = FontWeight.Medium, fontSize = 20.sp)
                        
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    copyToClipboard(context, formattedCode)
                                }
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Copy")
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    val url = sdk.createMapsUrl(sdk.decode(code))
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                }
                            ) {
                                Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("View on Map")
                            }
                        }
                    }
                }
            }
        }
    }
}