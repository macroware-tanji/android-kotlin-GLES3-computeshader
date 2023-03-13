package jp.co.xing.karaokejoysound.ui.karaoke.gles.util

import android.opengl.GLES32
import java.nio.Buffer
import java.nio.IntBuffer

class IndexBufferObject {
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
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,ids[0])
        //return ids[0]
    }

    public fun unbind(){
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER,0)
        //return ids[0]
    }

    public fun bufferData(size:Int, buffer: Buffer, usage: DataStoreUsage){
        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER,size,buffer,usage.v)
        //return ids[0]
    }
    public fun bufferSubData(offset: Int,size:Int, buffer: Buffer){
        GLES32.glBufferSubData(GLES32.GL_ELEMENT_ARRAY_BUFFER,offset,size,buffer)
    }
}