package com.jimmypiedrahita.accesibilidad

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FilterControlScreen()
                }
            }
        }
    }
}

@Composable
fun FilterControlScreen() {
    val context = LocalContext.current
    val (isAccessibilityEnabled, setAccessibilityEnabled) = remember {
        mutableStateOf(isAccessibilityServiceEnabled(context))
    }
    val (isFilterActive, setFilterActive) = remember {
        mutableStateOf(BlueLightFilterService.isFilterActive())
    }

    val (intensity, setIntensity) = remember {
        mutableIntStateOf(BlueLightFilterService.getCurrentIntensity())
    }

    // Check the status each time the screen gains focus.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                setAccessibilityEnabled(isAccessibilityServiceEnabled(context))
                setFilterActive(BlueLightFilterService.isFilterActive())
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isAccessibilityEnabled) {
            // State 1: Accessibility permit not granted
            Text("Blue Light Filter", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            Text("Please enable accessibility permission to continue")
            Spacer(Modifier.height(24.dp))
            Button(onClick = { openAccessibilitySettings(context) }) {
                Text("Enable Accessibility")
            }
        } else if (!isFilterActive) {
            // State 2: Permission granted but filter inactive
            Text("Ready to Filter", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                BlueLightFilterService.toggleFilter(true)
                setFilterActive(true)
            }) {
                Text("Activate Filter")
            }
        } else {
            // State 3: Active filter
            Text("Filter Active", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            // Intensity slider
            Text("Intensity: $intensity%")
            Spacer(Modifier.height(8.dp))
            Slider(
                value = intensity.toFloat(),
                onValueChange = { newValue ->
                    val newIntensity = newValue.toInt()
                    setIntensity(newIntensity)
                    BlueLightFilterService.setIntensity(newIntensity)
                },
                valueRange = 0f..100f,
                steps = 9,
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(Modifier.height(24.dp))
            Button(onClick = {
                BlueLightFilterService.toggleFilter(false)
                setFilterActive(false)
            }) {
                Text("Deactivate Filter")
            }
        }
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