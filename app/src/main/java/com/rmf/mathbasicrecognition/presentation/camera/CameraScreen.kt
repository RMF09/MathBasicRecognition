package com.rmf.mathbasicrecognition.presentation.camera

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Range
import android.util.Size
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.rmf.mathbasicrecognition.R
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.result.ResultBackNavigator
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Destination
@Composable
fun CameraScreen(
    resultBackNavigator: ResultBackNavigator<Uri>
) {

    val activity = LocalContext.current as Activity
    val cameraExecutor = remember {
        Executors.newSingleThreadExecutor()
    }
    val outputDirectory = remember {
        activity.getOutputDirectory()
    }
    var shouldShowCamera by remember {
        mutableStateOf(false)
    }
    var shouldShowEV by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    ComposableLifecycle { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            cameraExecutor.shutdown()
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                shouldShowCamera = true
            }
        })

    LaunchedEffect(key1 = Unit) {
        activity.requestCameraPermission(
            shouldShowCamera = {
                shouldShowCamera = it
            },
            permissionLaunch = {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            })
    }

    CameraView(
        outputDirectory = outputDirectory,
        executor = cameraExecutor,
        onImageCaptured = {
            cameraExecutor.shutdown()
            scope.launch {
                resultBackNavigator.navigateBack(result = it)
            }
        },
        onError = {
            Toast.makeText(
                activity,
                "Terjadi masalah, harap coba lagi (${it.localizedMessage})",
                Toast.LENGTH_LONG
            ).show()
        }
    )
}

@SuppressLint("RestrictedApi")
@Composable
fun CameraView(
    outputDirectory: File,
    executor: Executor,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    // 1
    var lensFacing by remember {
        mutableStateOf(
            CameraSelector.LENS_FACING_BACK
        )
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val preview = Preview.Builder().setTargetResolution(Size(480, 640)).build()
    val previewView = remember { PreviewView(context) }
    val imageCapture: ImageCapture =
        remember { ImageCapture.Builder().setTargetResolution(Size(480, 640)).build() }
    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()


    var camera: Camera? = remember { null }
    var rangeBrigthness: Range<Int>? = remember { null }

    var min by remember {
        mutableStateOf(0f)
    }

    var max by remember {
        mutableStateOf(0f)
    }

    var shouldShowEV by remember {
        mutableStateOf(false)
    }

    var evValue by remember {
        mutableStateOf(0)
    }


    // 2
    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        camera = cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )
        evValue = 0

        rangeBrigthness = camera?.cameraInfo?.exposureState?.exposureCompensationRange
        min = rangeBrigthness!!.lower.toFloat()
        max = rangeBrigthness!!.upper.toFloat()

        preview.setSurfaceProvider(previewView.surfaceProvider)
    }


    // 3
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxSize()
            .background(color = Black)
    ) {
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())

        IconButton(
            modifier = Modifier.padding(bottom = 20.dp),
            onClick = {
                takePhoto(
                    filenameFormat = "yyyy-MM-dd-HH-mm-ss-SSS",
                    imageCapture = imageCapture,
                    outputDirectory = outputDirectory,
                    executor = executor,
                    onImageCaptured = onImageCaptured,
                    onError = onError
                )
            },
            content = {
                Icon(
                    imageVector = Icons.Sharp.Lens,
                    contentDescription = "Take picture",
                    tint = White,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(1.dp)
                        .border(1.dp, White, CircleShape)
                )
            }
        )

        IconButton(
            modifier = Modifier
                .padding(bottom = 20.dp, end = 20.dp)
                .align(Alignment.BottomEnd),
            onClick = {
                lensFacing =
                    if (lensFacing == CameraSelector.LENS_FACING_BACK)
                        CameraSelector.LENS_FACING_FRONT
                    else
                        CameraSelector.LENS_FACING_BACK
            },
            content = {
                Icon(
                    imageVector = Icons.Sharp.Cameraswitch,
                    contentDescription = "Switch Camera",
                    tint = White,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(1.dp)
                )
            }
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 8.dp, end = 8.dp, bottom = 76.dp)
                .background(color = Black.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = {
                        shouldShowEV = !shouldShowEV
                    }) {
                        Icon(
                            imageVector = Icons.Sharp.Exposure,
                            contentDescription = null,
                            tint = White
                        )
                    }
                }

                //if(shouldShowEV) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        val color = if (evValue == -2) White.copy(alpha = 0.4f) else Transparent
                        IconButton(onClick = {
                            evValue = -2
                            camera?.cameraControl?.setExposureCompensationIndex(min.toInt())
                            val evResult =
                                camera?.cameraInfo?.exposureState?.exposureCompensationStep!!.toFloat() * min.toInt()

                        }, colors = IconButtonDefaults.iconButtonColors(containerColor = color)) {
                            Icon(
                                imageVector = Icons.Sharp.ExposureNeg2,
                                contentDescription = null,
                                tint = White
                            )
                        }
                    }
                    item {
                        val color = if (evValue == -1) White.copy(alpha = 0.4f) else Transparent
                        IconButton(onClick = {
                            evValue = -1
                            camera?.cameraControl?.setExposureCompensationIndex(min.toInt() / 2)
                            val evResult =
                                camera?.cameraInfo?.exposureState?.exposureCompensationStep!!.toFloat() * (min.toInt() / 2)

                        }, colors = IconButtonDefaults.iconButtonColors(containerColor = color)) {
                            Icon(
                                imageVector = Icons.Sharp.ExposureNeg1,
                                contentDescription = null,
                                tint = White
                            )
                        }
                    }
                    item {
                        val color = if (evValue == 0) White.copy(alpha = 0.4f) else Transparent
                        IconButton(onClick = {
                            evValue = 0
                            camera?.cameraControl?.setExposureCompensationIndex(0)
                            val evResult =
                                camera?.cameraInfo?.exposureState?.exposureCompensationStep!!.toFloat() * 0

                        }, colors = IconButtonDefaults.iconButtonColors(containerColor = color)) {
                            Icon(
                                imageVector = Icons.Sharp.ExposureZero,
                                contentDescription = null,
                                tint = White
                            )
                        }
                    }
                    item {
                        val color = if (evValue == 1) White.copy(alpha = 0.4f) else Transparent
                        IconButton(onClick = {
                            evValue = 1
                            camera?.cameraControl?.setExposureCompensationIndex(max.toInt() / 2)
                            val evResult =
                                camera?.cameraInfo?.exposureState?.exposureCompensationStep!!.toFloat() * (max.toInt() / 2)

                        }, colors = IconButtonDefaults.iconButtonColors(containerColor = color)) {
                            Icon(
                                imageVector = Icons.Sharp.ExposurePlus1,
                                contentDescription = null,
                                tint = White
                            )
                        }
                    }
                    item {
                        val color = if (evValue == 2) White.copy(alpha = 0.4f) else Transparent
                        IconButton(onClick = {
                            evValue = 2
                            camera?.cameraControl?.setExposureCompensationIndex(max.toInt())
                            val evResult =
                                camera?.cameraInfo?.exposureState?.exposureCompensationStep!!.toFloat() * max.toInt()

                        }, colors = IconButtonDefaults.iconButtonColors(containerColor = color)) {
                            Icon(
                                imageVector = Icons.Sharp.ExposurePlus2,
                                contentDescription = null,
                                tint = White
                            )
                        }
                    }
                }
                //}


            }
        }

    }
}

fun Activity.requestCameraPermission(
    shouldShowCamera: (Boolean) -> Unit,
    permissionLaunch: () -> Unit
) {
    shouldShowCamera(false)
    when {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED -> {
            shouldShowCamera(true)
        }
        ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.CAMERA
        ) ->
            permissionLaunch()
        else ->
            permissionLaunch()

    }
}

private fun takePhoto(
    filenameFormat: String,
    imageCapture: ImageCapture,
    outputDirectory: File,
    executor: Executor,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val photoFile = File(
        outputDirectory,
        SimpleDateFormat(
            filenameFormat,
            Locale.getDefault()
        ).format(System.currentTimeMillis()) + ".jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
        override fun onError(exception: ImageCaptureException) {
            onError(exception)
        }

        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            val savedUri = Uri.fromFile(photoFile)
            onImageCaptured(savedUri)
        }
    })
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                continuation.resume(cameraProvider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }

private fun Context.getOutputDirectory(): File {
    this.filesDir
    val mediaDir = filesDir.also {
        File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
    }

    return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
}

@Composable
fun ComposableLifecycle(
    lifeCycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onEvent: (LifecycleOwner, Lifecycle.Event) -> Unit
) {
    DisposableEffect(lifeCycleOwner) {
        val observer = LifecycleEventObserver { source, event ->
            onEvent(source, event)
        }
        lifeCycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifeCycleOwner.lifecycle.removeObserver(observer)
        }
    }
}


