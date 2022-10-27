package jp.co.xing.pianorollviewtest

import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import jp.co.xing.gl.renderer.PianoRollViewRenderer
import jp.co.xing.pianorollviewtest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    lateinit var pianoRollView:GLSurfaceView
    lateinit var pianoRollViewRenderer:PianoRollViewRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        initPianoRollView()
    }
    private fun initPianoRollView(){
        binding.pianorollview.setEGLContextClientVersion(3)

        pianoRollViewRenderer = PianoRollViewRenderer(this)
        // Set the Renderer for drawing on the GLSurfaceView
        binding.pianorollview.setRenderer(pianoRollViewRenderer)
    }
}