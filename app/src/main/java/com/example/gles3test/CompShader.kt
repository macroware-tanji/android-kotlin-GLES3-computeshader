package com.example.gles3test

import android.content.Context
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniform1f
import android.opengl.GLES32
import java.nio.FloatBuffer
import java.nio.IntBuffer


class CompShader(context: Context, width: Int,height: Int, depth: Int,internalFormat: Int,format: Int, type: Int, compShaderName: String) {
    private var context: Context
    private var width: Int
    private var height: Int
    private var depth: Int
    private var internalFormat: Int
    private var format: Int
    private var type: Int
    private var compShaderName: String

    public var texId: IntBuffer

    private var mProgram: Int
    private var frame : Float = 0.0f

    init {
        this.context = context
        this.width = width
        this.height = height
        this.depth = depth
        this.internalFormat= internalFormat
        this.format = format
        this.type= type
        this.compShaderName = compShaderName
        this.texId=IntBuffer.allocate(1);
        this.frame= 0.0f

//        var buffer = FloatBuffer.allocate(width*height*4)
//        for(y in 0 until height){
//            for(x in 0 until width) {
//                var index = (width*4) * y + x * 4;
//                buffer.put(index+0,1.0f)
//                buffer.put(index+1,1.0f)
//                buffer.put(index+2,0.0f)
//                buffer.put(index+3,1.0f)
//            }
//        }

        GLES32.glGenTextures(1, texId);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, texId[0]);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR);
        //GLES32.glTexImage2D(GLES32.GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type, buffer);
        GLES32.glTexStorage2D(GLES32.GL_TEXTURE_2D,1,internalFormat,this.width,this.height);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0);

        val compShader: Int = loadShaderFromAssets(GLES32.GL_COMPUTE_SHADER, compShaderName)
        mProgram = GLES32.glCreateProgram().also {

            // add the compute shader to program
            GLES32.glAttachShader(it, compShader)

            // creates OpenGL ES program executables
            GLES32.glLinkProgram(it)
        }
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
    fun execute(){
        //var frame : Float = 0.0f
        GLES32.glUseProgram(mProgram)
        GLES32.glBindImageTexture(0,texId[0],0,false,0,GLES32.GL_WRITE_ONLY,internalFormat)
        GLES32.glUniform1f(GLES32.glGetUniformLocation(mProgram, "roll"), frame  * 0.01f)
        GLES32.glDispatchCompute(width / 32, height / 32, depth);
        GLES32.glUseProgram(0);
        frame += 1f
    }

}