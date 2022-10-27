package jp.co.xing.gl.renderer

import android.content.Context
import android.opengl.GLES32
import android.opengl.GLSurfaceView
import android.util.Log
import jp.co.xing.gl.util.*
import jp.co.xing.pianorollviewtest.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PianoRollViewRenderer(context: Context): GLSurfaceView.Renderer {
    private var context: Context
    //private var width:Int=0
    //private var height:Int=0
    private var viewSize = Vec2(0.0f,0.0f)
    private var pianoRollSize = Vec2(0.0f,0.0f)
    private var offset = Vec2(0.0f,0.0f)
    private var rectWhole = Rect(0.0f,0.0f,0.0f,0.0f)
    private var rectPianoRoll = Rect(0.0f,0.0f,0.0f,0.0f)
    private lateinit var margins:Array<Float>
    private var sectionWidth: Float = 0.0f

    private val SECTION_MARGIN = 20.0f
    private val VERTICAL_LINE_WIDTH = 3.0f
    private val HORIZONTAL_LINE_HEIGHT = 3.0f

    private lateinit var texMapBK:TexMap0
    private lateinit var texMapSectionLineLeft:TexMap0
    private lateinit var texMapSectionLineRight:TexMap0
    private lateinit var outOfSectionL:Rectangle
    private lateinit var outOfSectionR:Rectangle

    private val COLOR_OUT_OF_SECTION = Color(0.0f,0.0f,0.0f,0.4f)

    init {
        this.context = context
    }
    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        // Set the background frame color
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        texMapBK = TexMap0(context, R.drawable.pianoroleback_3x)
        texMapSectionLineLeft = TexMap0(context, R.drawable.sectionbar_3x)
        texMapSectionLineRight = TexMap0(context, R.drawable.sectionbar_3x)
        outOfSectionL = Rectangle(context)
        outOfSectionR = Rectangle(context)
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        viewSize.x = width.toFloat()
        viewSize.y = height.toFloat()
        Log.d("PianoRollViewRenderer","surface width:%d height:%d".format(width,height))
        GLES32.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(p0: GL10?) {
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT or GLES32.GL_DEPTH_BUFFER_BIT)

        GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA)
        GLES32.glEnable(GLES32.GL_BLEND)

        pianoRollSize.x = viewSize.x
        pianoRollSize.y = viewSize.y * 0.72f

        margins = arrayOf(SECTION_MARGIN,viewSize.x-SECTION_MARGIN)
        sectionWidth = margins[1] - margins[0] - VERTICAL_LINE_WIDTH * 2.0f

        offset.x = margins[0] + VERTICAL_LINE_WIDTH
        offset.y = (viewSize.y - pianoRollSize.y)/2.0f

        rectWhole = Rect(0.0f,0.0f,viewSize.x,viewSize.y)
        rectPianoRoll = Rect(x=0.0f,
            y=offset.y + HORIZONTAL_LINE_HEIGHT,
            w=viewSize.x,
            h=pianoRollSize.y - HORIZONTAL_LINE_HEIGHT*2.0f)


        drawBackground()
        drawSectionLines()
        drawOutOfSections()
    }
    private fun drawBackground(){
        val pos = Vec2(0.0f,offset.y)
        texMapBK.draw(viewSize,pianoRollSize,pos)
    }
    private fun drawSectionLines(){
        val poss = arrayOf(Vec2(SECTION_MARGIN,offset.y), Vec2(viewSize.x-SECTION_MARGIN-VERTICAL_LINE_WIDTH,offset.y))
        val size = Vec2(VERTICAL_LINE_WIDTH,pianoRollSize.y)
        texMapSectionLineLeft.draw(viewSize,size,poss[0])
        texMapSectionLineLeft.draw(viewSize,size,poss[1])
    }
    private fun drawOutOfSections(){
        val poss = arrayOf(
            Vec2(0.0f,offset.y+HORIZONTAL_LINE_HEIGHT),
            Vec2(margins[1],offset.y+HORIZONTAL_LINE_HEIGHT)
        )
        val sizes = arrayOf(
            Vec2(margins[0],pianoRollSize.y-HORIZONTAL_LINE_HEIGHT*2.0f),
            Vec2(viewSize.x - margins[1],pianoRollSize.y-HORIZONTAL_LINE_HEIGHT*2.0f)
        )
        outOfSectionL.draw(viewSize,sizes[0],poss[0],0.0f,COLOR_OUT_OF_SECTION)
        outOfSectionL.draw(viewSize,sizes[1],poss[1],0.0f,COLOR_OUT_OF_SECTION)

    }
}