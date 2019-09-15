package com.liarstudio.recycler_drag_helper.viewholder

/**
 * Интерфейс-маркер для ViewHolder или View, которые участвуют в механизме Drag'N'Drop
 * Этот элемент должен подсвечиваться, когда на него перетаскивают другой,
 * либо подсвечивать позицию, в которую будет помещен перемещаемый View
 */
interface DragSeparator {

    /**
     * Подсветка элемента
     */
    fun highlight()

    /**
     * Очистка подсветки (возврат в обычное состояние)
     */
    fun clearHighlight()
}
