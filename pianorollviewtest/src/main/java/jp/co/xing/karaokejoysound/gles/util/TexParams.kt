package jp.co.xing.karaokejoysound.gles.util

import android.opengl.GLES32

enum class MinFILTER(val v:Int){
    NEAREST(GLES32.GL_NEAREST),
    LINEAR(GLES32.GL_LINEAR)
}
enum class MagFILTER(val v:Int){
    NEAREST(GLES32.GL_NEAREST),
    LINEAR(GLES32.GL_LINEAR)
}
