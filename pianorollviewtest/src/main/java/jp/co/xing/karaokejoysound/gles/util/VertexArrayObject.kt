package jp.co.xing.karaokejoysound.gles.util

import android.opengl.GLES32
import java.nio.IntBuffer

class VertexArrayObject {
    private var ids = IntBuffer.allocate(1)
    val id: Int
        get(){
            return ids[0]
        }
    public fun gen(): Int{
        GLES32.glGenVertexArrays(1,ids)
        return ids[0]
    }

    public fun del() {
        GLES32.glDeleteVertexArrays(1,ids)
    }

    public fun bind(){
        GLES32.glBindVertexArray(ids[0])
        //return ids[0]
    }
    public fun unbind(){
        GLES32.glBindVertexArray(0)
        //return ids[0]
    }
}