package com.example.smartlabelmagic

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SmartLabelScreen() }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SmartLabelScreen() {
    val ctx = LocalContext.current
    val classifier = remember { Classifier(ctx) }
    val scope = rememberCoroutineScope()

    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var label by remember { mutableStateOf("Pick a photo to classify") }
    var confidence by remember { mutableStateOf(0f) }
    var loading by remember { mutableStateOf(false) }

    val color by animateColorAsState(
        when {
            confidence >= 0.8f -> Color(0xFF2ECC71)
            confidence >= 0.5f -> Color(0xFFF1C40F)
            else -> Color(0xFF95A5A6)
        }, animationSpec = spring(), label = "color"
    )

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            loading = true
            label = "Analyzing…"
            scope.launch {
                val bmp = withContext(Dispatchers.IO) {
                    ctx.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it)
                    }
                }
                bitmap = bmp
                bmp?.let {
                    val res = withContext(Dispatchers.Default) { classifier.classify(it) }
                    confidence = res.confidence
                    label = "${prefix(confidence)} ${res.label} • ${(confidence * 100).toInt()}%"
                    loading = false
                } ?: run { label = "Could not load image"; loading = false }
            }
        }
    }

    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("✨ Smart Label v2", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(24.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(bitmap = bitmap!!.asImageBitmap(), contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(8.dp))
                } else Text("No image selected", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(12.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.15f))
                    .border(1.dp, color, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (loading) {
                    CircularProgressIndicator()
                } else {
                    Text(label, style = MaterialTheme.typography.titleSmall)
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(onClick = {
                pickImage.launch("image/*")
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Choose Photo")
            }
        }
    }
}

fun prefix(c: Float): String = when {
    c >= 0.8f -> "Definitely"
    c >= 0.5f -> "Probably"
    else -> "Maybe"
}
