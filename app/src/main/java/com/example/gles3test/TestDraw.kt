package com.example.gles3test

import android.content.Context
import android.opengl.GLES32
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import java.util.*

class TestDraw(context: Context) {
    private val COORDS_PER_VERTEX = 3
    private var rectCoords = floatArrayOf(
        // in counterclockwise order:
        +1.0f, +1.0f, 0.0f,      // #0: Upper right
        -1.0f, +1.0f, 0.0f,      // #1: Upper left
        -1.0f, -1.0f, 0.0f,      // #2: Lower left
        +1.0f, -1.0f, 0.0f,      // #3: Lower right
    )

    private var indexes = shortArrayOf(
        0,1,2,
        0,2,3
    )
    private var context: Context
    private var mProgram: Int

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
    private var uboInfo0: UniformBufferObject = UniformBufferObject()
    //private var uboInfo1: UniformBufferObject = UniformBufferObject()
    private var infoBuffer = FloatBuffer.allocate(3+4)

    private var startTime = Date().time

    init {
        this.context = context
        val vertexShader: Int = loadShaderFromAssets(GLES32.GL_VERTEX_SHADER, "test_draw-0.vertexshader")
        val fragmentShader: Int = loadShaderFromAssets(GLES32.GL_FRAGMENT_SHADER, "test_draw-0.fragmentshader")

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

        uboInfo0.gen()
        uboInfo0.bind()
        uboInfo0.bufferData(infoBuffer.capacity()*4,infoBuffer,DataStoreUsage.DYNAMIC_DRAW)
        uboInfo0.bindBufferBase(0)
        uboInfo0.unbind()

//        uboInfo1.gen()
//        uboInfo1.bind()
//        uboInfo1.bufferData(infoBuffer.capacity()*4,infoBuffer,DataStoreUsage.DYNAMIC_DRAW)
//        uboInfo1.bindBufferBase(1)
//        uboInfo1.unbind()
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
    fun draw(viewSize:Vec2 ) {
        // Add program to OpenGL ES environment
        GLES32.glUseProgram(mProgram)
        //GLES32.glLineWidth(100.0f)
        //GLES32.glEnable(GLES32.GL_PO POINT_SMOOTH)

        vao.bind()

        //tex.active(0)
        //tex.bind()

        var time = (Date().time - startTime)/1000.0f
        //var infoBuffer = FloatBuffer.allocate(6)
        infoBuffer.put(0,1.0f)//rectSize.x
        infoBuffer.put(1,0.0f)//rectSize.x
        infoBuffer.put(2,0.0f)//rectSize.x
        infoBuffer.put(3,1.0f)//rectSize.x

        infoBuffer.put(4,viewSize.x)//resolution.x
        infoBuffer.put(5,viewSize.y)//resolution.y

        infoBuffer.put(6,time.toFloat())//rectSize.x


//        infoBuffer.put(2,rectSize.x)//rectSize.x
//        infoBuffer.put(3,rectSize.y)//rectSize.y
//
//        infoBuffer.put(4,rectPos.x)//rectPos.x
//        infoBuffer.put(5,rectPos.y)//rectPos.y
//
//        infoBuffer.put(6,color.r)//color.r
//        infoBuffer.put(7,color.g)//color.g
//        infoBuffer.put(8,color.b)//color.b
//        infoBuffer.put(9,color.a)//color.a
//
//        infoBuffer.put(10,edge)//edge

        uboInfo0.bind()
        uboInfo0.bufferSubData(0,infoBuffer.capacity()*4,infoBuffer)
        uboInfo0.bindBufferBase(0)

//        uboInfo1.bind()
//        uboInfo1.bufferSubData(0,infoBuffer.capacity()*4,infoBuffer)
//        uboInfo1.bindBufferBase(1)

        //ssboDims.bind()
        //ssboDims.bufferSubData(0,dims.capacity()*4,dims)

        GLES32.glDrawElements(GLES32.GL_TRIANGLES, 6, GLES32.GL_UNSIGNED_SHORT, 0);

        uboInfo0.unbind()
        //uboInfo1.unbind()

        //uboDims.unbind()
        //ssboDims.unbind()
        //tex.unbind()

        vao.unbind()
        GLES32.glUseProgram(0)
    }

}