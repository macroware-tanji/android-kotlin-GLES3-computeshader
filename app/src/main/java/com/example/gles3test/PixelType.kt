package com.example.gles3test

import android.opengl.GLES32

enum class PixelType(val v: Int) {
    UNSIGNED_BYTE(GLES32.GL_UNSIGNED_BYTE),
    FLOAT(GLES32.GL_FLOAT)
}