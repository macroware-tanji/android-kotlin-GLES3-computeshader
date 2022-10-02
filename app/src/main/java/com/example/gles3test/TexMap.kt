package com.example.gles3test

//import android.R
import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES32
import android.opengl.GLUtils
import java.nio.*


private const val COORDS_PER_VERTEX = 3
private var rectCoords = floatArrayOf(
    // in counterclockwise order:
    +1.0f, +1.0f, 0.0f,      // #0: Upper right
    -1.0f, +1.0f, 0.0f,      // #1: Upper left
    -1.0f, -1.0f, 0.0f,      // #2: Lower left
    +1.0f, -1.0f, 0.0f,      // #3: Lower right
)

private const val UV_COORDS_PER_VERTEX = 2
private var uvCoords = floatArrayOf(     // in counterclockwise order:
    1.0f, 0.0f,                // #3: Lower right
    0.0f, 0.0f,               // #2: Lower left
    0.0f, 1.0f,               // #1: Upper left
    1.0f, 1.0f,               // #0: Upper right
)
private var indexes = shortArrayOf(
    0,1,2,
    0,2,3
)

class TexMap(context: Context, texId: IntBuffer? = null) {


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
    private var tex = IntBuffer.allocate(1)
    private var ibo = IntBuffer.allocate(1)
    init {
        this.context = context
        //val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        //val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        val vertexShader: Int = loadShaderFromAssets(GLES32.GL_VERTEX_SHADER, "shader.es30-2.vertexshader")
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
        if(texId is IntBuffer){
            tex = texId
        }
        else {
            GLES32.glGenTextures(1,tex)
            GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
            GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, tex[0]);
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR);
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR);

            val bmp = BitmapFactory.decodeResource(context.resources, R.drawable.lucky_yotsuba_clover_girl)

            GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, bmp, 0);
            GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0);
        }

        GLES32.glGenBuffers(1,ibo)
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER,indexes.size * 2,indexBuffer,GLES32.GL_STATIC_DRAW)
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    private var positionHandle: Int = 0
    private var mColorHandle: Int = 0

    private val vertexCount: Int = rectCoords.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex
    private val uvStride: Int = UV_COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    fun draw(texId:IntBuffer){
        // Add program to OpenGL ES environment
        GLES32.glUseProgram(mProgram)

        GLES32.glEnableVertexAttribArray(0)
        // Prepare the triangle coordinate data
        GLES32.glVertexAttribPointer(
            0,
            COORDS_PER_VERTEX,
            GLES32.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )

        GLES32.glEnableVertexAttribArray(1)
        // Prepare the triangle coordinate data
        GLES32.glVertexAttribPointer(
            1,
            UV_COORDS_PER_VERTEX,
            GLES32.GL_FLOAT,
            false,
            uvStride,
            uvBuffer
        )
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, texId[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, 6, GLES32.GL_UNSIGNED_SHORT, 0);



//        mColorHandle = GLES32.glGetUniformLocation(mProgram, "vColor").also { colorHandle ->
//
//            // Set color for drawing the triangle
//            GLES32.glUniform4fv(colorHandle, 1, color, 0)
//        }
        // Draw the triangle
//        GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, vertexCount)

        // Disable vertex array
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0);
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES32.glDisableVertexAttribArray(0)
        GLES32.glDisableVertexAttribArray(1)
        //GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, 0);

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
    fun draw() {
        // Add program to OpenGL ES environment
        GLES32.glUseProgram(mProgram)

        GLES32.glEnableVertexAttribArray(0)
        // Prepare the triangle coordinate data
        GLES32.glVertexAttribPointer(
            0,
            COORDS_PER_VERTEX,
            GLES32.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )

        GLES32.glEnableVertexAttribArray(1)
        // Prepare the triangle coordinate data
        GLES32.glVertexAttribPointer(
            1,
            UV_COORDS_PER_VERTEX,
            GLES32.GL_FLOAT,
            false,
            uvStride,
            uvBuffer
        )
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, tex[0]);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, 6, GLES32.GL_UNSIGNED_SHORT, 0);



//        mColorHandle = GLES32.glGetUniformLocation(mProgram, "vColor").also { colorHandle ->
//
//            // Set color for drawing the triangle
//            GLES32.glUniform4fv(colorHandle, 1, color, 0)
//        }
        // Draw the triangle
//        GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, vertexCount)

        // Disable vertex array
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0);
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES32.glDisableVertexAttribArray(0)
        GLES32.glDisableVertexAttribArray(1)
        //GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, 0);

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