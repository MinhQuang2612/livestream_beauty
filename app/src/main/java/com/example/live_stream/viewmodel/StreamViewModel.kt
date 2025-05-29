package com.example.live_stream.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.live_stream.rtmp.RtmpCameraManager
import com.pedro.library.view.OpenGlView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StreamViewModel : ViewModel() {
    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming

    private val _statusText = MutableStateFlow("Chưa kết nối")
    val statusText: StateFlow<String> = _statusText

    private var rtmpManager: RtmpCameraManager? = null
    private var openGlView: OpenGlView? = null

    fun setOpenGlView(openGlView: OpenGlView, context: Context) {
        this.openGlView = openGlView
        if (rtmpManager == null) {
            rtmpManager = RtmpCameraManager(context) { status, streaming ->
                viewModelScope.launch {
                    _statusText.value = status
                    _isStreaming.value = streaming
                }
            }
        }
        rtmpManager?.setOpenGlView(openGlView)
    }

    fun startStreaming(context: Context) {
        val hasAudio = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        val hasCamera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (!hasAudio || !hasCamera) {
            viewModelScope.launch {
                _statusText.value = "Bạn chưa cấp quyền Camera/Microphone!"
            }
            return
        }
        if (rtmpManager == null || openGlView == null) {
            viewModelScope.launch {
                _statusText.value = "Chưa khởi tạo camera!"
            }
            return
        }
        val ok = rtmpManager?.startStream("rtmp://192.168.70.36/live/test") ?: false
        if (!ok) {
            viewModelScope.launch {
                _statusText.value = "Lỗi chuẩn bị stream!"
            }
        }
    }

    fun stopStreaming() {
        rtmpManager?.stopStream()
        _isStreaming.value = false
        _statusText.value = "Đã dừng stream"
    }

    fun switchCamera() {
        rtmpManager?.switchCamera()
    }
}