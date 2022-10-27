package jp.co.xing.gl.util

import android.opengl.GLES32

enum class PixelFormat(val v: Int) {
    RGB(GLES32.GL_RGB),
    RGBA(GLES32.GL_RGBA),
}