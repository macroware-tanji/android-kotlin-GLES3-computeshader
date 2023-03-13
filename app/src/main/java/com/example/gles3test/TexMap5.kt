package com.example.gles3test

//import android.R
import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES32
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import java.util.*


private const val COORDS_PER_VERTEX = 3
private var rectCoords = floatArrayOf(
    // in counterclockwise order:
    +1.0f, +1.0f, 0.0f,      // #0: Upper right
    -1.0f, +1.0f, 0.0f,      // #1: Upper left
    -1.0f, -1.0f, 0.0f,      // #2: Lower left
    +1.0f, -1.0f, 0.0f,      // #3: Lower right
)

private const val UV_COORDS_PER_VERTEX = 2
private var uvCoords = floatArrayOf(
    // in counterclockwise order:
    1.0f, 0.0f,                // #3: Lower right
    0.0f, 0.0f,               // #2: Lower left
    0.0f, 1.0f,               // #1: Upper left
    1.0f, 1.0f,               // #0: Upper right
)
private var indexes = shortArrayOf(
    0,1,2,
    0,2,3
)

class TexMap5(context: Context, drawableIds:Array<Int>) {


//    private val vertexShaderCode =
//        "attribute vec4 vPosition;" +
//                "void main() {" +
//                "  gl_Position = vPosition;" +
//                "}"
//
//    private val fragmentShaderCode =
//        "precision mediump float;" +
//                "uniform vec4 vColor;" +
//                "void main() {" +
//                "  gl_FragColor = vColor;" +
//                "}"


    // Set color with red, green, blue and alpha (opacity) values
    //val color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)

    private var vertexBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(rectCoords.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(rectCoords)
                // set the buffer to read the first coordinate
                position(0)
            }
        }
    private var uvBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(uvCoords.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(uvCoords)
                // set the buffer to read the first coordinate
                position(0)
            }
        }

    private var indexBuffer: ShortBuffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(indexes.size * 2).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asShortBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(indexes)
                // set the buffer to read the first coordinate
                position(0)
            }
        }

//    fun loadShader(type: Int, shaderCode: String): Int {
//
//        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
//        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
//        return GLES32.glCreateShader(type).also { shader ->
//
//            // add the source code to the shader and compile it
//            GLES32.glShaderSource(shader, shaderCode)
//            GLES32.glCompileShader(shader)
//        }
//    }
    private fun loadShaderFromAssets(type: Int, shaderFileName: String): Int {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        return GLES32.glCreateShader(type).also { shader ->

            val shaderCode = context.getAssets().open(shaderFileName).reader(charset=Charsets.UTF_8).use{it.readText()}
            // add the source code to the shader and compile it
            GLES32.glShaderSource(shader, shaderCode)
            GLES32.glCompileShader(shader)
        }
    }

    private var mProgram: Int
    private var context:Context;
    //private var tex = IntBuffer.allocate(1)

    //private var tex = Tex2D()
    private var ibo = IndexBufferObject()// IntBuffer.allocate(1)
    private var vboVertex = VertexBufferObject()//IntBuffer.allocate(1)
    private var vboUV = VertexBufferObject()//IntBuffer.allocate(1)

    private var uboInfo: UniformBufferObject = UniformBufferObject()
    private var uboInfo2: UniformBufferObject = UniformBufferObject()
    //private var ssboDims: ShaderStrageBufferObject = ShaderStrageBufferObject()

    private var bmpSizes: Array<Vec2>
    private var texs: Array<Tex2D>

    //private var bmpWidth:Int=0
    //private var bmpHeight:Int=0
    private var vao = VertexArrayObject()
    private var vertInfo = FloatBuffer.allocate(6)
    private var fragInfo = FloatBuffer.allocate(2)

    private var texIndex = 0
    private var texSize = Vec2(0.0f,0.0f)
    private var period = 0.2f
    private var startTime = Date().time
    private var periods = arrayOf(0.2f,0.3f,0.4f,0.5f,0.6f,0.7f,0.8f)

    init {
        this.context = context
        val vertexShader: Int = loadShaderFromAssets(GLES32.GL_VERTEX_SHADER, "texmap-2.vertexshader")
        val fragmentShader: Int = loadShaderFromAssets(GLES32.GL_FRAGMENT_SHADER, "texmap-2.fragmentshader")

        // create empty OpenGL ES Program
        mProgram = GLES32.glCreateProgram().also {

            // add the vertex shader to program
            GLES32.glAttachShader(it, vertexShader)

            // add the fragment shader to program
            GLES32.glAttachShader(it, fragmentShader)

            // creates OpenGL ES program executables
            GLES32.glLinkProgram(it)
        }
        vboVertex.gen()
        vboVertex.bind()
        vboVertex.bufferData(rectCoords.size * 4,vertexBuffer,DataStoreUsage.STATIC_DRAW)
        vboVertex.unbind()

        vboUV.gen()
        vboUV.bind()
        vboUV.bufferData(uvCoords.size * 4,uvBuffer,DataStoreUsage.STATIC_DRAW)
        vboUV.unbind()

        ibo.gen()
        ibo.bind()
        ibo.bufferData(indexes.size * 2,indexBuffer,DataStoreUsage.STATIC_DRAW)
        ibo.unbind()

        vao.gen()
        vao.bind()

        vboVertex.bind()
        vboVertex.enable(0)
        vboVertex.pointer(COORDS_PER_VERTEX,DataType.FLOAT,false,0,0)
        vboVertex.unbind()

        vboUV.bind()
        vboUV.enable(1)
        vboUV.pointer(UV_COORDS_PER_VERTEX,DataType.FLOAT,false,0,0)
        vboUV.unbind()

        ibo.bind()

        vao.unbind()
        ibo.unbind()

        val options = BitmapFactory.Options()
        options.inScaled = false //密度によるサイズ変更をキャンセル

        bmpSizes = Array(drawableIds.size){Vec2(0.0f,0.0f)}
        texs = Array(drawableIds.size){ Tex2D() }
        for(i in drawableIds.indices){
            val bmp = BitmapFactory.decodeResource(context.resources, drawableIds[i],options)
            var size = Vec2(bmp.width.toFloat(),bmp.height.toFloat())
            bmpSizes[i] = size
            texs[i].gen()
            texs[i].bind()
            texs[i].minFilter(MinFILTER.LINEAR)
            texs[i].magFilter(MagFILTER.LINEAR)
            texs[i].imgae2D(0,bmp,0)
            texs[i].unbind()
        }
        texSize = bmpSizes[0]
        //var dims = FloatBuffer.allocate(4)
        //dims.put(0,0.0f)
        //dims.put(1,0.0f)
        //dims.put(2,0.0f)
        //dims.put(3,0.0f)

        uboInfo.gen()
        uboInfo.bind()
        uboInfo.bufferData(vertInfo.capacity()*4,vertInfo,DataStoreUsage.DYNAMIC_DRAW)
        uboInfo.bindBufferBase(0)
        uboInfo.unbind()

        uboInfo2.gen()
        uboInfo2.bind()
        uboInfo2.bufferData(fragInfo.capacity()*4,fragInfo,DataStoreUsage.DYNAMIC_DRAW)
        uboInfo2.bindBufferBase(1)
        uboInfo2.unbind()
//        ssboDims.gen()
//        ssboDims.bind()
//        ssboDims.bufferData(dims.capacity()*4,dims,DataStoreUsage.DYNAMIC_DRAW)
//        ssboDims.bindBufferBase(0)
    }

//    private var positionHandle: Int = 0
//    private var mColorHandle: Int = 0
//
//    private val vertexCount: Int = rectCoords.size / COORDS_PER_VERTEX
//    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex
//    private val uvStride: Int = UV_COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    fun draw(viewSize:Vec2, texPos:Vec2) {

        var time = (Date().time - startTime)/1000.0f
        if(time > period){
            texIndex = texs.indices.random()
            var i = periods.indices.random()
            period = periods[i]
            texSize = bmpSizes[texIndex]
            startTime = Date().time
            time = 0.0f
        }

        draw(viewSize,texSize,texPos, time, period)
    }
    fun draw(viewSize:Vec2, texSize:Vec2, texPos:Vec2) {

        var time = (Date().time - startTime)/1000.0f
        if(time > period){
            texIndex = texs.indices.random()
            var i = periods.indices.random()
            period = periods[i]
            //texSize = bmpSizes[texIndex]
            startTime = Date().time
            time = 0.0f
        }

        draw(viewSize,texSize,texPos, time, period)
    }

    private fun draw(viewSize:Vec2, texSize:Vec2, texPos:Vec2, time:Float, period:Float) {
        // Add program to OpenGL ES environment
        GLES32.glUseProgram(mProgram)
        vao.bind()

        texs[texIndex].active(0)
        texs[texIndex].bind()

        //var dims = FloatBuffer.allocate(4)
        vertInfo.put(0,viewSize.x)
        vertInfo.put(1,viewSize.y)
        vertInfo.put(2,texSize.x)
        vertInfo.put(3,texSize.y)
        vertInfo.put(4,texPos.x)
        vertInfo.put(5,texPos.y)

        uboInfo.bind()
        uboInfo.bufferSubData(0,vertInfo.capacity()*4,vertInfo)
        uboInfo.bindBufferBase(0)

        //var time = (Date().time - startTime)/1000.0f

        fragInfo.put(0,period)
        fragInfo.put(1,time)

        uboInfo2.bind()
        uboInfo2.bufferSubData(0,fragInfo.capacity()*4,fragInfo)
        uboInfo2.bindBufferBase(1)
//        ssboDims.bind()
//        ssboDims.bufferSubData(0,dims.capacity()*4,dims)

        GLES32.glDrawElements(GLES32.GL_TRIANGLES, 6, GLES32.GL_UNSIGNED_SHORT, 0);

        uboInfo.unbind()
//        ssboDims.unbind()
        uboInfo2.unbind()

        texs[texIndex].unbind()
        vao.unbind()

        GLES32.glUseProgram(0)
    }
}