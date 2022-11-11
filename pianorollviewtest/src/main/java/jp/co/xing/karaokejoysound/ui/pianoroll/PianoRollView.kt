package jp.co.xing.karaokejoysound.ui.pianoroll

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import jp.co.xing.karaokejoysound.gles.renderer.PianoRollViewRenderer

/**
 * TODO: document your custom view class.
 */
class PianoRollView : GLSurfaceView {

    private lateinit var pianoRollViewRenderer:PianoRollViewRenderer

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs) {
        init(attrs, 0)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
//        setEGLContextClientVersion(3)
//        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
//        getHolder().setFormat(PixelFormat.TRANSLUCENT);
//        setZOrderOnTop(true)
//        pianoRollViewRenderer = PianoRollViewRenderer(getContext())
//        pianoRollViewRenderer.graderInfo = graderInfo
//        pianoRollViewRenderer.playTime = 0.0f
//        // Set the Renderer for drawing on the GLSurfaceView
//        setRenderer(pianoRollViewRenderer)
   }
}