package com.rmf.mathbasicrecognition.ui.composeable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun ErrorDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        icon = {
            Icon(imageVector = Icons.Default.Warning, contentDescription = null)
        },
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Ups...")
        },
        text = {
            Text(
                text = message,
                fontSize = 16.sp,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Oke")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}