package com.example.gles3test

import android.content.Context
import android.opengl.GLES32
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import java.util.*

class Pentagon(context: Context) {
    private val COORDS_PER_VERTEX = 3
    private var vertexCoords = floatArrayOf(
        // in counterclockwise order:
        +0.0f, +0.0f, 0.0f,      // #0: center
        -0.0f, +0.0f, 1.0f,      // #1: upper center
        -0.0f, -0.0f, 2.0f,      // #2: upper left
        +0.0f, -0.0f, 3.0f,      // #3: Lower left
        +0.0f, -0.0f, 4.0f,      // #3: Lower right
        +0.0f, -0.0f, 5.0f,      // #3: upper right
    )

    private var indexes = shortArrayOf(
        0,1,2,
        0,2,3,
        0,3,4,
        0,4,5,
        0,5,1,
    )
    private var context: Context
    private var mProgram: Int

    private var vertexBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(vertexCoords.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(vertexCoords)
                // set the buffer to read the first coordinate
                position(0)
            }
        }
//    private var uvBuffer: FloatBuffer =
//        // (number of coordinate values * 4 bytes per float)
//        ByteBuffer.allocateDirect(uvCoords.size * 4).run {
//            // use the device hardware's native byte order
//            order(ByteOrder.nativeOrder())
//
//            // create a floating point buffer from the ByteBuffer
//            asFloatBuffer().apply {
//                // add the coordinates to the FloatBuffer
//                put(uvCoords)
//                // set the buffer to read the first coordinate
//                position(0)
//            }
//        }

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
    private var ibo = IndexBufferObject()// IntBuffer.allocate(1)
    private var vboVertex = VertexBufferObject()//IntBuffer.allocate(1)
    private var vao = VertexArrayObject()
    private var uboInfo: UniformBufferObject = UniformBufferObject()
    private var uboInfo2: UniformBufferObject = UniformBufferObject()
    private var infoBuffer = FloatBuffer.allocate(12)
    private var infoBuffer2 = FloatBuffer.allocate(4)
    private var startTime = Date().time

    init {
        this.context = context
        val vertexShader: Int = loadShaderFromAssets(GLES32.GL_VERTEX_SHADER, "pentagon-0.vertexshader")
        val fragmentShader: Int = loadShaderFromAssets(GLES32.GL_FRAGMENT_SHADER, "pentagon-0.fragmentshader")

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
        vboVertex.bufferData(vertexCoords.size * 4,vertexBuffer,DataStoreUsage.STATIC_DRAW)
        vboVertex.unbind()

//        vboUV.gen()
//        vboUV.bind()
//        vboUV.bufferData(uvCoords.size * 4,uvBuffer,DataStoreUsage.STATIC_DRAW)
//        vboUV.unbind()

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

//        vboUV.bind()
//        vboUV.enable(1)
//        vboUV.pointer(UV_COORDS_PER_VERTEX,DataType.FLOAT,false,0,0)
//        vboUV.unbind()

        ibo.bind()

        vao.unbind()
        ibo.unbind()

//        var infoBuffer = FloatBuffer.allocate(6)
//        infoBuffer.put(0,1.0f)
//        infoBuffer.put(1,1.0f)
//        infoBuffer.put(2,1.0f)
//        infoBuffer.put(3,1.0f)
//        infoBuffer.put(4,1.0f)
//        infoBuffer.put(5,1.0f)

        uboInfo.gen()
        uboInfo.bind()
        uboInfo.bufferData(infoBuffer.capacity()*4,infoBuffer,DataStoreUsage.DYNAMIC_DRAW)
        uboInfo.bindBufferBase(0)
        uboInfo.unbind()

        uboInfo2.gen()
        uboInfo2.bind()
        uboInfo2.bufferData(infoBuffer2.capacity()*4,infoBuffer2,DataStoreUsage.DYNAMIC_DRAW)
        uboInfo2.bindBufferBase(1)
        uboInfo2.unbind()

        startTime = Date().time
    }
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
    fun draw(viewSize:Vec2, center:Vec2, length:Float, score:Array<Float>, period:Float,color:Color) {
        // Add program to OpenGL ES environment
        GLES32.glUseProgram(mProgram)
        //GLES32.glLineWidth(100.0f)
        //GLES32.glEnable(GLES32.GL_PO POINT_SMOOTH)

        var time = (Date().time - startTime)/1000.0f

        vao.bind()

        //tex.active(0)
        //tex.bind()

        //var infoBuffer = FloatBuffer.allocate(6)
        infoBuffer.put(0,viewSize.x)//resolution.x
        infoBuffer.put(1,viewSize.y)//resolution.y

        infoBuffer.put(2,center.x)//center.x
        infoBuffer.put(3,center.y)//center.y

        infoBuffer.put(4,length)//length
        infoBuffer.put(5,score[0])//score
        infoBuffer.put(6,score[1])//score
        infoBuffer.put(7,score[2])//score
        infoBuffer.put(8,score[3])//score
        infoBuffer.put(9,score[4])//score
        infoBuffer.put(10,time)//time
        infoBuffer.put(11,period)//period

        uboInfo.bind()
        uboInfo.bufferSubData(0,infoBuffer.capacity()*4,infoBuffer)
        uboInfo.bindBufferBase(0)

        infoBuffer2.put(0,color.r)//color.r
        infoBuffer2.put(1,color.g)//color.g
        infoBuffer2.put(2,color.b)//color.b
        infoBuffer2.put(3,color.a)//color.a

        uboInfo2.bind()
        uboInfo2.bufferSubData(0,infoBuffer2.capacity()*4,infoBuffer2)
        uboInfo2.bindBufferBase(1)

        //ssboDims.bind()
        //ssboDims.bufferSubData(0,dims.capacity()*4,dims)

        GLES32.glDrawElements(GLES32.GL_TRIANGLES, indexes.size, GLES32.GL_UNSIGNED_SHORT, 0);

        uboInfo.unbind()

        //uboDims.unbind()
        //ssboDims.unbind()
        //tex.unbind()

        vao.unbind()
        GLES32.glUseProgram(0)
    }

}