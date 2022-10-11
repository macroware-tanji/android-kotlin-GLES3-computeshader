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
private var lineCoords = floatArrayOf(
    // in counterclockwise order:
    +0.8f, +0.8f, 0.0f,      // #0: Upper right
    -0.8f, +0.8f, 0.0f,      // #1: Upper left
    -0.8f, -0.8f, 0.0f,      // #2: Lower left
    +0.8f, -0.8f, 0.0f,      // #3: Lower right
)

private var indexes = shortArrayOf(
    0,1,
    2,3
)

class Lines(context: Context) {

    private var vertexBuffer: FloatBuffer =
        // (number of coordinate values * 4 bytes per float)
        ByteBuffer.allocateDirect(lineCoords.size * 4).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())

            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(lineCoords)
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
    //private var vboUV = VertexBufferObject()//IntBuffer.allocate(1)
    private var uboDims: UniformBufferObject = UniformBufferObject()
//    private var ssboDims: ShaderStrageBufferObject = ShaderStrageBufferObject()


    //private var bmpWidth:Int=0
    //private var bmpHeight:Int=0
    private var vao = VertexArrayObject()

    init {
        this.context = context
        val vertexShader: Int = loadShaderFromAssets(GLES32.GL_VERTEX_SHADER, "shader.es30-a.vertexshader")
        val fragmentShader: Int = loadShaderFromAssets(GLES32.GL_FRAGMENT_SHADER, "shader.es30-a.fragmentshader")

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
        vboVertex.bufferData(lineCoords.size * 4,vertexBuffer,DataStoreUsage.STATIC_DRAW)
        vboVertex.unbind()

        //vboUV.gen()
        //vboUV.bind()
        //vboUV.bufferData(uvCoords.size * 4,uvBuffer,DataStoreUsage.STATIC_DRAW)
        //vboUV.unbind()

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

        //vboUV.bind()
        //vboUV.enable(1)
        //vboUV.pointer(UV_COORDS_PER_VERTEX,DataType.FLOAT,false,0,0)
        //vboUV.unbind()

        ibo.bind()

        vao.unbind()
        ibo.unbind()

//        val options = BitmapFactory.Options()
//        options.inScaled = false //密度によるサイズ変更をキャンセル
//
//        val bmp = BitmapFactory.decodeResource(context.resources, R.drawable.lucky_yotsuba_clover_girl,options)
//        var config = bmp.config
//
//        bmpWidth = bmp.width
//        bmpHeight= bmp.height
//
//        tex.gen()
//        tex.bind()
//        tex.minFilter(MinFILTER.LINEAR)
//        tex.magFilter(MagFILTER.LINEAR)
//        tex.imgae2D(0,bmp,0)
//        tex.unbind()

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

        var lineWidth = FloatBuffer.allocate(2)
        GLES32.glGetFloatv(GLES32.GL_ALIASED_LINE_WIDTH_RANGE,lineWidth)
        var lineWidthMin = lineWidth[0]
        var lineWidthMax = lineWidth[1]

        var pointSize = FloatBuffer.allocate(2)
        GLES32.glGetFloatv(GLES32.GL_ALIASED_POINT_SIZE_RANGE,pointSize)
        var pointSizeMin = pointSize[0]
        var pointSizeMax = pointSize[1]

        var colorBuffer = FloatBuffer.allocate(4)
        colorBuffer.put(0,1.0f)
        colorBuffer.put(1,1.0f)
        colorBuffer.put(2,1.0f)
        colorBuffer.put(3,1.0f)

        uboDims.gen()
        uboDims.bind()
        uboDims.bufferData(colorBuffer.capacity()*4,colorBuffer,DataStoreUsage.DYNAMIC_DRAW)
        uboDims.bindBufferBase(0)
//
//        ssboDims.gen()
//        ssboDims.bind()
//        ssboDims.bufferData(dims.capacity()*4,dims,DataStoreUsage.DYNAMIC_DRAW)
//        ssboDims.bindBufferBase(0)
    }

    private var positionHandle: Int = 0
    private var mColorHandle: Int = 0

    private val vertexCount: Int = lineCoords.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex
    //private val uvStride: Int = UV_COORDS_PER_VERTEX * 4 // 4 bytes per vertex

    fun draw() {
        // Add program to OpenGL ES environment
        GLES32.glUseProgram(mProgram)
        GLES32.glLineWidth(100.0f)
        //GLES32.glEnable(GLES32.GL_PO POINT_SMOOTH)

        vao.bind()

        //tex.active(0)
        //tex.bind()

        var colorBuffer = FloatBuffer.allocate(4)
        colorBuffer.put(0,1.0f)
        colorBuffer.put(1,0.0f)
        colorBuffer.put(2,0.0f)
        colorBuffer.put(3,1.0f)

        uboDims.bind()
        uboDims.bufferSubData(0,colorBuffer.capacity()*4,colorBuffer)

        //ssboDims.bind()
        //ssboDims.bufferSubData(0,dims.capacity()*4,dims)

        GLES32.glDrawElements(GLES32.GL_POINTS, 4, GLES32.GL_UNSIGNED_SHORT, 0);

        //uboDims.unbind()
        //ssboDims.unbind()

        //tex.unbind()
        vao.unbind()
    }
}