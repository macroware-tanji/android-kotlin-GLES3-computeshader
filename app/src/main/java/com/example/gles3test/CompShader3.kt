package com.example.gles3test

import android.content.Context
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniform1f
import android.opengl.GLES32
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer


class CompShader3(context: Context, compShaderName: String) {
    private var context: Context
    private var compShaderName: String

    private var mProgram: Int

    private var ssbo: IntBuffer
    private var input0: FloatBuffer
    //private var output0: FloatBuffer
    private var arraySize: Int
    private var width:Int
    private var height:Int
    private var floatByte: Int
    private var ssboSize: Int

    init {
        this.context = context
        this.compShaderName = compShaderName

        val compShader: Int = loadShaderFromAssets(GLES32.GL_COMPUTE_SHADER, compShaderName)
        mProgram = GLES32.glCreateProgram().also {

            // add the compute shader to program
            GLES32.glAttachShader(it, compShader)

            // creates OpenGL ES program executables
            GLES32.glLinkProgram(it)
        }
        ssboSize=2
        floatByte=4
        width=32
        height=32
        arraySize = width*height

        this.ssbo = IntBuffer.allocate(ssboSize)

        GLES32.glGenBuffers(ssboSize, ssbo);
        input0 = FloatBuffer.allocate(arraySize)
        var bufs = arrayOf(input0,null)

        for(i in 0 until arraySize){
            input0.put(i,i.toFloat())
        }

        for( i in 0 until ssbo.capacity()){
            GLES32.glBindBuffer(GLES32.GL_SHADER_STORAGE_BUFFER, ssbo[i])
//            var error: Int=0
//            error=GLES32.glGetError()
//            if(error!= GLES32.GL_NO_ERROR){
//                Log.d("error",error.toString())
//            }

            GLES32.glBufferData(GLES32.GL_SHADER_STORAGE_BUFFER, arraySize*floatByte, bufs[i], GLES32.GL_STATIC_DRAW)
            //GLES32.glBindBufferBase(GLES32.GL_SHADER_STORAGE_BUFFER, i, ssbo[i])
//            error = GLES32.glGetError()
//            if(error!= GLES32.GL_NO_ERROR){
//                Log.d("error",error.toString())
//            }
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
        GLES32.glUseProgram(mProgram)

        for( i in 0 until ssbo.capacity()){
            GLES32.glBindBufferBase(GLES32.GL_SHADER_STORAGE_BUFFER, i, ssbo[i])
        }

        GLES32.glDispatchCompute(width/8,height/8,1)
//        var error = GLES32.glGetError()
//        if(error!= GLES32.GL_NO_ERROR){
//            Log.d("error",error.toString())
//        }
        GLES32.glMemoryBarrier(GLES32.GL_SHADER_STORAGE_BARRIER_BIT);
        GLES32.glBindBuffer(GLES32.GL_SHADER_STORAGE_BUFFER, ssbo[1]);
        var result = GLES32.glMapBufferRange(GLES32.GL_SHADER_STORAGE_BUFFER, 0, arraySize * floatByte, GLES32.GL_MAP_READ_BIT)

        var b = result as? ByteBuffer
        var f:FloatBuffer
        if(b!=null){
            b.order(ByteOrder.LITTLE_ENDIAN)
            f = b.asFloatBuffer()
            for(i in 0 until f.capacity()){
                Log.d("GLES3Test",f.get(i).toString())
            }
        }

        GLES32.glUnmapBuffer(GLES32.GL_SHADER_STORAGE_BUFFER);
    }

}