package com.example.gles3test

//import android.R
import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES32
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer


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

class TexMap3(context: Context) {


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

    private var uboDims: UniformBufferObject = UniformBufferObject()
    private var ssboDims: ShaderStrageBufferObject = ShaderStrageBufferObject()
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

    private var bmpWidth:Int=0
    private var bmpHeight:Int=0
    private var vao = VertexArrayObject()

    init {
        this.context = context
        val vertexShader: Int = loadShaderFromAssets(GLES32.GL_VERTEX_SHADER, "shader.es30-9.vertexshader")
        val fragmentShader: Int = loadShaderFromAssets(GLES32.GL_FRAGMENT_SHADER, "shader.es30-2.fragmentshader")

        // create empty OpenGL ES Program
        mProgram = GLES32.glCreateProgram().also {

            // add the vertex shader to program
            GLES32.glAttachShader(it, vertexShader)

            // add the fragment shader to program
            GLES32.glAttachShader(it, fragmentShader)

            // creates OpenGL ES program executables
            GLES32.glLinkProgram(it)
        }
        vao.gen()
        vao.bind()


        vboVertex.gen()
        vboVertex.bind()
        vboVertex.bufferData(rectCoords.size * 4,vertexBuffer,DataStoreUsage.STATIC_DRAW)

        vboVertex.enable(0)
        vboVertex.pointer(COORDS_PER_VERTEX,DataType.FLOAT,false,0,0)
        vboVertex.unbind()


//        GLES32.glGenBuffers(1,vboVertex)
//        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboVertex[0]);
//        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, rectCoords.size * 4,vertexBuffer,GLES32.GL_STATIC_DRAW)
//        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);

        vboUV.gen()
        vboUV.bind()
        vboUV.bufferData(uvCoords.size * 4,uvBuffer,DataStoreUsage.STATIC_DRAW)
        vboUV.enable(1)
        vboUV.pointer(UV_COORDS_PER_VERTEX,DataType.FLOAT,false,0,0)
        vboUV.unbind()

//        GLES32.glGenBuffers(1,vboUV)
//        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboUV[0]);
//        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, uvCoords.size * 4,uvBuffer,GLES32.GL_STATIC_DRAW)
//        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);

        ibo.gen()
        ibo.bind()
        ibo.bufferData(indexes.size * 2,indexBuffer,DataStoreUsage.STATIC_DRAW)

        vao.unbind()
        ibo.unbind()

        val options = BitmapFactory.Options()
        options.inScaled = false //密度によるサイズ変更をキャンセル

        val bmp = BitmapFactory.decodeResource(context.resources, R.drawable.lucky_yotsuba_clover_girl,options)
        var config = bmp.config

        bmpWidth = bmp.width
        bmpHeight= bmp.height

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

        var dims = FloatBuffer.allocate(4)
        dims.put(0,0.0f)
        dims.put(1,0.0f)
        dims.put(2,0.0f)
        dims.put(3,0.0f)

        uboDims.gen()
        uboDims.bind()
        uboDims.bufferData(dims.capacity()*4,dims,DataStoreUsage.DYNAMIC_DRAW)
        uboDims.bindBufferBase(0)

        ssboDims.gen()
        ssboDims.bind()
        ssboDims.bufferData(dims.capacity()*4,dims,DataStoreUsage.DYNAMIC_DRAW)
        ssboDims.bindBufferBase(0)
    }

    private var positionHandle: Int = 0
    private var mColorHandle: Int = 0

    private val vertexCount: Int = rectCoords.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex
    private val uvStride: Int = UV_COORDS_PER_VERTEX * 4 // 4 bytes per vertex

//    fun draw(texId:IntBuffer){
//        // Add program to OpenGL ES environment
//        GLES32.glUseProgram(mProgram)
//
//        GLES32.glEnableVertexAttribArray(0)
//        // Prepare the triangle coordinate data
//        GLES32.glVertexAttribPointer(
//            0,
//            COORDS_PER_VERTEX,
//            GLES32.GL_FLOAT,
//            false,
//            vertexStride,
//            vertexBuffer
//        )
//
//        GLES32.glEnableVertexAttribArray(1)
//        // Prepare the triangle coordinate data
//        GLES32.glVertexAttribPointer(
//            1,
//            UV_COORDS_PER_VERTEX,
//            GLES32.GL_FLOAT,
//            false,
//            uvStride,
//            uvBuffer
//        )
//        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
//        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
//        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, texId[0]);
//        GLES32.glDrawElements(GLES32.GL_TRIANGLES, 6, GLES32.GL_UNSIGNED_SHORT, 0);
//
//        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0);
//        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, 0);
//        GLES32.glDisableVertexAttribArray(0)
//        GLES32.glDisableVertexAttribArray(1)
//
//    }
    fun draw(viewWidth:Float, viewHeight:Float) {
        // Add program to OpenGL ES environment
        GLES32.glUseProgram(mProgram)
        vao.bind()


//        //GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboVertex.id);
//        vboVertex.bind()
//        //GLES32.glEnableVertexAttribArray(0)
//        vboVertex.enable(0)
//        // Prepare the triangle coordinate data
////        GLES32.glVertexAttribPointer(
////            0,
////            COORDS_PER_VERTEX,
////            GLES32.GL_FLOAT,
////            false,
////            0,//vertexStride,
////            0//vertexBuffer
////        )
//        vboVertex.pointer(COORDS_PER_VERTEX,DataType.FLOAT,false,0,0)
//        //GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
//        vboVertex.unbind()

//        //GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, vboUV.id);
//        vboUV.bind()
//
//        //GLES32.glEnableVertexAttribArray(1)
//        vboUV.enable(1)
//        // Prepare the triangle coordinate data
////        GLES32.glVertexAttribPointer(
////            1,
////            UV_COORDS_PER_VERTEX,
////            GLES32.GL_FLOAT,
////            false,
////            0,//uvStride,
////            0//uvBuffer
////        )
//        vboUV.pointer(UV_COORDS_PER_VERTEX,DataType.FLOAT,false,0,0)
//        //GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
//        vboUV.unbind()

//        //GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
//        ibo.bind()

        //GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        //GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, tex[0]);
        tex.active(0)
        tex.bind()

        var dims = FloatBuffer.allocate(4)
        dims.put(0,viewWidth)
        dims.put(1,viewHeight)
        dims.put(2,bmpWidth.toFloat())
        dims.put(3,bmpHeight.toFloat())

        uboDims.bind()
        uboDims.bufferSubData(0,dims.capacity()*4,dims)

        ssboDims.bind()
        ssboDims.bufferSubData(0,dims.capacity()*4,dims)

        GLES32.glDrawElements(GLES32.GL_TRIANGLES, 6, GLES32.GL_UNSIGNED_SHORT, 0);



//        mColorHandle = GLES32.glGetUniformLocation(mProgram, "vColor").also { colorHandle ->
//
//            // Set color for drawing the triangle
//            GLES32.glUniform4fv(colorHandle, 1, color, 0)
//        }
        // Draw the triangle
//        GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, vertexCount)

        uboDims.unbind()
        ssboDims.unbind()
        // Disable vertex array
        //GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0);
        tex.unbind()
        //GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, 0);
        vao.unbind()
        //GLES32.glDisableVertexAttribArray(0)
//    vboVertex.disaable()
//        //GLES32.glDisableVertexAttribArray(1)
//    vboUV.disaable()
//        //GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, 0);

//        // get handle to vertex shader's vPosition member
//        positionHandle = GLES31.glGetAttribLocation(mProgram, "vPosition").also {
//
//            // Enable a handle to the triangle vertices
//            GLES31.glEnableVertexAttribArray(it)
//
//            // Prepare the triangle coordinate data
//            GLES31.glVertexAttribPointer(
//                it,
//                COORDS_PER_VERTEX,
//                GLES31.GL_FLOAT,
//                false,
//                vertexStride,
//                vertexBuffer
//            )
//
//            // get handle to fragment shader's vColor member
//            mColorHandle = GLES31.glGetUniformLocation(mProgram, "vColor").also { colorHandle ->
//
//                // Set color for drawing the triangle
//                GLES31.glUniform4fv(colorHandle, 1, color, 0)
//            }
//
//            // Draw the triangle
//            GLES31.glDrawArrays(GLES31.GL_TRIANGLES, 0, vertexCount)
//
//            // Disable vertex array
//            GLES31.glDisableVertexAttribArray(it)
//        }
    }
}