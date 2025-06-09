package com.example.live_stream.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedro.common.ConnectChecker
import com.pedro.library.generic.GenericCamera2
import com.pedro.library.view.OpenGlView
import com.example.live_stream.models.BeautyFilter
import com.example.live_stream.models.StreamProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StreamViewModel : ViewModel(), ConnectChecker {
    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming

    private val _statusText = MutableStateFlow("Chưa kết nối")
    val statusText: StateFlow<String> = _statusText

    private val _streamUrl = MutableStateFlow("")
    val streamUrl: StateFlow<String> = _streamUrl

    private val _selectedProtocol = MutableStateFlow(StreamProtocol.RTMP)
    val selectedProtocol: StateFlow<StreamProtocol> = _selectedProtocol

    private val _selectedFilter = MutableStateFlow(BeautyFilter.NONE)
    val selectedFilter: StateFlow<BeautyFilter> = _selectedFilter

    private var genericCamera: GenericCamera2? = null
    private var openGlView: OpenGlView? = null

    fun setOpenGlView(openGlView: OpenGlView, context: Context) {
        this.openGlView = openGlView
        if (genericCamera == null) {
            genericCamera = GenericCamera2(openGlView, this)
        }
    }

    fun selectProtocol(protocol: StreamProtocol) {
        _selectedProtocol.value = protocol
        _streamUrl.value = protocol.example
    }

    fun updateStreamUrl(url: String) {
        _streamUrl.value = url
    }

    fun selectFilter(filter: BeautyFilter) {
        _selectedFilter.value = filter
        openGlView?.setFilter(filter.createFilter())
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
        
        if (genericCamera == null || openGlView == null) {
            viewModelScope.launch {
                _statusText.value = "Chưa khởi tạo camera!"
            }
            return
        }

        val streamUrl = _streamUrl.value
        if (streamUrl.isBlank()) {
            viewModelScope.launch {
                _statusText.value = "Vui lòng nhập URL stream!"
            }
            return
        }

        val prepared = genericCamera?.prepareAudio() == true && genericCamera?.prepareVideo() == true
        if (!prepared) {
            viewModelScope.launch {
                _statusText.value = "Lỗi chuẩn bị stream!"
            }
            return
        }

        genericCamera?.startStream(streamUrl)
        viewModelScope.launch {
            _statusText.value = "Đang kết nối..."
        }
    }

    fun stopStreaming() {
        genericCamera?.stopStream()
        _isStreaming.value = false
        _statusText.value = "Đã dừng stream"
    }

    fun switchCamera() {
        genericCamera?.switchCamera()
    }

    fun release() {
        stopStreaming()
        genericCamera?.stopPreview()
    }

    // ConnectChecker callbacks
    override fun onConnectionStarted(url: String) {
        viewModelScope.launch {
            _statusText.value = "Đang kết nối..."
        }
    }

    override fun onConnectionSuccess() {
        viewModelScope.launch {
            _statusText.value = "Đã kết nối thành công!"
            _isStreaming.value = true
        }
    }

    override fun onConnectionFailed(reason: String) {
        viewModelScope.launch {
            _statusText.value = "Lỗi kết nối: $reason"
            _isStreaming.value = false
        }
        genericCamera?.stopStream()
    }

    override fun onDisconnect() {
        viewModelScope.launch {
            _statusText.value = "Đã ngắt kết nối"
            _isStreaming.value = false
        }
    }

    override fun onAuthError() {
        viewModelScope.launch {
            _statusText.value = "Lỗi xác thực"
            _isStreaming.value = false
        }
        genericCamera?.stopStream()
    }

    override fun onAuthSuccess() {
        viewModelScope.launch {
            _statusText.value = "Xác thực thành công"
        }
    }

    override fun onNewBitrate(bitrate: Long) {
        // Optional: Handle bitrate changes
    }
}