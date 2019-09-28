package com.liarstudio.recycler_drag.base

class DragModel(
    var sourceItemId: String = EMPTY_DRAG_ID,
    var targetItemId: String = EMPTY_DRAG_ID,
    var isPlacedUp: Boolean = false
) {
    /**
     * Проверка на одинаковый id у целевого и перетаскиваемого элементов
     */
    val hasSameId get() = sourceItemId == targetItemId

    /**
     * Проверка на то, было ли начато перетаскивание
     */
    val isInitialized get() = sourceItemId != EMPTY_DRAG_ID && targetItemId != EMPTY_DRAG_ID

    /**
     * Очистка модели
     */
    fun clear() {
        sourceItemId = EMPTY_DRAG_ID
        targetItemId = EMPTY_DRAG_ID
        isPlacedUp = false
    }

    companion object {
        private const val EMPTY_DRAG_ID = "EMPTY_DRAG_ID"
    }
}