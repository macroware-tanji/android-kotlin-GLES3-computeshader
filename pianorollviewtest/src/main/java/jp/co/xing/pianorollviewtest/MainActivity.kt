package jp.co.xing.pianorollviewtest

import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import jp.co.brother.rex.KaraokeGrader
import jp.co.xing.gl.renderer.PianoRollViewRenderer
import jp.co.xing.pianorollviewtest.databinding.ActivityMainBinding

//data class Mora(var time:Double,var duration: Double,var note:Int, var flag:Int)
data class Criteria(var notes:List<KaraokeGrader.Mora>)
data class CriteriaRoot(
    var criterias: List<Criteria>
)

//data class GradingSection(var head:Double, var tail:Double, var text:String)
data class Sections(
    var sections: List<KaraokeGrader.GradingSection>
)

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    lateinit var pianoRollView:GLSurfaceView
    lateinit var pianoRollViewRenderer:PianoRollViewRenderer
    lateinit var graderInfo:KaraokeGrader.GraderInfo

    val handler = Handler(Looper.getMainLooper())
    var startTime:Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //var criteria = KaraokeGrader.Criteria()
        //var graderInfo = KaraokeGrader.GraderInfo()
        val jsonCriteria = this.getAssets().open("criteria.json").reader(charset=Charsets.UTF_8).use{it.readText()}
        val criteriaRoot = Gson().fromJson(jsonCriteria, CriteriaRoot::class.java)

        var criteriaList:MutableList<KaraokeGrader.Criteria> = mutableListOf()
        for( criteria in criteriaRoot.criterias){
            var array = criteria.notes.toTypedArray()
            var c = KaraokeGrader.Criteria(array)
            criteriaList.add(c)
        }
        var criteriaArray = criteriaList.toTypedArray()


        val jsonSections = this.getAssets().open("grdingSection.json").reader(charset=Charsets.UTF_8).use{it.readText()}
        val sectionsRoot = Gson().fromJson(jsonSections, Sections::class.java)

        var gradingSectionArray = sectionsRoot.sections.toTypedArray()

        graderInfo = KaraokeGrader.GraderInfo(1, arrayOf(0.0,0.0),criteriaArray,gradingSectionArray)

        initPianoRollView()

        startTime = System.currentTimeMillis()

        var thread = Thread(object:Runnable{
            override fun run() {
                while (true){

                    val current = System.currentTimeMillis()
                    val elapsedTime = current - startTime
                    pianoRollViewRenderer.playTime = elapsedTime.toFloat()/1000.0f
                    Thread.sleep(20)
                }
            }
        })
        thread.start()
//
//        handler.post(object:Runnable{
//            override fun run() {
//                while (true){
//
//                    val current = System.currentTimeMillis()
//                    val elapsedTime = current - startTime
//                    pianoRollViewRenderer.playTime = elapsedTime.toFloat()/1000.0f
//                    Thread.sleep(20)
//                }
//            }
//        })
    }
    private fun initPianoRollView(){
        binding.pianorollview.setEGLContextClientVersion(3)
        binding.pianorollview.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        //binding.pianorollview.getHolder().setFormat(PixelFormat.TRANSPARENT);
        binding.pianorollview.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        binding.pianorollview.setZOrderOnTop(true)
        pianoRollViewRenderer = PianoRollViewRenderer(this)
        pianoRollViewRenderer.graderInfo = graderInfo
        pianoRollViewRenderer.playTime = 0.0f
        // Set the Renderer for drawing on the GLSurfaceView
        binding.pianorollview.setRenderer(pianoRollViewRenderer)
    }
}