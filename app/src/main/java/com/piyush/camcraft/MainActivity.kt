package com.piyush.camcraft

import android.Manifest
import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.piyush.camcraft.ui.theme.CamCraftTheme
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CamCraftTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CamCraftApp()
                }
            }
        }
    }
}

// 1. Define our screens
enum class Screen {
    Home, Camera, Gallery
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CamCraftApp() {
    // 2. Track the current screen state
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // 3. Simple navigation logic
    when (currentScreen) {
        Screen.Home -> HomeScreen(
            onNavigate = { screen ->
                if (screen == Screen.Camera && !cameraPermissionState.status.isGranted) {
                    cameraPermissionState.launchPermissionRequest()
                } else {
                    currentScreen = screen
                }
            }
        )
        Screen.Camera -> {
            if (cameraPermissionState.status.isGranted) {
                // Pass a callback to go back to the home screen
                CameraPreviewScreen(onBack = { currentScreen = Screen.Home })
            }
        }
        Screen.Gallery -> GalleryScreen(onBack = { currentScreen = Screen.Home })
    }
}

@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📸 CamCraft", style = MaterialTheme.typography.displayMedium)
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onNavigate(Screen.Camera) },
            modifier = Modifier.fillMaxWidth(0.6f).height(50.dp)
        ) {
            Text("Open Camera")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { onNavigate(Screen.Gallery) },
            modifier = Modifier.fillMaxWidth(0.6f).height(50.dp)
        ) {
            Text("Saved Photos")
        }
    }
}

@Composable
fun GalleryScreen(viewModel: GalleryViewModel = viewModel(), onBack: () -> Unit) {
    val images by viewModel.images.collectAsState()

    // Load images when this screen opens
    LaunchedEffect(Unit) {
        viewModel.loadImages()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Saved Photos", style = MaterialTheme.typography.titleLarge)
        }

        if (images.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No photos yet! Go snap some. 📷")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(images) { image ->
                    // Coil handles loading the Image URI in the background
                    AsyncImage(
                        model = image.uri,
                        contentDescription = "Captured Photo",
                        modifier = Modifier
                            .padding(4.dp)
                            .aspectRatio(1f)
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
fun CameraPreviewScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx -> androidx.camera.view.PreviewView(ctx) },
            update = { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner, cameraSelector, preview, imageCapture
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        )

        // Back button for Camera
        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        Row(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
                },
                modifier = Modifier.size(56.dp).background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(Icons.Default.Cameraswitch, contentDescription = "Switch Camera", tint = Color.White)
            }

            IconButton(
                onClick = { takePhoto(imageCapture, context) },
                modifier = Modifier.size(80.dp).background(Color.White, CircleShape)
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = "Take Photo", tint = Color.Black, modifier = Modifier.size(40.dp))
            }
        }
    }
}

private fun takePhoto(imageCapture: ImageCapture, context: android.content.Context) {
    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CamCraft")
        }
    }

    val outputOptions = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
    ).build()

    imageCapture.takePicture(
        outputOptions, ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Toast.makeText(context, "Capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
            }
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                Toast.makeText(context, "Saved to Pictures/CamCraft!", Toast.LENGTH_SHORT).show()
            }
        }
    )
}