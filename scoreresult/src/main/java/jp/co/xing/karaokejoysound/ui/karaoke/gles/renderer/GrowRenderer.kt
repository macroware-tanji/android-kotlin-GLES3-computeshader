package jp.co.xing.karaokejoysound.ui.karaoke.gles.renderer

import android.content.Context
import android.opengl.GLES32
import android.opengl.GLSurfaceView
import android.util.Log
import jp.co.xing.karaokejoysound.R
import jp.co.xing.karaokejoysound.ui.karaoke.gles.util.TexMap4
import jp.co.xing.karaokejoysound.ui.karaoke.gles.util.Vec2
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GrowRenderer(context: Context): GLSurfaceView.Renderer {
    private val TAG = "GrowRenderer"
    private var context: Context
    private var viewSize = Vec2(0.0f,0.0f)
    private lateinit var mTextMap4_1: TexMap4
    private lateinit var mTextMap4_2: TexMap4

    init {
        this.context = context
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        mTextMap4_1 = TexMap4(context, R.drawable.img_glow1, 5.0f,1.0f)
        mTextMap4_2 = TexMap4(context,R.drawable.img_glow2, 4.0f,-1.0f)

    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        viewSize.x = width.toFloat()
        viewSize.y = height.toFloat()
        Log.d(TAG,"onSurfaceChanged() width:%d height:%d".format(width,height))
        //Log.d("MyGLRenderer","view width:%d height:%d".format(view.width,view.height))
        GLES32.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(p0: GL10?) {
        /*
        アルファブレンド
glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

反転
glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_ZERO);

加算
glBlendFunc(GL_ONE, GL_ONE);

加算+アルファ(PhotoShop的スクリーン)
glBlendFunc(GL_SRC_ALPHA, GL_ONE);

スクリーン(PhotoShop的 比較(明))
glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_ONE);

乗算
glBlendFunc(GL_ZERO, GL_SRC_COLOR);

乗算+アルファ
dst = dst * src * alpha
= (dst * src) * alpha
glBlendFunc(GL_ZERO, GL_SRC_COLOR);
glBlendFunc(GL_ZERO, GL_SRC_ALPHA);


         */
        //GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA);
        GLES32.glBlendFunc(GLES32.GL_ONE, GLES32.GL_ONE);
        //GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE);
        //GLES32.glBlendFunc(GLES32.GL_ONE_MINUS_DST_COLOR, GLES32.GL_ONE);
        //GLES32.glBlendFunc(GLES32.GL_ZERO, GLES32.GL_SRC_COLOR);
        //GLES32.glBlendFunc(GLES32.GL_ZERO, GLES32.GL_SRC_COLOR);
        //GLES32.glBlendFunc(GLES32.GL_ZERO, GLES32.GL_SRC_ALPHA);
        GLES32.glEnable(GLES32.GL_BLEND);
        // Redraw background color
        //GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT)
        //GLES32.glClearColor(0.3f, 0.5f, 0.8f, 1.0f)
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT or GLES32.GL_DEPTH_BUFFER_BIT)
        val texPos2 = Vec2(viewSize.x/2.0f,viewSize.y/2.0f)
        mTextMap4_1.draw(viewSize,texPos2)
        mTextMap4_2.draw(viewSize,texPos2)
    }
}