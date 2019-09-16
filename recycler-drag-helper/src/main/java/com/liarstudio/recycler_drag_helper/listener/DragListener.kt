package com.liarstudio.recycler_drag_helper.listener

import android.view.DragEvent
import android.view.View

/**
 * Kotlin-style [View.OnDragListener] wrapper to consume [DragEvent] simple.
 * The main idea and realization is taken from [https://github.com/OOOZH/DragAndDropIT]
 */
class DragListener constructor(
    private val onDragStart: DragEventCallback? = null,
    private val onDragEnd: DragEventCallback? = null,
    private val onDragLocation: DragEventCallback? = null,
    private val onDrop: DragEventCallback? = null,
    private val onEntered: DragEventCallback? = null,
    private val onExited: DragEventCallback? = null
) : View.OnDragListener {

    override fun onDrag(view: View, dragEvent: DragEvent): Boolean {

        val dragAction = dragEvent.action
        val dragView = dragEvent.localState as? View

        when (dragAction) {
            DragEvent.ACTION_DRAG_STARTED -> {
                onDragStart?.invoke(view, dragView, dragEvent)
            }
            DragEvent.ACTION_DRAG_LOCATION -> {
                onDragLocation?.invoke(view, dragView, dragEvent)
            }
            DragEvent.ACTION_DROP -> {
                onDrop?.invoke(view, dragView, dragEvent)
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                onDragEnd?.invoke(view, dragView, dragEvent)
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                onEntered?.invoke(view, dragView, dragEvent)
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                onExited?.invoke(view, dragView, dragEvent)
            }
        }
        return true
    }
}
