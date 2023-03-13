package jp.co.xing.karaokejoysound.ui.karaoke.gles.renderer

import android.content.Context
import android.opengl.GLES32
import android.opengl.GLSurfaceView
import android.util.Log
import jp.co.xing.karaokejoysound.ui.karaoke.gles.util.Color
import jp.co.xing.karaokejoysound.ui.karaoke.gles.util.Pentagon
import jp.co.xing.karaokejoysound.ui.karaoke.gles.util.Vec2
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PentagonRenderer(context: Context): GLSurfaceView.Renderer {
    interface Listner {
        fun onEndAnimation()
    }
    var listner: Listner? = null

    private val TAG = "PentagonRenderer"
    private var context: Context
    private var viewSize = Vec2(0.0f,0.0f)
    private lateinit var mPentagon: Pentagon
    private var scores = arrayOf(1.0f,1.0f,1.0f,1.0f,1.0f)
    private var startCalled=false
    private var started=false
    private var inited=false

    init {
        this.context = context
    }

    fun setScore(pitch:Float, technique:Float, longtone:Float,intonation:Float, stability:Float){
        scores = arrayOf(pitch/40f,technique/5f,longtone/10f,intonation/15f,stability/30f)
    }

    fun start(){
        startCalled = true
        if(inited){
            if(!started){
                mPentagon.start()
                started=true
            }
        }
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        mPentagon = Pentagon(context)
        inited=true
        if(this.listner!=null){
            mPentagon.listner = object: Pentagon.Listner {
                override fun onEndAnimation() {
                    listner!!.onEndAnimation()
                }
            }
        }
        if(startCalled && !started){
            mPentagon.start()
            started=true
        }
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

        //var scores = arrayOf(1.0f,1.0f,1.0f,1.0f,1.0f)
        val center = Vec2(viewSize.x/2.0f,viewSize.y/2.0f)
        val color2 = Color(0x02.toFloat()/0xff.toFloat(),1.0f,0xc2.toFloat()/0xff.toFloat(),0.6f)

        mPentagon.draw(viewSize,center,viewSize.x/2.0f,scores,2.0f,color2)
    }
}