package jp.co.xing.pianorollviewtest

import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.gson.Gson
import jp.co.brother.rex.KaraokeGrader
import jp.co.xing.karaokejoysound.gles.renderer.PianoRollViewRenderer
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
        val jsonCriteria = this.getAssets().open("sampledata/criteria.json").reader(charset=Charsets.UTF_8).use{it.readText()}
        val criteriaRoot = Gson().fromJson(jsonCriteria, CriteriaRoot::class.java)

        var criteriaList:MutableList<KaraokeGrader.Criteria> = mutableListOf()
        for( criteria in criteriaRoot.criterias){
            var array = criteria.notes.toTypedArray()
            var c = KaraokeGrader.Criteria(array)
            criteriaList.add(c)
        }
        var criteriaArray = criteriaList.toTypedArray()


        val jsonSections = this.getAssets().open("sampledata/grdingSection.json").reader(charset=Charsets.UTF_8).use{it.readText()}
        val sectionsRoot = Gson().fromJson(jsonSections, Sections::class.java)

        var gradingSectionArray = sectionsRoot.sections.toTypedArray()

        graderInfo = KaraokeGrader.GraderInfo(1, arrayOf(0.0,0.0),criteriaArray,gradingSectionArray)


        val jsonDetectedPitch = this.getAssets().open("sampledata/detectedPitch.json").reader(charset=Charsets.UTF_8).use{it.readText()}
        val jsonDetectedPitchRoot = Gson().fromJson(jsonDetectedPitch, DetectedPitch::class.java)

        val jsonDetectedEvent = this.getAssets().open("sampledata/detectedEvent.json").reader(charset=Charsets.UTF_8).use{it.readText()}
        val jsonDetectedEventRoot = Gson().fromJson(jsonDetectedEvent, DetectedEvent::class.java)

        var judgmentBlocks = getJudgmentBlocks()
        //for(b in judgmentBlocks){
        //    Log.d("MainActivity",b.toString())
        //}

        initPianoRollView()

        startTime = System.currentTimeMillis()

        var thread = Thread(object:Runnable{
            override fun run() {
                var startIndex = 0
                var eventStartIndex = 0
                var judgmentStartIndex = 0
                while (true){

                    val current = System.currentTimeMillis()
                    val elapsedTime = current - startTime
                    var playTime = elapsedTime.toFloat()/1000.0f

                    pianoRollViewRenderer.playTime = playTime

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
                    if( playTime > 0.31 ){
                        for(i in eventStartIndex until jsonDetectedEventRoot.detectedEvent.count()){
                            var detectedEvent = jsonDetectedEventRoot.detectedEvent[i]
                            if(detectedEvent.time < playTime - 0.31f - 0.2f){
                                pianoRollViewRenderer.addDetectedEvent(detectedEvent.time,detectedEvent.vocalNo,detectedEvent.vocalNo,detectedEvent.type)
                                eventStartIndex = i + 1
                            }
                            else{
                                break;
                            }
                        }
                        for(i in judgmentStartIndex until judgmentBlocks.count()){
                            var j = judgmentBlocks[i]
                            if(j.noteInfo.time < playTime - 0.31f ){
                                j.matched = 1
                                j.fixedTime = playTime - 0.31f
                                pianoRollViewRenderer.addjudgmentBlock(j)
                                judgmentStartIndex = i + 1
                                //Log.d("MainActivity",j.toString())
                            }
                            else{
                                break;
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
    private fun getJudgmentBlocks():MutableList<PianoRollViewRenderer.BlockInfo>{
        var result:MutableList<PianoRollViewRenderer.BlockInfo> = mutableListOf()
        for((tehonNumber, criteria) in graderInfo.criteria.withIndex()){
            for(noteInfo in criteria.notes){
                var blockLength = 0.31f
                var blockCount = (noteInfo.duration / blockLength).toInt()
                var blockRemain = noteInfo.duration % blockLength
                for(blockIndex in 0 until blockCount){
                    var time = noteInfo.time + blockIndex * blockLength
                    result.add(
                        PianoRollViewRenderer.BlockInfo(
                            tehonNumber,
                            KaraokeGrader.Mora(
                                time,
                                blockLength.toDouble(),
                                noteInfo.note,
                                noteInfo.flag
                            ),
                            false,
                            0,
                            0.0f)
                    )
                }
                if(blockRemain > 0.0f){
                    var time = noteInfo.time + blockCount * blockLength
                    result.add(
                        PianoRollViewRenderer.BlockInfo(
                            tehonNumber,
                            KaraokeGrader.Mora(
                                time,
                                blockRemain.toDouble(),
                                noteInfo.note,
                                noteInfo.flag
                            ),
                            blockCount > 0 && blockRemain < 0.15f,
                            0,
                            0.0f)
                    )
                }
            }
        }
        return result
    }
}