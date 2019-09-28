package com.liarstudio.recycler_drag.drag.shadow

import android.graphics.*
import android.view.View
import com.liarstudio.recycler_drag.R

/**
 * [View.DragShadowBuilder] для [MediaItem]
 */
class MediaDragShadowBuilder(private val shadow: View) : View.DragShadowBuilder(shadow) {

    private var scale = 1f
    private val defaultScale = .75f
    private val roundedCornerRadius =
        shadow.context.resources.getDimensionPixelOffset(R.dimen.recycler_drag_radius).toFloat()
    private val maxHeight =
        shadow.context.resources.displayMetrics.heightPixels / 2f //максимальная ширина перетаскиваемого элемента (половина экрана)

    override fun onDrawShadow(canvas: Canvas?) {
        roundCanvasCorners(canvas)
        canvas?.scale(scale, scale)
        view.draw(canvas)
    }

    override fun onProvideShadowMetrics(outShadowSize: Point, outShadowTouchPoint: Point) {
        //Если высота уже уменьшенной объекта меньше, чем максимальная, уменьшаем до максимальной возможной
        scale = if (shadow.height * defaultScale > maxHeight) {
            maxHeight / shadow.height.toFloat()
        } else {
            defaultScale //иначе уменьшаем на дефолтное значения
        }
        val width = (shadow.width * scale).toInt()
        val height = (shadow.height * scale).toInt()
        outShadowSize.set(width, height)
        outShadowTouchPoint.set(outShadowSize.x / 2, outShadowSize.y / 2)
    }

    private fun roundCanvasCorners(canvas: Canvas?) {
        val clipPath = Path()
        val radius = roundedCornerRadius
        val padding = radius / 2
        val w = shadow.width * scale
        val h = shadow.height * scale
        val shadowRect = RectF(padding, padding, w - padding, h - padding)
        clipPath.addRoundRect(
            shadowRect,
            radius,
            radius,
            Path.Direction.CW
        )
        canvas?.clipPath(clipPath)
    }
}