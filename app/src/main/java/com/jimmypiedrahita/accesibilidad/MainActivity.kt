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
    val isEnabled = remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }
    val serviceIntent = remember { Intent(context, BlueLightFilterService::class.java) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isEnabled.value) {
            Text("Activated filter", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                context.stopService(serviceIntent) }) {
                Text("Deactivate filter")
            }
        } else {
            Text("Blue light filter", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            Text("You need to enable the accessibility permission")
            Spacer(Modifier.height(24.dp))
            Button(onClick = { openAccessibilitySettings(context) }) {
                Text("Activate service")
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