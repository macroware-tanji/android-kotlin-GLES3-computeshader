package jp.co.xing.gl.renderer

import android.content.Context
import android.opengl.GLES32
import android.opengl.GLSurfaceView
import android.util.Log
import androidx.core.math.MathUtils
import jp.co.brother.rex.KaraokeGrader
import jp.co.xing.gl.util.*
import jp.co.xing.pianorollviewtest.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PianoRollViewRenderer(context: Context): GLSurfaceView.Renderer {

    private enum class NoteType{
        EXAMPLE1_NOTE,
        EXAMPLE2_NOTE,
        SONG_MATCH,
        SONG_MISMATCH,
        SONG_MATCH_EXAMPLE2,
        SONG_MISMATCH_EXAMPLE2,
    }

    private var context: Context
    //private var width:Int=0
    //private var height:Int=0
    private var viewSize = Vec2(0.0f,0.0f)
    private var pianoRollSize = Vec2(0.0f,0.0f)
    private var noteHeight:Float=0.0f
    private var offset = Vec2(0.0f,0.0f)
    private var rectWhole = Rect(0.0f,0.0f,0.0f,0.0f)
    private var rectPianoRoll = Rect(0.0f,0.0f,0.0f,0.0f)
    private lateinit var margins:Array<Float>
    private var sectionWidth: Float = 0.0f
    private var currSectionNumber:Int = -1
    private var prevSectionNumber:Int = -1
    private lateinit var section:KaraokeGrader.GradingSection
    private var sectionSpeed:Float = 0.0f

    private val SECTION_MARGIN = 20.0f
    private val VERTICAL_LINE_WIDTH = 3.0f
    private val HORIZONTAL_LINE_HEIGHT = 3.0f

    private lateinit var texMapBK:TexMap0
    private lateinit var texMapSectionLine:TexMap0
    private lateinit var texProgressBar:TexMap0
    private lateinit var rectangle:Rectangle

    private val COLOR_OUT_OF_SECTION = Color(0.0f,0.0f,0.0f,0.4f)
    private val COLOR_EXAMPLE1_NOTE = Color(0x94/255.0f, 0x97/255.0f,0x9a/255.0f,1.0f)
    private val COLOR_EXAMPLE2_NOTE = Color(0x61/255.0f, 0x75/255.0f,0x8a/255.0f,1.0f)
    private val COLOR_SONG_MATCH = Color(0x02/255.0f, 0xff/255.0f,0xc2/255.0f,1.0f)
    private val COLOR_SONG_MISMATCH = Color(0xcb/255.0f, 0x31/255.0f,0x02/255.0f,1.0f)
    private val COLOR_SONG_MATCH_EXAMPLE2 = Color(0x41/255.0f, 0x69/255.0f,0xe1/255.0f,1.0f)
    private val COLOR_SONG_MISMATCH_EXAMPLE2 = Color(0xcb/255.0f, 0x31/255.0f,0x02/255.0f,1.0f)

    var graderInfo: KaraokeGrader.GraderInfo = KaraokeGrader.GraderInfo(0,
        arrayOf(0.0,0.0),
        arrayOf<KaraokeGrader.Criteria>(),
        arrayOf<KaraokeGrader.GradingSection>())
        set(value){
            field = value
            onSetGraderInfo(value)
        }

    private var elapsedTime_:Float = 0.0f
        get() {
            var currentTimeMillis = System.currentTimeMillis()/1000.0f
            if(elapsedStartTime < 0.0f){
                elapsedStartTime = currentTimeMillis
            }
            return currentTimeMillis - elapsedStartTime
        }
    private var elapsedStartTime:Float = -1.0f
    private var elapsedTime:Float = 0.0f

    private var introEndTime:Float = 0.0f
    private var outroStartTime:Float = 0.0f
    private var centerNote:Int = 0
    private var adjustScrollValue:Float = 0.0f
    private var scrollStartTime:Float = 0.0f
    private val SCROLL_TIME = 0.1f
    private var adjustStartPosition:Float = 0.0f
    private var dyScroll:Float = 0.0f
    private lateinit var notes1InSection:Array<KaraokeGrader.Mora>
    private lateinit var notes2InSection:Array<KaraokeGrader.Mora>

    var playTime: Float = 0.0f

    init {
        this.context = context
    }
    private fun getSectionByTime(time:Float): Int {
        for(i in 0 until graderInfo.gradingSections.count()){
            val sec = graderInfo.gradingSections[i]
            if(sec.head <= time && time <= sec.tail){
                return i
            }
            if(i==0){
                if(sec.head - 4.0f < time && time < sec.head){
                    return 0
                }
            }
            if( i < graderInfo.gradingSections.count() - 1 ){
                val nextSec = graderInfo.gradingSections[i+1]
                if(sec.tail < time && time < nextSec.head && nextSec.head - 4.0f < time){
                    return i+1
                }
            }
        }
        return -1
    }

    private fun onSetGraderInfo(graderInfo: KaraokeGrader.GraderInfo){
        var firstNote = graderInfo.criteria[0].notes[0]
        if(graderInfo.criteria.count()>1){
            if(firstNote.time > graderInfo.criteria[1].notes[0].time){
                firstNote = graderInfo.criteria[1].notes[0]
            }
            var lastNote1 = graderInfo.criteria[0].notes.last()
            var lastNote2 = graderInfo.criteria[1].notes.last()
            outroStartTime = arrayOf(lastNote1.time + lastNote1.duration,lastNote2.time + lastNote2.duration).max().toFloat()
        }
        else{
            var lastNote1 = graderInfo.criteria[0].notes.last()
            outroStartTime = (lastNote1.time + lastNote1.duration).toFloat()
        }
        introEndTime = firstNote.time.toFloat()
        centerNote = firstNote.note

    }

    fun getNotesInRange(start:Float, end:Float, tehonNo:Int) : Array<KaraokeGrader.Mora> {
        if( tehonNo < graderInfo.criteria.size ){
            var notes = graderInfo.criteria[tehonNo].notes.filter {
                start < it.time + it.duration && it.time < end && it.note >= 2 && it.note <=125 && it.flag!=2
            }
            return notes.toTypedArray()
        }
        return arrayOf()
    }

    fun getCurrentNotes(currentTime:Float,margin:Float,fixed:Boolean): List<KaraokeGrader.Mora> {
        var notes:MutableList<KaraokeGrader.Mora> = mutableListOf()

        for(note in notes1InSection){
            if(note.time <= currentTime && currentTime <= note.time + note.duration + margin){
                notes.add(note)
            }
        }
        for(note in notes2InSection){
            if(note.time <= currentTime && currentTime <= note.time + note.duration + margin){
                notes.add(note)
            }
        }

        return notes
    }

    private fun smoothstep(edge0:Float,edge1:Float, value:Float): Float {
        var x = MathUtils.clamp((value-edge0)/(edge1-edge0),0.0f,1.0f)
        return x * x * x
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        // Set the background frame color
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        texMapBK = TexMap0(context, R.drawable.pianoroleback_3x)
        texMapSectionLine = TexMap0(context, R.drawable.sectionbar_3x)
        texProgressBar = TexMap0(context, R.drawable.pianorole_cover_3x)
        rectangle = Rectangle(context)
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        viewSize.x = width.toFloat()
        viewSize.y = height.toFloat()
        Log.d("PianoRollViewRenderer","surface width:%d height:%d".format(width,height))
        GLES32.glViewport(0, 0, width, height)

        pianoRollSize.x = viewSize.x
        pianoRollSize.y = viewSize.y * 0.72f
        noteHeight = (pianoRollSize.y - HORIZONTAL_LINE_HEIGHT * 13.0f)/12.0f

        margins = arrayOf(SECTION_MARGIN,viewSize.x-SECTION_MARGIN)
        sectionWidth = margins[1] - margins[0] - VERTICAL_LINE_WIDTH * 2.0f

        offset.x = margins[0] + VERTICAL_LINE_WIDTH
        offset.y = (viewSize.y - pianoRollSize.y)/2.0f

        rectWhole = Rect(0.0f,0.0f,viewSize.x,viewSize.y)
        rectPianoRoll = Rect(x=0.0f,
            y=offset.y + HORIZONTAL_LINE_HEIGHT,
            w=viewSize.x,
            h=pianoRollSize.y - HORIZONTAL_LINE_HEIGHT*2.0f)

        adjustScrollValue = (centerNote-13).toFloat() * (noteHeight + HORIZONTAL_LINE_HEIGHT)/2.0f
    }

    override fun onDrawFrame(p0: GL10?) {
        elapsedTime = elapsedTime_
        GLES32.glBlendFunc(GLES32.GL_SRC_ALPHA, GLES32.GL_ONE_MINUS_SRC_ALPHA)
        GLES32.glEnable(GLES32.GL_BLEND)

        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT or GLES32.GL_DEPTH_BUFFER_BIT)
        //GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT or GLES32.GL_DEPTH_BUFFER_BIT)

        currSectionNumber = getSectionByTime(playTime)

        drawBackground()

        if( currSectionNumber >= 0 ){
            if(currSectionNumber != prevSectionNumber){
                section = graderInfo.gradingSections[currSectionNumber]
                sectionSpeed = sectionWidth / (section.tail - section.head).toFloat()
                prevSectionNumber = currSectionNumber

                notes1InSection = getNotesInRange(section.head.toFloat(),section.tail.toFloat(),0)
                notes2InSection = getNotesInRange(section.head.toFloat(),section.tail.toFloat(),1)
            }
            var notes = getCurrentNotes(playTime,0.0f,false)
            var scrollEndTime = scrollStartTime + SCROLL_TIME
            var isScrolling = elapsedTime < scrollEndTime
            if(isScrolling && elapsedTime > introEndTime){
                adjustScrollValue = Math.round(adjustStartPosition + dyScroll  * smoothstep(scrollStartTime,scrollEndTime,elapsedTime)).toFloat()
            }
            else{
                var diff = 0
                for(note in notes){
                    var sub = note.note - centerNote
                    if( sub > 9 ){
                        diff = sub - 9
                    }
                    else if(sub < -9){
                        diff = sub + 9
                    }
                }
                if(diff !=0 ){
                    centerNote += diff
                    dyScroll = diff.toFloat() * (noteHeight+HORIZONTAL_LINE_HEIGHT)/2.0f
                    adjustStartPosition = adjustScrollValue
                    scrollStartTime = elapsedTime
                }
                else{
                    adjustScrollValue = (centerNote-13).toFloat() * (noteHeight+HORIZONTAL_LINE_HEIGHT)/2.0f
                }
            }
            for(note in notes1InSection ){
                drawNote(note.time.toFloat(), note.duration.toFloat(),note.note,NoteType.EXAMPLE1_NOTE,false, 0.0f)
            }
            for(note in notes2InSection ){
                drawNote(note.time.toFloat(), note.duration.toFloat(),note.note,NoteType.EXAMPLE2_NOTE,false, 0.0f)
            }
            drawSectionLines()
            drawOutOfSections()

            drawProgressBar()
        }
        else{
            drawSectionLines()
            drawOutOfSections()
        }
    }
    private fun drawBackground(){
        val pos = Vec2(0.0f,offset.y)
        texMapBK.draw(viewSize,pianoRollSize,pos)
    }
    private fun drawSectionLines(){
        val poss = arrayOf(Vec2(SECTION_MARGIN,offset.y), Vec2(viewSize.x-SECTION_MARGIN-VERTICAL_LINE_WIDTH,offset.y))
        val size = Vec2(VERTICAL_LINE_WIDTH,pianoRollSize.y)
        texMapSectionLine.draw(viewSize,size,poss[0])
        texMapSectionLine.draw(viewSize,size,poss[1])
    }
    private fun drawOutOfSections(){
        val poss = arrayOf(
            Vec2(0.0f,offset.y+HORIZONTAL_LINE_HEIGHT),
            Vec2(margins[1],offset.y+HORIZONTAL_LINE_HEIGHT)
        )
        val sizes = arrayOf(
            Vec2(margins[0],pianoRollSize.y-HORIZONTAL_LINE_HEIGHT*2.0f),
            Vec2(viewSize.x - margins[1],pianoRollSize.y-HORIZONTAL_LINE_HEIGHT*2.0f)
        )
        rectangle.draw(viewSize,sizes[0],poss[0],0.0f,COLOR_OUT_OF_SECTION)
        rectangle.draw(viewSize,sizes[1],poss[1],0.0f,COLOR_OUT_OF_SECTION)
    }
    private fun drawProgressBar(){
        if( currSectionNumber >= 0 && section.head <= playTime && playTime <= section.tail ){
            var bmpSize = texProgressBar.bmpSize
            var height = pianoRollSize.y-HORIZONTAL_LINE_HEIGHT*2.0f
            var size = Vec2(bmpSize.x * height/bmpSize.y,height)


            var x = offset.x + (playTime - section.head) * sectionSpeed

            var pos = Vec2(x.toFloat() - size.x,offset.y+HORIZONTAL_LINE_HEIGHT)
            texProgressBar.draw(viewSize,size,pos)
        }
    }
    private fun drawNote(start:Float,duration:Float, note:Int, type:NoteType, progressed:Boolean, baseTime:Float){
        if(start + duration < section.head || section.tail < start){
            return
        }
        val color = when (type) {
            NoteType.EXAMPLE1_NOTE -> COLOR_EXAMPLE1_NOTE
            NoteType.EXAMPLE2_NOTE -> COLOR_EXAMPLE1_NOTE
            NoteType.SONG_MATCH -> COLOR_SONG_MATCH
            NoteType.SONG_MISMATCH -> COLOR_SONG_MISMATCH
            NoteType.SONG_MATCH_EXAMPLE2 -> COLOR_SONG_MATCH_EXAMPLE2
            NoteType.SONG_MISMATCH_EXAMPLE2 -> COLOR_SONG_MISMATCH_EXAMPLE2
            else -> Color(1.0f,1.0f,1.0f,1.0f)
        }

        var startX = start - section.head.toFloat()
        var x = offset.x + startX * sectionSpeed
        var y = offset.y + pianoRollSize.y - (note * (noteHeight + HORIZONTAL_LINE_HEIGHT) / 2.0f - adjustScrollValue) - HORIZONTAL_LINE_HEIGHT/2.0f
        var width = duration * sectionSpeed
        var height = noteHeight + HORIZONTAL_LINE_HEIGHT
        var progress  =  if(progressed && start + duration > section.head) (playTime - baseTime)/duration else 1.0f

        rectangle.draw(viewSize,Vec2(width,height),Vec2(x,y),16.0f,color)
    }
}