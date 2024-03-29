package com.example.gles3test

import android.content.Context
import android.opengl.GLES32
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.View
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer (context: Context): GLSurfaceView.Renderer {

    private lateinit var mTriangle: Triangle
    private lateinit var mTextMap: TexMap
    private lateinit var mTextMap2: TexMap2
    private lateinit var mTextMap3: TexMap3
    private lateinit var mTextMap4: TexMap4
    private lateinit var mTextMap5: TexMap5

    private lateinit var mMSDF: MSDF
    private lateinit var mMSDFOutline: MSDFOutline

    private lateinit var mSDF: SDF
    private lateinit var mSDFOutline: SDFOutline

    private lateinit var mTexBlink: TexBlink
    private lateinit var mTexBlink1: TexBlink1

    private lateinit var mRectangle: Rectangle
    private lateinit var mRadiation: Radiation
    private lateinit var mPentagon: Pentagon
    private lateinit var mCompShader: CompShader
    //private lateinit var mCompShader2: CompShader2
    //private lateinit var mCompShader3: CompShader3
    private lateinit var mCompShader4: CompShader4
    private lateinit var mLines: Lines
    private lateinit var mTestDraw: TestDraw


    private var context: Context
    //private var width:Int=0
    //private var height:Int=0
    private var viewSize = Vec2(0.0f,0.0f)
    private var startTime:Long = 0
    private var blinkStart = 2.0f
    private var blinkDuration = 1.0f
    private var blinkFadeTime = 0.2f

    init {
        this.context = context
    }

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        mTriangle = Triangle(context)
        mCompShader = CompShader(context,512,512,1, GLES32.GL_RGBA32F,GLES32.GL_RGBA,GLES32.GL_FLOAT,"shader.es30-3.computeshader")
        mTextMap = TexMap(context,null)
        mTextMap2 = TexMap2(context)
        mTextMap3 = TexMap3(context,R.drawable.msdf)
        mTextMap4 = TexMap4(context,R.drawable.lucky_yotsuba_clover_girl, 4.0f,1.0f)
        var ids = arrayOf(R.drawable.score_kira1_3x, R.drawable.score_kira2_3x, R.drawable.score_kira3_3x)
        mTextMap5 = TexMap5(context,ids)
        mPentagon = Pentagon(context)

        mTexBlink = TexBlink(context,R.drawable.lucky_yotsuba_clover_girl)
        mTexBlink1 = TexBlink1(context,R.drawable.lucky_yotsuba_clover_girl)

        mLines = Lines(context)
        mRectangle = Rectangle(context)
        mRadiation = Radiation(context)
        mTestDraw= TestDraw(context)

        mMSDF = MSDF(context,R.drawable.msdf)
        mMSDFOutline = MSDFOutline(context,R.drawable.msdf)
        mSDF = SDF(context,R.drawable.test)
        mSDFOutline = SDFOutline(context,R.drawable.test)

//        mCompShader2=CompShader2(context,"shader.es30-4.computeshader")
//        mCompShader2.execute()

//        mCompShader3=CompShader3(context,"shader.es30-5.computeshader")
//        mCompShader3.execute()
//        mCompShader4=CompShader4(context,"shader.es30-6.computeshader")
//        mCompShader4.execute()
        startTime = System.currentTimeMillis()

    }

    override fun onDrawFrame(unused: GL10) {

        var currTime = System.currentTimeMillis()
        var elapsedTime = (currTime - startTime)/1000.0f
        //Log.d("TEST",elapsedTime.toString())

        GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA);
        //GLES32.glBlendFunc(GLES32.GL_ONE, GLES32.GL_ONE);
        GLES32.glEnable(GLES32.GL_BLEND);
        //GLES32.glDisable(GLES32.GL_BLEND);
        // Redraw background color
        //GLES32.glClearColor(0.3f, 0.5f, 0.8f, 1.0f)
        //GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT)
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        //GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT or GLES32.GL_DEPTH_BUFFER_BIT)
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT)

        //mRadiation.draw(viewSize)
        //mTestDraw.draw(viewSize)
        //mTriangle.draw()
        //mTextMap.draw()

        //mCompShader.execute()
        //mTextMap.draw(mCompShader.texId)
        mTextMap.draw()

        //val viewSize = Vec2(width.toFloat(),height.toFloat())
        val texPos = Vec2(100.0f,400.0f)
        val texSize = Vec2(800.0f,200.0f)
        //mTextMap5.draw(viewSize,texSize, texPos)

        val texPos2 = Vec2(20.0f,20.0f)
        val texPos2_1 = Vec2(140.0f,20.0f)
        val texPos2_2 = Vec2(260.0f,20.0f)
        val texPos2_3 = Vec2(20.0f,140.0f)
        val texSize2 = Vec2(100.0f,100.0f)
        val texSize2_1 = Vec2(100.0f,100.0f)
        val texSize2_2 = Vec2(100.0f,100.0f)
        val texSize2_3 = Vec2(200.0f,200.0f)
        //mTextMap4.draw(viewSize,Vec2(253.0f,253.0f),texPos2_0)
        //GLES32.glDisable(GLES32.GL_BLEND);
        //mTextMap3.draw(viewSize,texSize2,texPos2)
        //GLES32.glEnable(GLES32.GL_BLEND);
        //val texPos2 = Vec2(10.0f,10.0f)

        mMSDF.draw(viewSize,texSize2,texPos2)
        mMSDFOutline.draw(viewSize,texSize2_1,texPos2_1)
        mSDF.draw(viewSize,texSize2_2,texPos2_2)

        mSDFOutline.draw(viewSize,texSize2_3,texPos2_3)

        //mTexBlink.draw(viewSize,texPos2,blinkStart,blinkDuration,elapsedTime,blinkFadeTime)
        //mTexBlink1.draw(viewSize,texPos2,blinkStart,blinkDuration,elapsedTime)
        //mRectangle.draw(width.toFloat(),height.toFloat(),width.toFloat(),height.toFloat(),0.0f,0.0f,100.0f)

        //val viewSize = Vec2(width.toFloat(),height.toFloat())
        //val rectSize = Vec2(800.0f,200.0f)
        //val rectPos = Vec2(100.0f,viewSize.y - 200.0f)
        val rectSize = Vec2(800.0f,200.0f)
        val rectPos = Vec2(0.0f,0.0f)
        val edge = 50.0f
        val color = Color(0.0f,1.0f,0.0f,1.0f)
        //mRectangle.draw(viewSize,rectSize,rectPos,edge,color)

        var scores = arrayOf(1.0f,1.0f,1.0f,1.0f,1.0f)
        val center = Vec2(viewSize.x/2.0f,viewSize.y - viewSize.x/2.0f)
        val color2 = Color(0.0f,1.0f,0.0f,1.0f)
        //mPentagon.draw(viewSize,center,viewSize.x/2.0f,scores,2.0f,color2)
        //mLines.draw()
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        viewSize.x = width.toFloat()
        viewSize.y = height.toFloat()
        Log.d("MyGLRenderer","surface width:%d height:%d".format(width,height))
        //Log.d("MyGLRenderer","view width:%d height:%d".format(view.width,view.height))
        GLES32.glViewport(0, 0, width, height)
    }
}