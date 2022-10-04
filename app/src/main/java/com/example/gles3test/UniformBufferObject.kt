package com.example.gles3test

import java.nio.IntBuffer
import android.opengl.GLES32
import java.nio.Buffer

class UniformBufferObject {
    private var ids = IntBuffer.allocate(1)
    //private var size: Int=0
    //private var buffer?: Buffer=null
    //private final var gl: GLES32=GLES32
    val id: Int
    get(){
        return ids[0]
    }

//    enum class Usage(val id: Int){
//        STATIC_DRAW(GLES32.GL_STATIC_DRAW),
//        DYNAMIC_DRAW(GLES32.GL_DYNAMIC_DRAW)
//    }

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
        GLES32.glBindBuffer(GLES32.GL_UNIFORM_BUFFER,ids[0])
        //return ids[0]
    }

    public fun unbind(){
        GLES32.glBindBuffer(GLES32.GL_UNIFORM_BUFFER,0)
        //return ids[0]
    }

    public fun bufferData(size:Int,buffer:Buffer,usage: DataStoreUsage){
        GLES32.glBufferData(GLES32.GL_UNIFORM_BUFFER,size,buffer,usage.v)
        //return ids[0]
    }
    public fun bindBufferBase(bindingPos: Int){
        GLES32.glBindBufferBase( GLES32.GL_UNIFORM_BUFFER, bindingPos, ids[0] );
        //return ids[0]
    }

}