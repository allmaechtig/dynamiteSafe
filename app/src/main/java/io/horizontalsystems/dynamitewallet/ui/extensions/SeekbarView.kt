package io.horizontalsystems.dynamitewallet.ui.extensions

import android.content.Context
import android.util.AttributeSet
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import io.horizontalsystems.dynamitewallet.R
import kotlinx.android.synthetic.main.view_seekbar.view.*

class SeekbarView : ConstraintLayout {

    init {
        inflate(context, R.layout.view_seekbar, this)
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun reset() {
        seekBar.progress = 2
    }

    fun bind(onProgressChanged: ((Int) -> Unit)) {

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                onProgressChanged.invoke(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

    }

}
