package jp.co.xing.karaokejoysound.ui.karaoke.gles.util

import android.opengl.GLES32

enum class ColorComponents(val v: Int) {
    RGBA8(GLES32.GL_RGBA8),
    RGBA32F(GLES32.GL_RGBA32F)
}