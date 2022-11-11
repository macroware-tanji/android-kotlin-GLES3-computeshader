package com.example.gles3test

//import android.R
import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES32
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer



class TexBlink1(context: Context, drawableId:Int) {
    private val COORDS_PER_VERTEX = 3
    private var rectCoords = floatArrayOf(
        // in counterclockwise order:
        +1.0f, +1.0f, 0.0f,      // #0: Upper right
        -1.0f, +1.0f, 0.0f,      // #1: Upper left
        -1.0f, -1.0f, 0.0f,      // #2: Lower left
        +1.0f, -1.0f, 0.0f,      // #3: Lower right
    )

    private val UV_COORDS_PER_VERTEX = 2
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

    private var tex = Tex2D()
    private var ibo = IndexBufferObject()// IntBuffer.allocate(1)
    private var vboVertex = VertexBufferObject()//IntBuffer.allocate(1)
    private var vboUV = VertexBufferObject()//IntBuffer.allocate(1)

    private var uboVertInfo: UniformBufferObject = UniformBufferObject()
    private var uboFragInfo: UniformBufferObject = UniformBufferObject()
    //private var ssboDims: ShaderStrageBufferObject = ShaderStrageBufferObject()

    var bmpSize = Vec2(0.0f,0.0f)
    //private var bmpWidth:Int=0
    //private var bmpHeight:Int=0
    private var vao = VertexArrayObject()
    private var vertInfo = FloatBuffer.allocate(9)
    private var fragInfo = FloatBuffer.allocate(3)

    init {
        this.context = context
        val vertexShader: Int = loadShaderFromAssets(GLES32.GL_VERTEX_SHADER, "texblink-1.vertexshader")
        val fragmentShader: Int = loadShaderFromAssets(GLES32.GL_FRAGMENT_SHADER, "texblink-1.fragmentshader")

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
        vboVertex.bufferData(rectCoords.size * 4,vertexBuffer, DataStoreUsage.STATIC_DRAW)
        vboVertex.unbind()

        vboUV.gen()
        vboUV.bind()
        vboUV.bufferData(uvCoords.size * 4,uvBuffer, DataStoreUsage.STATIC_DRAW)
        vboUV.unbind()

        ibo.gen()
        ibo.bind()
        ibo.bufferData(indexes.size * 2,indexBuffer, DataStoreUsage.STATIC_DRAW)
        ibo.unbind()

        vao.gen()
        vao.bind()

        vboVertex.bind()
        vboVertex.enable(0)
        vboVertex.pointer(COORDS_PER_VERTEX, DataType.FLOAT,false,0,0)
        vboVertex.unbind()

        vboUV.bind()
        vboUV.enable(1)
        vboUV.pointer(UV_COORDS_PER_VERTEX, DataType.FLOAT,false,0,0)
        vboUV.unbind()

        ibo.bind()

        vao.unbind()
        ibo.unbind()

        val options = BitmapFactory.Options()
        options.inScaled = false //密度によるサイズ変更をキャンセル

        val bmp = BitmapFactory.decodeResource(context.resources, drawableId,options)
        //var config = bmp.config

        bmpSize.x = bmp.width.toFloat()
        bmpSize.y = bmp.height.toFloat()

        tex.gen()
        tex.bind()
        tex.minFilter(MinFILTER.LINEAR)
        tex.magFilter(MagFILTER.LINEAR)
        tex.imgae2D(0,bmp,0)
        tex.unbind()

//        GLES32.glGenTextures(1,tex)
//        //GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
//        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, tex[0]);
//        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR);
//        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR);
//        GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, bmp, 0);
//        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0);


//        GLES32.glGenBuffers(1,ibo)
//        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
//        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER,indexes.size * 2,indexBuffer,GLES32.GL_STATIC_DRAW)
//        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, 0);

        //var dims = FloatBuffer.allocate(4)
        //dims.put(0,0.0f)
        //dims.put(1,0.0f)
        //dims.put(2,0.0f)
        //dims.put(3,0.0f)

        uboVertInfo.gen()
        uboVertInfo.bind()
        uboVertInfo.bufferData(vertInfo.capacity()*4,vertInfo, DataStoreUsage.DYNAMIC_DRAW)
        uboVertInfo.bindBufferBase(0)
        uboVertInfo.unbind()

        uboFragInfo.gen()
        uboFragInfo.bind()
        uboFragInfo.bufferData(fragInfo.capacity()*4,fragInfo, DataStoreUsage.DYNAMIC_DRAW)
        uboFragInfo.bindBufferBase(1)
        uboFragInfo.unbind()
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
    fun draw(viewSize: Vec2, texPos: Vec2, start:Float, duration:Float, time:Float) {
        draw(viewSize,bmpSize,texPos,start,duration,time)
    }

    fun draw(viewSize: Vec2, texSize: Vec2, texPos: Vec2, start:Float, duration:Float, time:Float) {
        // Add program to OpenGL ES environment
        GLES32.glUseProgram(mProgram)
        vao.bind()

        tex.active(0)
        tex.bind()

        //var dims = FloatBuffer.allocate(4)
        vertInfo.put(0,viewSize.x)
        vertInfo.put(1,viewSize.y)
        vertInfo.put(2,texSize.x)
        vertInfo.put(3,texSize.y)
        vertInfo.put(4,texPos.x)
        vertInfo.put(5,texPos.y)
        vertInfo.put(6,start)
        vertInfo.put(7,duration)
        vertInfo.put(8,time)

        uboVertInfo.bind()
        uboVertInfo.bufferSubData(0,vertInfo.capacity()*4,vertInfo)
        uboVertInfo.bindBufferBase(0)

        fragInfo.put(0,start)
        fragInfo.put(1,duration)
        fragInfo.put(2,time)

        uboFragInfo.bind()
        uboFragInfo.bufferSubData(0,fragInfo.capacity()*4,fragInfo)
        uboFragInfo.bindBufferBase(1)
//        ssboDims.bind()
//        ssboDims.bufferSubData(0,dims.capacity()*4,dims)

        GLES32.glDrawElements(GLES32.GL_TRIANGLES, 6, GLES32.GL_UNSIGNED_SHORT, 0);

        uboVertInfo.unbind()
        uboFragInfo.unbind()
//        ssboDims.unbind()

        tex.unbind()
        vao.unbind()

        GLES32.glUseProgram(0)
    }
}