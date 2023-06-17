package com.rmf.mathbasicrecognition.presentation.home

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultRecipient
import com.rmf.mathbasicrecognition.domain.model.DataMathExpression
import com.rmf.mathbasicrecognition.presentation.destinations.CameraScreenDestination
import com.rmf.mathbasicrecognition.ui.composeable.ErrorDialog
import com.rmf.mathbasicrecognition.ui.composeable.LoadingDialog
import com.rmf.mathbasicrecognition.utils.evaluateMathExpression
import com.rmf.mathbasicrecognition.utils.exhaustive
import com.rmf.mathbasicrecognition.utils.extractMathExpression
import com.rmf.mathbasicrecognition.utils.formatMathResult

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@RootNavGraph(start = true)
@Composable
fun HomeScreen(
    navigator: DestinationsNavigator,
    resultRecipient: ResultRecipient<CameraScreenDestination, Uri>,
    viewModel: HomeViewModel = hiltViewModel()
) {

    val context = LocalContext.current

    val textRecognizer = remember {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    resultRecipient.onNavResult { result ->
        when (result) {
            NavResult.Canceled -> {}
            is NavResult.Value -> {
                recognizeTextFromImage(
                    context = context,
                    textRecognizer = textRecognizer,
                    imageUri = result.value,
                    onResult = {
                        viewModel.onEvent(HomeUIEvent.OnResult(it))
                    },
                    onError = {
                        viewModel.onEvent(HomeUIEvent.OnError(it))
                    },
                    onProcess = {
                        viewModel.onEvent(HomeUIEvent.OnProcess)
                    }
                )
            }
        }.exhaustive
    }

    val permission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE

        }


    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                recognizeTextFromImage(
                    context = context,
                    textRecognizer = textRecognizer,
                    imageUri = it,
                    onResult = { result -> viewModel.onEvent(HomeUIEvent.OnResult(result)) },
                    onError = { error -> viewModel.onEvent(HomeUIEvent.OnError(error)) },
                    onProcess = {
                        viewModel.onEvent(HomeUIEvent.OnProcess)
                    }
                )
            }
        }
    )


    val checkPermissionLauncherForGallery = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                photoPickerLauncher.launch("image/*")
            } else {
                //Show Dialog
            }
        }
    )
    Scaffold(
        bottomBar = {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                onClick = {
                    checkPermissionLauncherForGallery.launch(permission)
                    //navigator.navigate(CameraScreenDestination)
                }) {
                Text(text = "Ambil Gambar")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(it)
                .verticalScroll(rememberScrollState())
        ) {
            viewModel.state.list.forEachIndexed { index, data ->
                ItemMathExpression(number = index.plus(1), data = data)
                if (viewModel.state.list.size != 1) {
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f)
                    )
                }
            }
        }
    }

    if (viewModel.state.isLoading)
        LoadingDialog()

    viewModel.state.error?.let { message ->
        ErrorDialog(message = message) {
            viewModel.onEvent(HomeUIEvent.OnDismissDialog)
        }
    }
}


private fun recognizeTextFromImage(
    context: Context,
    textRecognizer: TextRecognizer,
    imageUri: Uri,
    onResult: (DataMathExpression) -> Unit,
    onError: (String) -> Unit,
    onProcess: () -> Unit,
) {
    try {
        val inputImage = InputImage.fromFilePath(context, imageUri)
        onProcess()
        textRecognizer.process(inputImage)
            .addOnSuccessListener { text ->
                val recognizeText = text.text

                Log.e("TAG", "recognizeTextFromImage: $recognizeText")
                val resultExtract = extractMathExpression(recognizeText)
                Log.e("TAG", "result Extract: $resultExtract")
                if (resultExtract != null) {
                    val result = evaluateMathExpression(resultExtract ?: "")
                    val resultFormat = formatMathResult(result)
                    Log.e("TAG", "result : $result, $resultFormat")
                    onResult(DataMathExpression(resultExtract, resultFormat))
                } else {
                    onError("Tidak bisa menemukan ekspresi matematika")
                }

            }
            .addOnFailureListener { e ->
                Log.e("TAG", "recognizeTextFromImage: $e")
                onError("Terjadi kesalahan pada saat proses, harap coba lagi")
            }
    } catch (e: Exception) {
        e.printStackTrace()
        onError("Terjadi kesalahan pada saat proses, harap coba lagi")

    }
}
