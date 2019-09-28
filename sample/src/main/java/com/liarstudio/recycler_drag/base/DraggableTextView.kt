package com.liarstudio.recycler_drag.base

import android.content.Context
import android.util.AttributeSet
import android.view.DragEvent
import android.widget.TextView

class DraggableTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1
) : TextView(context, attrs, defStyleAttr) {

    override fun onDragEvent(event: DragEvent?): Boolean {
        return false
    }
}