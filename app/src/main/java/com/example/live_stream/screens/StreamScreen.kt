package com.example.live_stream.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.live_stream.viewmodel.StreamViewModel
import com.example.live_stream.models.StreamProtocol
import com.example.live_stream.models.BeautyFilter
import com.pedro.library.view.OpenGlView

@Composable
fun StreamScreen(
    viewModel: StreamViewModel,
    activityContext: Context
) {
    val isStreaming by viewModel.isStreaming.collectAsState()
    val statusText by viewModel.statusText.collectAsState()
    val streamUrl by viewModel.streamUrl.collectAsState()
    val selectedProtocol by viewModel.selectedProtocol.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                OpenGlView(ctx).apply {
                    post {
                        viewModel.setOpenGlView(this, ctx)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )

        // Filter Selection - Ngay dưới camera để dễ thấy effect
        Text("Filter làm đẹp:", style = MaterialTheme.typography.titleMedium)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(BeautyFilter.values()) { filter ->
                FilterChip(
                    onClick = { viewModel.selectFilter(filter) },
                    label = { Text(filter.displayName) },
                    selected = filter == selectedFilter,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        // Protocol Selection
        Text("Giao thức stream:", style = MaterialTheme.typography.titleMedium)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(StreamProtocol.values()) { protocol ->
                FilterChip(
                    onClick = { viewModel.selectProtocol(protocol) },
                    label = { Text(protocol.displayName) },
                    selected = protocol == selectedProtocol,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        // URL Input
        OutlinedTextField(
            value = streamUrl,
            onValueChange = { viewModel.updateStreamUrl(it) },
            label = { Text("URL Stream") },
            placeholder = { Text(selectedProtocol.example) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Control Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (isStreaming) viewModel.stopStreaming()
                    else viewModel.startStreaming(activityContext)
                },
                modifier = Modifier.weight(1f),
                enabled = streamUrl.isNotBlank()
            ) {
                Text(if (isStreaming) "Dừng Stream" else "Bắt đầu Stream")
            }

            Button(
                onClick = { viewModel.switchCamera() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Đổi camera")
            }
        }

        // Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = "Trạng thái: $statusText",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    // Lifecycle handling
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE) {
                if (isStreaming) viewModel.stopStreaming()
            } else if (event == androidx.lifecycle.Lifecycle.Event.ON_DESTROY) {
                viewModel.release()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}