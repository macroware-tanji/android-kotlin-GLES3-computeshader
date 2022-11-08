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

data class DetectedPitchItem(var frames:Int, var vocalNo:Int, var pitch:List<Float>, var dbfs:List<Float>)
data class DetectedPitch(var detected:List<DetectedPitchItem>)

data class DetectedEventItem(var time:Float, var vocalNo:Int, var criteriaNo:Int, var type:String)
data class DetectedEvent(var detectedEvent:List<DetectedEventItem>)


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    lateinit var pianoRollView:GLSurfaceView
    lateinit var pianoRollViewRenderer:PianoRollViewRenderer
    lateinit var graderInfo:KaraokeGrader.GraderInfo

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


        val jsonDetectedPitch = this.getAssets().open("detectedPitch.json").reader(charset=Charsets.UTF_8).use{it.readText()}
        val jsonDetectedPitchRoot = Gson().fromJson(jsonDetectedPitch, DetectedPitch::class.java)

        val jsonDetectedEvent = this.getAssets().open("detectedEvent.json").reader(charset=Charsets.UTF_8).use{it.readText()}
        val jsonDetectedEventRoot = Gson().fromJson(jsonDetectedEvent, DetectedEvent::class.java)

        initPianoRollView()

        startTime = System.currentTimeMillis()

        var thread = Thread(object:Runnable{
            override fun run() {
                var startIndex = 0
                var eventStartIndex = 0
                while (true){

                    val current = System.currentTimeMillis()
                    val elapsedTime = current - startTime
                    pianoRollViewRenderer.playTime = elapsedTime.toFloat()/1000.0f
                    for(i in startIndex until jsonDetectedPitchRoot.detected.count()){
                        var detectedPitch = jsonDetectedPitchRoot.detected[i]
                        if((detectedPitch.frames + detectedPitch.pitch.count()) * 0.01f + 0.01f < pianoRollViewRenderer.playTime){
                            pianoRollViewRenderer.addDetectedPitch(
                                detectedPitch.frames,
                                detectedPitch.vocalNo,
                                detectedPitch.pitch.toTypedArray(),
                                detectedPitch.dbfs.toTypedArray())
                            startIndex = i + 1
                        }
                        else{
                            break;
                        }
                    }
                    if( elapsedTime > 0.31 ){
                        for(i in eventStartIndex until jsonDetectedEventRoot.detectedEvent.count()){
                            var detectedEvent = jsonDetectedEventRoot.detectedEvent[i]
                            if(detectedEvent.time < elapsedTime - 0.31 - 0.2){
                                pianoRollViewRenderer.addDetectedEvent(detectedEvent.time,detectedEvent.vocalNo,detectedEvent.vocalNo,detectedEvent.type)
                            }
                        }
                    }

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