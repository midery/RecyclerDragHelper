package com.liarstudio.recycler_drag

import com.liarstudio.recycler_drag.base.DragModel
import com.liarstudio.recycler_drag.drag.DragElement

class MainScreenModel {

    val draggableElements: MutableList<DragElement> = createDraggableList()

    private val dragModel = DragModel()

    fun startDrag(id: String) {
        dragModel.sourceItemId = id
        draggableElements.forEach {
            if (it.id == id) it.isDragged = true
        }
    }

    fun endDrag() {
        if (dragModel.isInitialized && !dragModel.hasSameId) {
            val sourcePosition = draggableElements.indexOfFirst { it.id == dragModel.sourceItemId }
            val source = draggableElements[sourcePosition]
            draggableElements.removeAt(sourcePosition)
            var targetPosition = draggableElements.indexOfFirst { it.id == dragModel.targetItemId }
            if (!dragModel.isPlacedUp) ++targetPosition
            draggableElements.add(targetPosition, source)
        }
        draggableElements.forEach { it.isDragged = false }
        dragModel.clear()
    }

    /**
     * Обновление состояния
     */
    fun updateDragState(targetId: String, isPlacedUp: Boolean) {
        dragModel.targetItemId = targetId
        dragModel.isPlacedUp = isPlacedUp
    }

    private fun createDraggableList(): MutableList<DragElement> {
        return (1..10).map { DragElement("Draggable #$it") }.toMutableList()
    }
}