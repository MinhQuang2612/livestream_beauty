package com.example.live_stream

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.example.live_stream.screens.StreamScreen
import com.example.live_stream.viewmodel.StreamViewModel

class MainActivity : ComponentActivity() {
    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.INTERNET
    )

    private val viewModel: StreamViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = requiredPermissions.all { permissions[it] == true }
        if (!allGranted) {
            Toast.makeText(this, "Cần cấp đủ quyền để sử dụng chức năng stream!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Xin quyền khi mở app
        permissionLauncher.launch(requiredPermissions)
        setContent {
            // Truyền context để ViewModel kiểm tra quyền khi cần
            StreamScreen(viewModel = viewModel, activityContext = this)
        }
    }
}