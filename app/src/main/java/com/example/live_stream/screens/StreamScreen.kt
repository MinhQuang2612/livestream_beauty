package com.example.live_stream.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.live_stream.viewmodel.StreamViewModel
import com.pedro.library.view.OpenGlView

@Composable
fun StreamScreen(
    viewModel: StreamViewModel,
    activityContext: Context
) {
    val isStreaming by viewModel.isStreaming.collectAsState()
    val statusText by viewModel.statusText.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        AndroidView(
            factory = { ctx ->
                OpenGlView(ctx).also { viewModel.setOpenGlView(it, ctx) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = statusText)
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (isStreaming) viewModel.stopStreaming()
                else viewModel.startStreaming(activityContext)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isStreaming) "Dừng Stream" else "Bắt đầu Stream")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.switchCamera() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Đổi camera")
        }
    }

    // Dừng stream khi app pause
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE && isStreaming) {
                viewModel.stopStreaming()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}