package com.example.gles3test

import android.opengl.GLES32
import java.nio.Buffer
import java.nio.IntBuffer

class VertexBufferObject {
    private var ids = IntBuffer.allocate(1)
    val id: Int
        get(){
            return ids[0]
        }

    private var locationIndex: Int=0
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
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,ids[0])
        //return ids[0]
    }

    public fun unbind(){
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER,0)
        //return ids[0]
    }

    public fun bufferData(size:Int, buffer: Buffer, usage: DataStoreUsage){
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER,size,buffer,usage.v)
        //return ids[0]
    }
    public fun enable(index: Int){
        locationIndex = index
        GLES32.glEnableVertexAttribArray(index)
    }
    public fun disaable(){
        GLES32.glDisableVertexAttribArray(locationIndex)
    }
    public fun pointer(size:Int,type:DataType,normalized:Boolean, stride:Int, offset:Int){
        GLES32.glVertexAttribPointer(locationIndex,size,type.v,normalized,stride,offset)
    }
    public fun pointer(size:Int,type:DataType,normalized:Boolean, stride:Int, ptr: Buffer){
        GLES32.glVertexAttribPointer(locationIndex,size,type.v,normalized,stride,ptr)
    }
}