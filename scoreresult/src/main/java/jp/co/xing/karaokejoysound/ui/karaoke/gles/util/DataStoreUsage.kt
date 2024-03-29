package jp.co.xing.karaokejoysound.ui.karaoke.gles.util
import android.opengl.GLES32
enum class DataStoreUsage(val v:Int) {
    STREAM_DRAW(GLES32.GL_STREAM_DRAW),
    STREAM_READ(GLES32.GL_STREAM_READ),
    STREAM_COPY(GLES32.GL_STREAM_COPY),
    STATIC_DRAW(GLES32.GL_STATIC_DRAW),
    STATIC_READ(GLES32.GL_STATIC_READ),
    STATIC_COPY(GLES32.GL_STATIC_COPY),
    DYNAMIC_DRAW(GLES32.GL_DYNAMIC_DRAW),
    DYNAMIC_READ(GLES32.GL_DYNAMIC_READ),
    DYNAMIC_COPY(GLES32.GL_DYNAMIC_COPY),
}