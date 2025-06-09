package com.example.live_stream.models

enum class StreamProtocol(
    val displayName: String,
    val example: String
) {
    RTMP("RTMP", "rtmp://192.168.1.100:1935/live/test"),
    RTSP("RTSP", "rtsp://192.168.1.100:8554/test"),
    SRT("SRT", "srt://192.168.1.100:9998"),
    UDP("UDP", "udp://192.168.1.100:1234")
} 