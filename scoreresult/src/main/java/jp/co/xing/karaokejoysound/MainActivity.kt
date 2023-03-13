package jp.co.xing.karaokejoysound

import android.graphics.PixelFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import jp.co.xing.karaokejoysound.databinding.ActivityMainBinding
import jp.co.xing.karaokejoysound.ui.karaoke.gles.renderer.*

class MainActivity : AppCompatActivity() {
    companion object {
        val TAG = MainActivity::class.simpleName
    }


    private lateinit var binding: ActivityMainBinding
    private lateinit var radiationRenderer: RadiationRenderer
    private lateinit var growRenderer: GrowRenderer
    private lateinit var pentagonRenderer: PentagonRenderer
    private lateinit var kiraRenderer01: KiraRenderer
    private lateinit var kiraRenderer02: KiraRenderer
    private lateinit var kiraRenderer03: KiraRenderer
    private lateinit var kiraRenderer04: KiraRenderer
    private lateinit var kiraRenderer05: KiraRenderer
    private lateinit var medalAnimRenderer: MedalAnimRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        initRenderer();
    }
    private fun initRenderer(){
        binding.radiation.setEGLContextClientVersion(3)
        binding.radiation.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        //binding.radiation.holder.setFormat(PixelFormat.TRANSLUCENT)
        //binding.radiation.setZOrderOnTop(true)
        radiationRenderer = RadiationRenderer(this)
        binding.radiation.setRenderer(radiationRenderer)

        binding.grow.setEGLContextClientVersion(3)
        binding.grow.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        binding.grow.holder.setFormat(PixelFormat.TRANSLUCENT)
        binding.grow.setZOrderOnTop(true)
        growRenderer = GrowRenderer(this)
        binding.grow.setRenderer(growRenderer)

        binding.medal.setEGLContextClientVersion(3)
        binding.medal.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        binding.medal.holder.setFormat(PixelFormat.TRANSLUCENT)
        binding.medal.setZOrderOnTop(true)
        medalAnimRenderer = MedalAnimRenderer(this, R.drawable.img_medal01_rainbow,true)
        binding.medal.setRenderer(medalAnimRenderer)
        medalAnimRenderer.startAnimation()

        binding.pentagon.setEGLContextClientVersion(3)
        binding.pentagon.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        binding.pentagon.holder.setFormat(PixelFormat.TRANSLUCENT)
        binding.pentagon.setZOrderOnTop(true)
        pentagonRenderer = PentagonRenderer(this)
        pentagonRenderer.listner = object : PentagonRenderer.Listner{
            override fun onEndAnimation() {
                Log.d(TAG,"onEndAnimation()")
            }
        }
        pentagonRenderer.setScore(
            40f,
            5f,
            10f,
            15f,
            30f)
        binding.pentagon.setRenderer(pentagonRenderer)
        pentagonRenderer.start()

        binding.kira01.setEGLContextClientVersion(3)
        binding.kira01.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        binding.kira01.holder.setFormat(PixelFormat.TRANSLUCENT)
        binding.kira01.setZOrderOnTop(true)
        kiraRenderer01 = KiraRenderer(this)
        binding.kira01.setRenderer(kiraRenderer01)

        binding.kira02.setEGLContextClientVersion(3)
        binding.kira02.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        binding.kira02.holder.setFormat(PixelFormat.TRANSLUCENT)
        binding.kira02.setZOrderOnTop(true)
        kiraRenderer02 = KiraRenderer(this)
        binding.kira02.setRenderer(kiraRenderer02)

        binding.kira03.setEGLContextClientVersion(3)
        binding.kira03.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        binding.kira03.holder.setFormat(PixelFormat.TRANSLUCENT)
        binding.kira03.setZOrderOnTop(true)
        kiraRenderer03 = KiraRenderer(this)
        binding.kira03.setRenderer(kiraRenderer03)

        binding.kira04.setEGLContextClientVersion(3)
        binding.kira04.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        binding.kira04.holder.setFormat(PixelFormat.TRANSLUCENT)
        binding.kira04.setZOrderOnTop(true)
        kiraRenderer04 = KiraRenderer(this)
        binding.kira04.setRenderer(kiraRenderer04)

        binding.kira05.setEGLContextClientVersion(3)
        binding.kira05.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        binding.kira05.holder.setFormat(PixelFormat.TRANSLUCENT)
        binding.kira05.setZOrderOnTop(true)
        kiraRenderer05 = KiraRenderer(this)
        binding.kira05.setRenderer(kiraRenderer05)

    }
}