package jp.co.xing.karaokejoysound.ui.karaoke.gles.util

import android.opengl.GLES32

enum class PixelType(val v: Int) {
    UNSIGNED_BYTE(GLES32.GL_UNSIGNED_BYTE),
    FLOAT(GLES32.GL_FLOAT)
}