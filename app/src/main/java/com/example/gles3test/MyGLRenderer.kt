package com.example.gles3test

import android.content.Context
import android.opengl.GLES32
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer (context: Context): GLSurfaceView.Renderer {

    private lateinit var mTriangle: Triangle
    private lateinit var mTextMap: TexMap
    private lateinit var mTextMap2: TexMap2
    private lateinit var mTextMap3: TexMap3
    private lateinit var mCompShader: CompShader
    //private lateinit var mCompShader2: CompShader2
    //private lateinit var mCompShader3: CompShader3
    private lateinit var mCompShader4: CompShader4
    private var context: Context
    private var width:Int=0
    private var height:Int=0

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
        mTextMap3 = TexMap3(context)
//        mCompShader2=CompShader2(context,"shader.es30-4.computeshader")
//        mCompShader2.execute()

//        mCompShader3=CompShader3(context,"shader.es30-5.computeshader")
//        mCompShader3.execute()
//        mCompShader4=CompShader4(context,"shader.es30-6.computeshader")
//        mCompShader4.execute()
    }

    override fun onDrawFrame(unused: GL10) {
        // Redraw background color
        //GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT)
        GLES32.glClearColor(0.3f, 0.5f, 0.8f, 1.0f)
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT or GLES32.GL_DEPTH_BUFFER_BIT)

        //mTriangle.draw()
        //mTextMap.draw()

        //mCompShader.execute()
        //mTextMap.draw(mCompShader.texId)

        mTextMap3.draw(width.toFloat(),height.toFloat())
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        this.width = width
        this.height = height
        GLES32.glViewport(0, 0, width, height)
    }
}