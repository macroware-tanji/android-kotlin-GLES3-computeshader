package jp.co.xing.karaokejoysound.ui.karaoke.gles.renderer

import android.content.Context
import android.opengl.GLES32
import android.opengl.GLSurfaceView
import android.util.Log
import jp.co.xing.karaokejoysound.R
import jp.co.xing.karaokejoysound.ui.karaoke.gles.util.*
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.PI

class MedalAnimRenderer(val context: Context, val medalResId:Int, var enableAnimation:Boolean): GLSurfaceView.Renderer {
    companion object {
        val TAG = MedalAnimRenderer::class.simpleName
    }
    private var angles = floatArrayOf(
        0f,
        (36f / 180f * PI).toFloat(),
        (72 / 180 * PI).toFloat(),
        (120 / 180 * PI).toFloat(),
        (150 / 180 * PI).toFloat()
    )

    private var expRates = floatArrayOf(
        0.18f,
        0.12f,
        0.08f,
        0.10f,
        0.15f
    )

    private var viewSize = Vec2(0.0f,0.0f)
    private lateinit var medalWoAnim: MedalWoAnim
    private lateinit var texSize:Vec2
    private lateinit var texPos:Vec2

    private lateinit var starCircle:StarCircle
    private lateinit var starCircleTexSize:Vec2

    private lateinit var star:Star
    private lateinit var starTexSize:Vec2

    private lateinit var medal: MedalAnim
    private lateinit var medalTexSize:Vec2

    private var startTime:Long = 0
    private var nextCycleStart:Float = -1f

    private var started:Boolean=false

    fun startAnimation(){
        started=true
        startTime = Date().time
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        medalWoAnim = MedalWoAnim(context,medalResId)
        starCircle= StarCircle(context, R.drawable.star_circle)
        star = Star(context,R.drawable.star)
        medal = MedalAnim(context,medalResId,R.drawable.img_medal00_gray)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        viewSize.x = width.toFloat()
        viewSize.y = height.toFloat()
        Log.d(TAG,"onSurfaceChanged() width:%d height:%d".format(width,height))
        //Log.d("MyGLRenderer","view width:%d height:%d".format(view.width,view.height))
        GLES32.glViewport(0, 0, width, height)

        val rate = viewSize.x / viewSize.y
        val medalLen = if(rate > 1){
            viewSize.y * 44f/65f
        }
        else{
            viewSize.x * 44f/65f
        }
        texSize = Vec2(medalLen,medalLen)
        texPos = Vec2((viewSize.x - medalLen)/2, (viewSize.y - medalLen)/2)

        val circleLen = if(rate > 1){
            viewSize.y
        }
        else{
            viewSize.x
        }
        starCircleTexSize = Vec2(circleLen,circleLen)
        starTexSize = Vec2(circleLen,circleLen)

        startTime = Date().time

    }

    override fun onDrawFrame(gl: GL10?) {
        var currTime = Date().time
        var timeMSec = (currTime - startTime)
        var timeSec = timeMSec/1000f

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

        if(!started){
            return
        }

        if(enableAnimation){
            if(nextCycleStart <= timeSec){
                if(nextCycleStart < 0f) {
                    nextCycleStart = 4.0f
                } else {
                    startTime+=4000
                    timeMSec = (currTime - startTime)
                    timeSec = timeMSec/1000f
                    //nextCycleStart += 4.0f
                    enableAnimation = false
                }
                for(i in angles.indices){
                    var random = 180.0f * Math.random().toFloat() / angles.size.toFloat()
                    var deg = 180.0f / angles.size.toFloat() * i.toFloat() + random
                    var rad = deg/180.0f * PI.toFloat()
                    angles[i] = rad
                    Log.d(TAG,"angles=${angles[i]} expRates=${expRates[i]} time=${timeSec}")
                }
                expRates.shuffle()
            }
        }

        if(!enableAnimation){
            medalWoAnim.draw(viewSize,texSize)
        }
        else{
            medal.draw(viewSize,texSize,timeSec)
            //Log.d(TAG,time.toString())
//            texMap0.draw(viewSize,texSize,texPos)
            starCircle.draw(viewSize,starCircleTexSize,timeSec)

            for(i in angles.indices){
                star.draw(viewSize,starTexSize,expRates[i],angles[i],timeSec)
            }
        }
    }
}