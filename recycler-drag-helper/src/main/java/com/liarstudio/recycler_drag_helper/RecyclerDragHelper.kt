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
 * Помощник в механизме Drag'N'Drop, осуществляемом на [RecyclerView]
 *
 * @param recyclerView внутри которого происходит перетаскивание элементов
 * @param onDragStarted действие, выполняемое после начала перетаскивания
 * @param onDragged действие, выполняемое во время перетаскивания
 * @param onDragCompleted действие, выполняемое после завершения перетаскивания
 * @param onDragCanceled действие, выполняемое при перетаскивании на неактивный элемент
 */
open class RecyclerDragHelper(
    private val recyclerView: RecyclerView,
    val onDragStarted: (id: Long) -> Unit,
    val onDragged: (id: Long, isPlacedUp: Boolean) -> Unit,
    val onDragCompleted: () -> Unit,
    val onDragCanceled: () -> Unit
) {

    private var lastDragId = -1L // идентификатор последнего перетаскиваемого элемента
    private var lastScrollTime = -1L // время последнего скролла (в мс)
    private var lastScrollDuration = -1 // длительность последнего перетаскивания
    private var lastHighlightPosition = -1 // последняя подсвеченная позиция
    private var isDragConsumed = false

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
     * Действие, выполняемое в начале Drag'N'Drop
     *
     * @param id идентификатор элемента, который будет перетаскиваться
     */
    fun initializeDrag(id: Long) {
        isDragConsumed = false
        lastDragId = id
        onDragStarted(id)
    }

    protected open fun isTopBorder(targetView: View): Boolean =
        targetView.id == R.id.recycler_drag_top || targetView is DragTop || targetView is Toolbar

    protected open fun isBottomBorder(targetView: View) =
        targetView.id == R.id.recycler_drag_bottom || targetView is DragBottom

    /**
     * Размер (в пикселях) скролла при движении внутри списка
     */
    protected open fun getSmallScrollSize(): Int = SMALL_SCROLL_SIZE

    /**
     * Размер (в пикселях) скролла при наведении на нижнюю или вернхюю границы
     */
    protected open fun getBorderScrollSize(): Int = MEDIUM_SCROLL_SIZE

    /**
     * Задержка на осуществление вычислений, необходимо для более плавного скролла
     */
    protected open fun getCalculationTimeout(): Int = CALCULATION_TIMEOUT

    private fun onDraggedInternal(dx: Float, dy: Float) {
        val llm = recyclerView.layoutManager as? LinearLayoutManager ?: return
        val view = recyclerView.findChildViewUnder(dx, dy) ?: return
        val viewHolder = recyclerView.findContainingViewHolder(view) ?: return
        if (viewHolder is Draggable && viewHolder.getId() != lastDragId) {
            val centerY = view.top + view.height / 2f // нахождение центра целевой view
            val shouldHighlightUp = dy > centerY // необходимо ли подсветить верхний контроллер
            val layoutPosition = viewHolder.layoutPosition
            repaintSpaces(llm, layoutPosition, shouldHighlightUp)
            onDragged(viewHolder.getId(), !shouldHighlightUp)
        }
    }

    private fun onDragFinishedInternal() {
        lastDragId = -1
        isDragConsumed = true
        onDragCompleted()
    }

    private fun onDragCanceledInternal() {
        if (!isDragConsumed) {
            isDragConsumed = true
            recyclerView.post { onDragCanceled() }
        }
    }

    /**
     * Перекраска пустых мест между элементами RecyclerView,
     * показывающая, в какое место попадет перемещаемый элемент
     */
    private fun repaintSpaces(llm: LinearLayoutManager, position: Int, isUp: Boolean) {
        val lastView = llm.findViewByPosition(lastHighlightPosition)
        if (lastView != null) {
            val viewHolder = recyclerView.findContainingViewHolder(lastView)
            if (viewHolder is DragSeparator) {
                viewHolder.clearHighlight()
            }
        }

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
     * Скролл recyclerView с учетом позиции перетаскиваемого элемента
     * и таймингов последнего перетаскивания
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

            if (isTopBorder(targetView)) { // Мы находимся в самом верху
                scrollWithTimingsUpdate(true, getBorderScrollSize())
            }

            if (isBottomBorder(targetView)) { // Мы находимся в самом низу
                scrollWithTimingsUpdate(false, getBorderScrollSize())
            }
        }
    }

    /**
     * Скролл с обновлением счетчиков времени
     * Происходит рассчет времени на анимацию, записывается текущее время, а затем происходит скролл
     */
    private fun scrollWithTimingsUpdate(isUp: Boolean, scrollSize: Int = getSmallScrollSize()) {
        val signedScrollSize = if (isUp) -scrollSize else scrollSize
        lastScrollDuration = computeScrollDuration(0, signedScrollSize)
        lastScrollTime = System.currentTimeMillis()
        recyclerView.smoothScrollBy(0, signedScrollSize)
    }

    /**
     * Вычисление длительности анимации скролла
     *
     * Исходный код взят из стандартного класса [RecyclerView]
     */
    private fun computeScrollDuration(dx: Int, dy: Int): Int {
        val absDx = abs(dx)
        val absDy = abs(dy)
        val horizontal = absDx > absDy
        val containerSize = recyclerView.height

        val duration: Int
        val absDelta = (if (horizontal) absDx else absDy).toFloat()
        duration = ((absDelta / containerSize + 1) * 300).toInt()
        return min(duration, 2000)
    }

    companion object {
        private const val SMALL_SCROLL_SIZE = 200
        private const val MEDIUM_SCROLL_SIZE = 300
        private const val CALCULATION_TIMEOUT = 250
    }
}
