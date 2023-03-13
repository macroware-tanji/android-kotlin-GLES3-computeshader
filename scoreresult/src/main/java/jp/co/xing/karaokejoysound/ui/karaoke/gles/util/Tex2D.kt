package jp.co.xing.karaokejoysound.ui.karaoke.gles.util

import android.graphics.Bitmap
import android.opengl.GLES32
import android.opengl.GLUtils
import java.nio.Buffer
import java.nio.IntBuffer

class Tex2D {
    /*
            GLES32.glGenTextures(1,tex)
        //GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, tex[0]);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR);

        val bmp = BitmapFactory.decodeResource(context.resources, R.drawable.lucky_yotsuba_clover_girl)
        var config = bmp.config

        bmpWidth = bmp.width
        bmpHeight= bmp.height

        GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, bmp, 0);

        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0);

     */
//    enum class MinFILTER(val v:Int){
//        NEAREST(GLES32.GL_NEAREST),
//        LINEAR(GLES32.GL_LINEAR)
//    }
//    enum class MagFILTER(val v:Int){
//        NEAREST(GLES32.GL_NEAREST),
//        LINEAR(GLES32.GL_LINEAR)
//    }
    private var ids = IntBuffer.allocate(1)
    val id: Int
        get(){
            return ids[0]
        }

    public fun gen(): Int {
        GLES32.glGenTextures(1,ids)
        return ids[0]
    }

    public fun del(){
        GLES32.glDeleteTextures(1,ids)
    }

    public fun bind(){
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, ids[0])
    }
    public fun unbind(){
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0)
    }
    public fun paramteri(pname:Int,param:Int){
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, pname, param)
    }
    public fun imgae2D(level: Int,bmp:Bitmap, border:Int){
        GLUtils.texImage2D(GLES32.GL_TEXTURE_2D, 0, bmp, 0)
    }
    public fun imgae2D(level: Int, internalformat: ColorComponents, width:Int, height:Int, border:Int, format: PixelFormat, type: PixelType, buffer: Buffer){
        GLES32.glTexImage2D(GLES32.GL_TEXTURE_2D, level, internalformat.v, width,height,border,format.v,type.v,buffer)
    }
    public fun subImage2D(level: Int,xoffset:Int,yoffset:Int,bmp:Bitmap){
        GLUtils.texSubImage2D(GLES32.GL_TEXTURE_2D,level,xoffset,yoffset,bmp)
    }
    public fun subImage2D(level: Int, xoffset:Int, yoffset:Int, width:Int, height:Int, format: PixelFormat, type: PixelType, buffer: Buffer){
        GLES32.glTexSubImage2D(GLES32.GL_TEXTURE_2D,level,xoffset,yoffset,width,height,format.v,type.v,buffer)
    }
    public fun storage2D(level: Int, internalformat: ColorComponents, width:Int, height:Int){
        GLES32.glTexStorage2D(GLES32.GL_TEXTURE_2D,1,internalformat.v,width,height)
    }
    public fun minFilter(v: MinFILTER){
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D,GLES32.GL_TEXTURE_MIN_FILTER,v.v)
    }
    public fun magFilter(v: MagFILTER){
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D,GLES32.GL_TEXTURE_MAG_FILTER,v.v)
    }
    public fun active(texUnit:Int){
        GLES32.glActiveTexture(GLES32.GL_TEXTURE0 + texUnit);
    }
}