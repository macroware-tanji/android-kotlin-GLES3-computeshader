package jp.co.xing.karaokejoysound.ui.karaoke.gles.renderer

import android.content.Context
import android.opengl.GLES32
import android.opengl.GLSurfaceView
import android.util.Log
import jp.co.xing.karaokejoysound.R
import jp.co.xing.karaokejoysound.ui.karaoke.gles.util.TexMap5
import jp.co.xing.karaokejoysound.ui.karaoke.gles.util.Vec2
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class KiraRenderer(context: Context): GLSurfaceView.Renderer  {
    private val TAG = "KiraRenderer"
    private var context: Context
    private var viewSize = Vec2(0.0f,0.0f)
    private lateinit var mTextMap5: TexMap5

    init {
        this.context = context
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        var ids = arrayOf(R.drawable.score_kira1, R.drawable.score_kira2, R.drawable.score_kira3)
        mTextMap5 = TexMap5(context,ids)
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        viewSize.x = width.toFloat()
        viewSize.y = height.toFloat()
        Log.d(TAG,"onSurfaceChanged() width:%d height:%d".format(width,height))
        //Log.d("MyGLRenderer","view width:%d height:%d".format(view.width,view.height))
        GLES32.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(p0: GL10?) {
        GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA);
        GLES32.glEnable(GLES32.GL_BLEND);
        // Redraw background color
        //GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT)
        //GLES32.glClearColor(0.3f, 0.5f, 0.8f, 1.0f)
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT or GLES32.GL_DEPTH_BUFFER_BIT)
        val texPos = Vec2(0.0f,0.0f)
        mTextMap5.draw(viewSize,texPos)
    }
}