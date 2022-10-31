package jp.co.xing.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.opengl.GLSurfaceView
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import jp.co.xing.pianorollviewtest.R

/**
 * TODO: document your custom view class.
 */
class PianoRollView : GLSurfaceView {

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs) {
        init(attrs, 0)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {

    }
}