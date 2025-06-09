package com.example.live_stream.models

import com.pedro.encoder.input.gl.render.filters.BaseFilterRender
import com.pedro.encoder.input.gl.render.filters.BeautyFilterRender
import com.pedro.encoder.input.gl.render.filters.BlurFilterRender
import com.pedro.encoder.input.gl.render.filters.BrightnessFilterRender
import com.pedro.encoder.input.gl.render.filters.ContrastFilterRender
import com.pedro.encoder.input.gl.render.filters.NoFilterRender
import com.pedro.encoder.input.gl.render.filters.SepiaFilterRender
import com.pedro.encoder.input.gl.render.filters.SaturationFilterRender

enum class BeautyFilter(
    val displayName: String,
    val description: String,
    val filterClass: Class<out BaseFilterRender>
) {
    NONE("Không filter", "Hiển thị gốc không có filter", NoFilterRender::class.java),
    BEAUTY("Beauty", "Làm mịn da, làm đẹp tự nhiên", BeautyFilterRender::class.java),
    BRIGHTNESS("Sáng", "Tăng độ sáng của hình ảnh", BrightnessFilterRender::class.java),
    BLUR("Mờ", "Làm mờ hình ảnh", BlurFilterRender::class.java),
    CONTRAST("Tương phản", "Tăng độ tương phản", ContrastFilterRender::class.java),
    SEPIA("Sepia", "Hiệu ứng vintage màu nâu", SepiaFilterRender::class.java),
    SATURATION("Độ bão hòa", "Tăng độ sống động màu sắc", SaturationFilterRender::class.java);

    fun createFilter(): BaseFilterRender {
        return try {
            filterClass.getDeclaredConstructor().newInstance()
        } catch (e: Exception) {
            NoFilterRender()
        }
    }
} 