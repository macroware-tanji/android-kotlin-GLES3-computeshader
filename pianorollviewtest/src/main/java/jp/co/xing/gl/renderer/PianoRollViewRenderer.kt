package jp.co.xing.gl.renderer

import android.content.Context
import android.opengl.GLES32
import android.opengl.GLSurfaceView
import android.util.Log
import androidx.core.math.MathUtils
import jp.co.brother.rex.KaraokeGrader
import jp.co.xing.gl.util.*
import jp.co.xing.pianorollviewtest.R
//import java.lang.Math.pow
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.concurrent.withLock
import kotlin.math.*

class PianoRollViewRenderer(context: Context): GLSurfaceView.Renderer {

    private val TAG = "PianoRollViewRenderer"

    private enum class NoteType{
        EXAMPLE1_NOTE,
        EXAMPLE2_NOTE,
        SONG_MATCH,
        SONG_MISMATCH,
        SONG_MATCH_EXAMPLE2,
        SONG_MISMATCH_EXAMPLE2,
        DEBUG_F0_EXAMPLE1,
        DEBUG_F0_EXAMPLE2,
    }
    private enum class EventType {
        KOBUSHI,
        SHAKURI,
        VIBRATO
    }

    private data class FOInfo(
        var notes: Array<Float> = arrayOf(0.0f,0.0f),
        var levels: Array<Float> = arrayOf(0.0f,0.0f),
        var time:Float = 0.0f,
        var fixed: Boolean = false,
        var enables:Array<Boolean> = arrayOf(false,false),
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FOInfo

            if (!notes.contentEquals(other.notes)) return false
            if (!levels.contentEquals(other.levels)) return false
            if (time != other.time) return false
            if (fixed != other.fixed) return false
            if (!enables.contentEquals(other.enables)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = notes.contentHashCode()
            result = 31 * result + levels.contentHashCode()
            result = 31 * result + time.hashCode()
            result = 31 * result + fixed.hashCode()
            result = 31 * result + enables.contentHashCode()
            return result
        }
    }

    private data class EventInfo(
        var type:EventType,
        var time:Float,
        var note:Int
    )

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
    private var judgmentTime:Float = 0.0f
    private var dxProgressBar:Float =0.0f

    private val SECTION_MARGIN = 20.0f
    private val VERTICAL_LINE_WIDTH = 3.0f
    private val HORIZONTAL_LINE_HEIGHT = 3.0f

    private lateinit var texMapBK:TexMap0
    private lateinit var texMapSectionLine:TexMap0
    private lateinit var texMapVibrato:TexMap0
    private lateinit var texMapShakuri:TexMap0
    private lateinit var texMapKobushi:TexMap0
    private lateinit var texProgressBar:TexMap0
    private lateinit var rectangle:Rectangle
    private var texMapNoteIndicator = mutableListOf<TexMap0>()


    private val COLOR_OUT_OF_SECTION = Color(0.0f,0.0f,0.0f,0.4f)
    private val COLOR_EXAMPLE1_NOTE = Color(0x94/255.0f, 0x97/255.0f,0x9a/255.0f,1.0f)
    private val COLOR_EXAMPLE2_NOTE = Color(0x61/255.0f, 0x75/255.0f,0x8a/255.0f,1.0f)
    private val COLOR_SONG_MATCH = Color(0x02/255.0f, 0xff/255.0f,0xc2/255.0f,1.0f)
    private val COLOR_SONG_MISMATCH = Color(0xcb/255.0f, 0x31/255.0f,0x02/255.0f,1.0f)
    private val COLOR_SONG_MATCH_EXAMPLE2 = Color(0x41/255.0f, 0x69/255.0f,0xe1/255.0f,1.0f)
    private val COLOR_SONG_MISMATCH_EXAMPLE2 = Color(0xcb/255.0f, 0x31/255.0f,0x02/255.0f,1.0f)

    private val COLOR_DEBUG_F0_EXAMPLE1 = Color(0.0f, 0.4f,0.8f,1.0f)
    private val COLOR_DEBUG_F0_EXAMPLE2 = Color(1.0f, 0.0f,0.8f,1.0f)

    private  val BLOCK_LENGTH = 310

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
            var currentTimeMillis = System.currentTimeMillis()

            if(elapsedStartTime < 0){
                elapsedStartTime = currentTimeMillis
            }
            return (currentTimeMillis - elapsedStartTime)/1000.0f
        }
    private var elapsedStartTime:Long = -1
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
    private var f0Infos:MutableList<FOInfo> = mutableListOf()
    private var eventQueue:MutableList<EventInfo> = mutableListOf()
    private var eventAnimationQueue:MutableMap<EventInfo,Float> = mutableMapOf()
    private var currentNotes:List<KaraokeGrader.Mora> = listOf()

    var playTime: Float = 0.0f

    private val lock = java.util.concurrent.locks.ReentrantLock()

    init {
        this.context = context
    }

    private fun Log2(v: Float): Float {
        return Math.log(v.toDouble()).toFloat() / Math.log(2.0).toFloat()
    }
    private fun repeat(t:Float, length:Float):Float{
        var v = t % length
        if(v < 0.0f){
            v += length
        }
        return v;
    }
    private fun frequencyToNote(freq: Float, baseFreq:Float = 440.0f, baseNote:Float = 69.0f): Float {
        return if( freq > 0.0f)  12.0f * Log2(freq / baseFreq) + baseNote else 0.0f;
    }
    private fun correctNote(note: Float, standard: Float): Float {
        return if (note > 0) standard - 6 + repeat(note - (standard - 6), 12.0f) else 0.0f
    }
    fun addDetectedPitch(frames:Int, vocalNo:Int, pitch:Array<Float>, dbfs:Array<Float>){
        lock.withLock {
            while(f0Infos.count() < frames + pitch.count()){
                f0Infos.add(FOInfo())
            }
            for(i in 0 until pitch.count()){
                f0Infos[frames+i].notes[vocalNo] = frequencyToNote(freq = pitch[i])
                f0Infos[frames+i].levels[vocalNo] = dbfs[i]
                f0Infos[frames+i].time = (frames + i) * 0.01f
                f0Infos[frames+i].fixed = false
                f0Infos[frames+i].enables[vocalNo] = true
            }
        }
    }
    fun addDetectedEvent(time:Float, vocalNo:Int, criteriaNo:Int, type:String){
        lock.withLock {
            var note = getEventNote(time)
            if(note > 0){
                var e = EventInfo(EventType.valueOf(type.uppercase()),time,note)
                var f = eventQueue.filter { it.time== e.time && it.type == e.type && it.note == e.note }
                if(f.isEmpty()){
                    eventQueue.add(e)
                    Log.d(TAG,e.toString())
                }
            }
        }
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

    private fun getEventNote(time:Float): Int {
        var judgementRange = (BLOCK_LENGTH * 10)/1000.0f
        var start = max(time,judgementRange) - judgementRange
        return getNoteInRange(start,time)
    }

    private fun getNoteInRange(start:Float, end:Float): Int {
        var f0note:Int = 0
        var f0s = f0Infos.filter { it.time in start..end }
        for(f0 in f0s.reversed()){
            f0note = round(f0.notes[0]).toInt()
            if(f0note>0){
                break
            }
        }
        var tehonNote = 0
        var tehonNotes1 = getTehonNotesInRange(start,end,0)
        if(!tehonNotes1.isEmpty()){
            tehonNote = tehonNotes1.last().note
        }
        if(tehonNote==0){
            var tehonNotes2 = getTehonNotesInRange(start,end,1)
            if(!tehonNotes2.isEmpty()){
                tehonNote = tehonNotes2.last().note
            }
        }
        if(f0note==0){
            return tehonNote
        }
        if(tehonNote==0){
            return f0note
        }
        if(abs(f0note -tehonNote)>=6){
            return tehonNote
        }
        return f0note
    }
    private fun getTehonNotesInRange(start:Float, end:Float, tehonNo:Int) : Array<KaraokeGrader.Mora> {
        if( tehonNo < graderInfo.criteria.size ){
            var notes = graderInfo.criteria[tehonNo].notes.filter {
                start < it.time + it.duration && it.time < end && it.note >= 2 && it.note <=125 && it.flag!=2
            }
            return notes.toTypedArray()
        }
        return arrayOf()
    }

    private fun getCurrentNotes(currentTime:Float, margin:Float, fixed:Boolean): List<KaraokeGrader.Mora> {
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

        texMapVibrato= TexMap0(context, R.drawable.img_vibrato_s_3x)
        texMapKobushi= TexMap0(context, R.drawable.img_kobushi_s_3x)
        texMapShakuri= TexMap0(context, R.drawable.img_shakuri_s_3x)

        var imgIndicatorIds = arrayOf(
            R.drawable.marker00_3x,
            R.drawable.marker01_3x,
            R.drawable.marker02_3x,
            R.drawable.marker03_3x,
            R.drawable.marker04_3x)

        for(i in imgIndicatorIds){
            var t = TexMap0(context,i)
            texMapNoteIndicator.add(t)
        }
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
        lock.withLock{
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

                    notes1InSection = getTehonNotesInRange(section.head.toFloat(),section.tail.toFloat(),0)
                    notes2InSection = getTehonNotesInRange(section.head.toFloat(),section.tail.toFloat(),1)
                }
                currentNotes = getCurrentNotes(playTime,0.0f,false)
                var scrollEndTime = scrollStartTime + SCROLL_TIME
                var isScrolling = elapsedTime < scrollEndTime
                if(isScrolling && elapsedTime > introEndTime){
                    adjustScrollValue = Math.round(adjustStartPosition + dyScroll  * smoothstep(scrollStartTime,scrollEndTime,elapsedTime)).toFloat()
                }
                else{
                    var diff = 0
                    for(note in currentNotes){
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

                if(f0Infos.count() >= BLOCK_LENGTH){
                    judgmentTime = max((min(playTime,f0Infos.last().time)*100)/100.0f,BLOCK_LENGTH/100.0f) - BLOCK_LENGTH/100.0f
                }

                drawSectionLines()
                drawOutOfSections()

                drawF0()
                drawEvent()
                drawProgressBar()
                drawNoteIndicator()
            }
            else{
                drawSectionLines()
                drawOutOfSections()
            }
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


            var x = offset.x + (playTime - section.head.toFloat()) * sectionSpeed
            dxProgressBar = x;

            var pos = Vec2(x.toFloat() - size.x,offset.y+HORIZONTAL_LINE_HEIGHT)
            texProgressBar.draw(viewSize,size,pos)
        }
    }
    private fun drawNoteIndicator(){
        val indexes = arrayOf(0,1,2,3,4,3,2,1)
        for(currentNote in currentNotes){
            var waitTime = 80
            var noteIndicatorIndex = indexes[((elapsedTime * 1000).toInt() % (indexes.count() * waitTime))/waitTime]
            var dy = offset.y + pianoRollSize.y - (currentNote.note * (noteHeight+HORIZONTAL_LINE_HEIGHT)/2.0f - adjustScrollValue)

            var texPos = Vec2(
                dxProgressBar - texMapNoteIndicator[noteIndicatorIndex].bmpSize.x/2.0f,
                dy - (texMapNoteIndicator[noteIndicatorIndex].bmpSize.y - noteHeight)/2.0f)
            texMapNoteIndicator[noteIndicatorIndex].draw(viewSize,texPos)
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
            NoteType.DEBUG_F0_EXAMPLE1 -> COLOR_DEBUG_F0_EXAMPLE1
            NoteType.DEBUG_F0_EXAMPLE2 -> COLOR_DEBUG_F0_EXAMPLE2
            else -> Color(1.0f,1.0f,1.0f,1.0f)
        }

        var startX = start - section.head.toFloat()
        var x = offset.x + startX * sectionSpeed
        var y = offset.y + pianoRollSize.y - (note * (noteHeight + HORIZONTAL_LINE_HEIGHT) / 2.0f - adjustScrollValue) - HORIZONTAL_LINE_HEIGHT/2.0f
        var width = duration * sectionSpeed
        var height = noteHeight + HORIZONTAL_LINE_HEIGHT
        var progress  =  if(progressed && start + duration > section.head) (playTime - baseTime)/duration else 1.0f

        var edge = noteHeight/2.0f

        rectangle.draw(viewSize,Vec2(width,height),Vec2(x,y),edge,color)
    }
    private fun drawF0(){
        val startIndex = (section.head*100).toInt()
        if(startIndex < f0Infos.count()){
            val endIndex = arrayOf( (section.tail*100).toInt() + 1,f0Infos.count()-1).min()
            var types = arrayOf(NoteType.DEBUG_F0_EXAMPLE1,NoteType.DEBUG_F0_EXAMPLE2)
            for(i in startIndex .. endIndex){
                var f0 = f0Infos[i]
                for(j in 0 until graderInfo.vocalCount ){
                    drawNote(i*0.01f,0.01f,f0.notes[j].toInt(),types[j],false,0.0f)
                }
            }
        }
    }
    private fun drawEvent(){
        val secPreFrame = 1001.0f/30000.0f
        val coeff = (noteHeight+HORIZONTAL_LINE_HEIGHT) * 8.0f / (secPreFrame*8.0f).pow(2.0f)
        val interceptX = -secPreFrame * 8.0f
        val periods = arrayOf<Float>(0.0f,12.0f,16.0f,20.0f,24.0f)
        val animStartOffsetY = -(abs(coeff) * (secPreFrame * 4.0f).pow(2.0f))

        eventQueue = eventQueue.filter { it.time > section.head && currSectionNumber >= 0 } as MutableList<EventInfo>
        eventAnimationQueue = eventAnimationQueue.filter { it.key.time > section.head && currSectionNumber>=0 } as MutableMap<EventInfo, Float>

        for(e in eventQueue){
            if(e.time >= judgmentTime){
                continue
            }
            if(e.time < section.head){
                continue
            }
            var dy = 0.0f
            var texHeight = 0.0f
            var texWidth = 0.0f
            var texMap:TexMap0
            when(e.type){
                EventType.VIBRATO->{
                    texHeight = texMapVibrato.bmpSize.y
                    texWidth = texMapVibrato.bmpSize.x
                    dy = offset.y + pianoRollSize.y - (e.note.toFloat() * (noteHeight + HORIZONTAL_LINE_HEIGHT)/2.0f - adjustScrollValue) - texHeight/2.0f
                    texMap = texMapVibrato
                }
                EventType.KOBUSHI->{
                    texHeight = texMapKobushi.bmpSize.y
                    texWidth = texMapKobushi.bmpSize.x
                    dy = offset.y + pianoRollSize.y - (e.note.toFloat() * (noteHeight + HORIZONTAL_LINE_HEIGHT)/2.0f - adjustScrollValue) - texHeight/2.0f
                    texMap = texMapKobushi
                }
                EventType.SHAKURI->{
                    texHeight = texMapShakuri.bmpSize.y
                    texWidth = texMapShakuri.bmpSize.x
                    dy = offset.y + pianoRollSize.y - (e.note.toFloat() * (noteHeight + HORIZONTAL_LINE_HEIGHT)/2.0f - adjustScrollValue) - texHeight/2.0f
                    texMap = texMapShakuri
                }
                else -> {
                    continue
                }
            }
            var animationElapsedTime = 0.0f
            var animationStartTime = eventAnimationQueue[e]
            if(animationStartTime != null){
                animationElapsedTime = playTime - animationStartTime
            }
            else{
                eventAnimationQueue[e] = playTime
                animationElapsedTime = 0.0f
            }
            if(animationElapsedTime<0.0f){
                continue
            }
            var expansionRate = 1.0f
            var animationOffsetY = 0.0f
            if(periods[0] * secPreFrame <= animationElapsedTime && animationElapsedTime < periods[1] * secPreFrame){
                var st = periods[0] * secPreFrame
                var ed = periods[1] * secPreFrame
                expansionRate=2.0f
                animationOffsetY = animStartOffsetY + coeff * (animationElapsedTime-st + interceptX).pow(2.0f)
            }
            else if(periods[1] * secPreFrame <= animationElapsedTime && animationElapsedTime < periods[2] * secPreFrame){
                var st = periods[1] * secPreFrame
                var ed = periods[2] * secPreFrame
                var stExpansionRate = 2.0f
                var edExpansionRate = 0.8f

                expansionRate = stExpansionRate + (edExpansionRate - stExpansionRate) * sin(PI.toFloat()/2.0f * (animationElapsedTime-st)/(ed-st))
            }
            else if(periods[2] * secPreFrame <= animationElapsedTime && animationElapsedTime < periods[3] * secPreFrame){
                var st = periods[2] * secPreFrame
                var ed = periods[3] * secPreFrame
                var stExpansionRate = 0.8f
                var edExpansionRate = 1.5f

                expansionRate = stExpansionRate + (edExpansionRate - stExpansionRate) * sin(PI.toFloat()/2.0f * (animationElapsedTime-st)/(ed-st))
            }
            else if(periods[3] * secPreFrame <= animationElapsedTime && animationElapsedTime < periods[4] * secPreFrame){
                var st = periods[3] * secPreFrame
                var ed = periods[4] * secPreFrame
                var stExpansionRate = 1.5f
                var edExpansionRate = 1.0f
                expansionRate = stExpansionRate + (edExpansionRate - stExpansionRate) * sin(PI.toFloat()/2.0f * (animationElapsedTime-st)/(ed-st))
            }
            else{
                expansionRate = 1.0f
            }
            var dx = offset.x + (e.time - section.head.toFloat()) * sectionSpeed
            var texSize = Vec2(texWidth*expansionRate,texHeight*expansionRate)
            var texPos = Vec2(dx - texSize.x/2.0f, dy + animationOffsetY - (texSize.y-noteHeight)/2.0f)
            texMap.draw(viewSize,texSize,texPos)
        }

    }
}