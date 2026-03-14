package com.jimmypiedrahita.accesibilidad

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.core.graphics.toColorInt
import com.jimmypiedrahita.accesibilidad.ui.theme.AccesibilidadTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AccesibilidadTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FilterApp()
                }
            }
        }
    }
}

@Composable
fun FilterApp() {
    val context = LocalContext.current
    var isAccessibilityEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }

    // Check status on resume
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isAccessibilityEnabled = isAccessibilityServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (isAccessibilityEnabled) {
        FilterDashboardScreen()
    } else {
        PermissionRequestScreen()
    }
}

@Composable
fun PermissionRequestScreen() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = "Permission Needed",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Accessibility Permission Required",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "To overlay the blue light filter on top of other apps, we need accessibility permissions. This ensures your eyes are protected at all times.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { openAccessibilitySettings(context) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Enable Accessibility")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDashboardScreen() {
    val context = LocalContext.current
    // State management
    var isFilterActive by remember { mutableStateOf(BlueLightFilterService.isFilterActive()) }
    var intensity by remember { mutableIntStateOf(BlueLightFilterService.getCurrentIntensity()) }
    var currentColor by remember { mutableStateOf(BlueLightFilterService.getCurrentColor()) }
    var showColorPicker by remember { mutableStateOf(false) }

    // Sync state on resume
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isFilterActive = BlueLightFilterService.isFilterActive()
                intensity = BlueLightFilterService.getCurrentIntensity()
                currentColor = BlueLightFilterService.getCurrentColor()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Eye Protector", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Toggle Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clickable {
                        val newState = !isFilterActive
                        BlueLightFilterService.toggleFilter(newState)
                        isFilterActive = newState
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFilterActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isFilterActive) "Filter Active" else "Filter Inactive",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isFilterActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isFilterActive) "Tap to turn off" else "Tap to turn on",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isFilterActive) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Switch(
                        checked = isFilterActive,
                        onCheckedChange = { 
                            BlueLightFilterService.toggleFilter(it)
                            isFilterActive = it
                        }
                    )
                }
            }

            AnimatedVisibility(visible = isFilterActive) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Start of Intensity Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text("Intensity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.weight(1f))
                                Text("$intensity%", style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(Modifier.height(8.dp))
                            Slider(
                                value = intensity.toFloat(),
                                onValueChange = { newValue ->
                                    intensity = newValue.toInt()
                                    BlueLightFilterService.setIntensity(intensity)
                                },
                                valueRange = 0f..100f,
                                steps = 0,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Start of Color Car
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showColorPicker = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Filter Color", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(currentColor)
                                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = currentColor,
            onDismiss = { showColorPicker = false },
            onColorSelected = { newColor ->
                currentColor = newColor
                BlueLightFilterService.setColor(newColor)
            }
        )
    }
}

@Composable
fun ColorPickerDialog(
    initialColor: Color,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    var currentColor by remember { mutableStateOf(initialColor) }
    
    // HSV State setup
    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(0f) }
    var value by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        val hsv = FloatArray(3)
        AndroidColor.colorToHSV(initialColor.toArgb(), hsv)
        hue = hsv[0]
        saturation = hsv[1]
        value = hsv[2]
    }

    val updateColorFromHSV = {
        val newColor = Color.hsv(hue, saturation, value)
        currentColor = newColor
        onColorSelected(newColor)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Filter Color", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                 // Presets
                Text("Presets", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val presets = listOf(
                        Color(0xFF000000), // Black
                        Color(0xFF241e00), // Amber-ish dark
                        Color(0xFF042400), // Green-ish dark
                        Color(0xFF2e1d00), // Orange-ish dark
                        Color(0xFF7D5260)  // Pink-ish dark
                    )
                    items(presets.size) { index ->
                        val color = presets[index]
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    2.dp, 
                                    if (currentColor == color) MaterialTheme.colorScheme.primary else Color.Transparent, 
                                    CircleShape
                                )
                                .clickable {
                                    currentColor = color
                                    onColorSelected(color)
                                    // Update HSV sliders to match preset
                                    val hsv = FloatArray(3)
                                    AndroidColor.colorToHSV(color.toArgb(), hsv)
                                    hue = hsv[0]
                                    saturation = hsv[1]
                                    value = hsv[2]
                                }
                        ) {
                            if (currentColor == color) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("Custom", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                
                // Color Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(currentColor)
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha=0.1f), RoundedCornerShape(8.dp))
                )

                // Sliders
                LabelledSlider(label = "Hue", value = hue, onValueChange = { hue = it; updateColorFromHSV() }, range = 0f..360f)
                LabelledSlider(label = "Saturation", value = saturation, onValueChange = { saturation = it; updateColorFromHSV() }, range = 0f..1f)
                LabelledSlider(label = "Brightness", value = value, onValueChange = { value = it; updateColorFromHSV() }, range = 0f..1f)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    )
}

@Composable
fun LabelledSlider(label: String, value: Float, onValueChange: (Float) -> Unit, range: ClosedFloatingPointRange<Float>) {
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.height(32.dp) // Compact height
        )
    }
}

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val serviceName = ComponentName(context, BlueLightFilterService::class.java)
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    return enabledServices.split(':').any { ComponentName.unflattenFromString(it) == serviceName }
}

fun openAccessibilitySettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    })
}