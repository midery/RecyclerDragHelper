package com.liarstudio.recycler_drag_helper

import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.liarstudio.recycler_drag_helper.border.DragBottom
import com.liarstudio.recycler_drag_helper.border.DragTop
import com.liarstudio.recycler_drag_helper.listener.DragListener
import com.liarstudio.recycler_drag_helper.viewholder.DragSeparator
import com.liarstudio.recycler_drag_helper.viewholder.Draggable
import kotlin.math.abs
import kotlin.math.min

/**
 * Helper, used to simplify Drag'N'Drop logic in [RecyclerView].
 *
 * @param recyclerView which elements is used in drag operation
 * @param onDragStarted action called when drag is started
 * @param onDragged action called when dragged element is above element, registered in listener
 * @param onDragCompleted action called when drag operation is successfully completed
 * @param onDragCanceled action called when drag operation is cancelled (drag element is not consumed)
 */
open class RecyclerDragHelper(
    private val recyclerView: RecyclerView,
    val onDragStarted: (id: String) -> Unit,
    val onDragged: (id: String, isPlacedUp: Boolean) -> Unit,
    val onDragCompleted: () -> Unit,
    val onDragCanceled: () -> Unit
) {

    private var lastDragId = UNKNOWN_ID
    private var lastScrollTime = UNKNOWN_TIME
    private var lastScrollDuration = UNKNOWN_TIME
    private var lastHighlightPosition = UNKNOWN_POSITION
    private val isDragConsumed: Boolean
        get() = lastDragId == UNKNOWN_ID

    protected open var scrollAreaSize =
        recyclerView.context.resources.getDimensionPixelOffset(R.dimen.recycler_scroll_area_size)

    val onDragListener = DragListener(
        onDragLocation = { targetView, _, event ->
            scrollIfNecessary(targetView, event.y)
            onDraggedInternal(event.x, event.y)
        },

        onDrop = { _, _, _ -> onDragFinishedInternal() },
        onDragEnd = { _, _, _ -> onDragCanceledInternal() }
    )

    /**
     * Initialize drag and drop
     *
     * @param id id of dragged element
     */
    fun initializeDrag(id: String) {
        lastDragId = id
        onDragStarted(id)
    }

    /**
     * Check if view is drag top border and the element should move faster
     */
    protected open fun isTopBorder(targetView: View): Boolean =
        targetView.id == R.id.recycler_drag_top || targetView is DragTop || targetView is Toolbar

    /**
     * Check if view is drag bottom border and the element should move faster
     */
    protected open fun isBottomBorder(targetView: View) =
        targetView.id == R.id.recycler_drag_bottom || targetView is DragBottom

    /**
     * Get size of short scroll in pixels.
     *
     * Short scroll is used when an element is above RecyclerView and the scroll is moderate.
     */
    protected open fun getShortScrollSize(): Int = SMALL_SCROLL_SIZE

    /**
     * Get size of a medium scroll in pixels
     *
     * Medium scroll is used when an element is above top or bottom border and needs to move faster
     */
    protected open fun getBorderScrollSize(): Int = MEDIUM_SCROLL_SIZE

    /**
     * Get size of calculation timeout used to scroll smoother
     */
    protected open fun getCalculationTimeout(): Int = CALCULATION_TIMEOUT

    private fun onDraggedInternal(dx: Float, dy: Float) {
        val llm = recyclerView.layoutManager as? LinearLayoutManager ?: return
        val view = recyclerView.findChildViewUnder(dx, dy) ?: return
        val viewHolder = recyclerView.findContainingViewHolder(view) ?: return
        if (viewHolder is Draggable && viewHolder.getId() != lastDragId) {
            val centerY = view.top + view.height / 2f // find a center of a view
            val shouldHighlightUp = dy > centerY
            val layoutPosition = viewHolder.layoutPosition
            repaintSeparators(llm, layoutPosition, shouldHighlightUp)
            onDragged(viewHolder.getId(), !shouldHighlightUp)
        }
    }

    private fun onDragFinishedInternal() {
        clearVariables()
        onDragCompleted()
    }

    private fun onDragCanceledInternal() {
        if (!isDragConsumed) {
            clearVariables()
            recyclerView.post { onDragCanceled() }
        }
    }

    private fun clearVariables() {
        val llm = recyclerView.layoutManager as? LinearLayoutManager ?: return
        clearHighlightOnLastSeparator(llm)
        lastDragId = UNKNOWN_ID
        lastHighlightPosition = UNKNOWN_POSITION
        lastScrollDuration = UNKNOWN_TIME
        lastScrollTime = UNKNOWN_TIME
    }

    /**
     * Paints separators between draggable elements in RecyclerView
     * This separators are used to show the place draggable element will be placed
     */
    private fun repaintSeparators(llm: LinearLayoutManager, position: Int, isUp: Boolean) {
        clearHighlightOnLastSeparator(llm)
        highlightSeparatorOnPosition(llm, position, isUp)
    }

    private fun clearHighlightOnLastSeparator(llm: LinearLayoutManager) {
        val lastView = llm.findViewByPosition(lastHighlightPosition)
        if (lastView != null) {
            val viewHolder = recyclerView.findContainingViewHolder(lastView)
            if (viewHolder is DragSeparator) {
                viewHolder.clearHighlight()
            }
        }
    }

    private fun highlightSeparatorOnPosition(
        llm: LinearLayoutManager,
        position: Int,
        isUp: Boolean
    ) {
        lastHighlightPosition = if (isUp) position + 1 else position - 1

        val nextView = llm.findViewByPosition(lastHighlightPosition)
        if (nextView != null) {
            val viewHolder = recyclerView.findContainingViewHolder(nextView)
            if (viewHolder is DragSeparator) {
                viewHolder.highlight()
            }
        }
    }

    /**
     * Scroll RecyclerView.
     * Scroll size depends on the position of dragged element, targetView, and last scroll time.
     */
    private fun scrollIfNecessary(targetView: View, dy: Float) {
        val now = System.currentTimeMillis()

        val canScroll = now > (lastScrollTime + lastScrollDuration - getCalculationTimeout())

        if (canScroll) {
            if (targetView is RecyclerView) {
                when {
                    dy <= scrollAreaSize -> scrollWithTimingsUpdate(true)
                    targetView.height - dy <= scrollAreaSize -> scrollWithTimingsUpdate(false)
                }
            }

            if (isTopBorder(targetView)) {
                scrollWithTimingsUpdate(true, getBorderScrollSize())
            }

            if (isBottomBorder(targetView)) {
                scrollWithTimingsUpdate(false, getBorderScrollSize())
            }
        }
    }

    /**
     * Scroll with update of last time counters
     * Computes duration of scroll, gets current time, and then performs smooth scroll.
     */
    private fun scrollWithTimingsUpdate(isUp: Boolean, scrollSize: Int = getShortScrollSize()) {
        val signedScrollSize = if (isUp) -scrollSize else scrollSize
        lastScrollDuration = computeScrollDuration(0, signedScrollSize)
        lastScrollTime = System.currentTimeMillis()
        recyclerView.smoothScrollBy(0, signedScrollSize)
    }

    /**
     * Compute scroll duration
     *
     * Source code from [RecyclerView]s private method
     */
    private fun computeScrollDuration(dx: Int, dy: Int): Long {
        val absDx = abs(dx)
        val absDy = abs(dy)
        val horizontal = absDx > absDy
        val containerSize = recyclerView.height

        val duration: Int
        val absDelta = (if (horizontal) absDx else absDy).toFloat()
        duration = ((absDelta / containerSize + 1) * 300).toInt()
        return min(duration, 2000).toLong()
    }

    companion object {
        private const val SMALL_SCROLL_SIZE = 200
        private const val MEDIUM_SCROLL_SIZE = 300
        private const val CALCULATION_TIMEOUT = 250
        private const val UNKNOWN_POSITION = -1
        private const val UNKNOWN_TIME = -1L
        private const val UNKNOWN_ID = "unknown_drag_id"
    }
}
