package com.example.gles3test

import android.opengl.GLES32
import java.nio.Buffer
import java.nio.IntBuffer

class ShaderStrageBufferObject {
    private var ids = IntBuffer.allocate(1)
    val id: Int
        get(){
            return ids[0]
        }
    init {

    }
    public fun gen(): Int{
        GLES32.glGenBuffers(1,ids)
        return ids[0]
    }
    public fun del(){
        GLES32.glDeleteBuffers(1,ids)
    }

    public fun bind(){
        GLES32.glBindBuffer(GLES32.GL_SHADER_STORAGE_BUFFER, ids[0])
        //return ids[0]
    }

    public fun unbind(){
        GLES32.glBindBuffer(GLES32.GL_SHADER_STORAGE_BUFFER,0)
        //return ids[0]
    }

    public fun bufferData(size:Int, buffer: Buffer, usage: DataStoreUsage){
        GLES32.glBufferData(GLES32.GL_SHADER_STORAGE_BUFFER,size,buffer,usage.v)
        //return ids[0]
    }

    public fun bufferSubData(offset: Int,size:Int, buffer: Buffer){
        GLES32.glBufferSubData(GLES32.GL_SHADER_STORAGE_BUFFER,offset,size,buffer)
    }

    public fun bindBufferBase(bindingPos: Int){
        GLES32.glBindBufferBase( GLES32.GL_SHADER_STORAGE_BUFFER, bindingPos, ids[0] );
        //return ids[0]
    }

}