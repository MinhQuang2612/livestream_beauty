package com.example.live_stream.rtmp

import android.content.Context
import com.pedro.library.rtmp.RtmpCamera2
import com.pedro.common.ConnectChecker
import com.pedro.library.view.OpenGlView

class RtmpCameraManager(
    private val context: Context,
    private val onStatusChanged: (String, Boolean) -> Unit
) : ConnectChecker {

    private var rtmpCamera: RtmpCamera2? = null

    fun setOpenGlView(openGlView: OpenGlView) {
        rtmpCamera = RtmpCamera2(openGlView, this)
    }

    fun startStream(rtmpUrl: String): Boolean {
        rtmpCamera?.let {
            if (!it.isStreaming && it.prepareAudio() && it.prepareVideo()) {
                it.startStream(rtmpUrl)
                onStatusChanged("Đang kết nối...", false)
                return true
            } else {
                onStatusChanged("Lỗi chuẩn bị stream!", false)
            }
        }
        return false
    }

    fun stopStream() {
        rtmpCamera?.let {
            if (it.isStreaming) it.stopStream()
        }
    }

    fun switchCamera() {
        rtmpCamera?.switchCamera()
    }

    // ConnectChecker callbacks
    override fun onConnectionStarted(url: String) {
        onStatusChanged("Đang kết nối...", false)
    }

    override fun onConnectionSuccess() {
        onStatusChanged("Đã kết nối thành công!", true)
    }

    override fun onConnectionFailed(reason: String) {
        stopStream()
        onStatusChanged("Lỗi kết nối: $reason", false)
    }

    override fun onDisconnect() {
        onStatusChanged("Đã ngắt kết nối", false)
    }

    override fun onAuthError() {
        onStatusChanged("Lỗi xác thực RTMP", false)
    }

    override fun onAuthSuccess() {
        onStatusChanged("Xác thực RTMP thành công", true)
    }
}