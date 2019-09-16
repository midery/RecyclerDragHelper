package com.liarstudio.recycler_drag_helper.viewholder

/**
 * Marker interface for ViewHolder used to separate other elements during Drag'N'Drop.
 * This element should be highlighted, when dragged over,
 * or highlight position in which dragged View should be placed
 */
interface DragSeparator {

    /**
     * Highlight separator
     */
    fun highlight()

    /**
     * Clear highlight state (return to usual state)
     */
    fun clearHighlight()
}
