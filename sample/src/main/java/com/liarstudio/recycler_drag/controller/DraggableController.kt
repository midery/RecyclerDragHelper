package com.liarstudio.recycler_drag.controller

import android.content.ClipData
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.liarstudio.recycler_drag.R
import com.liarstudio.recycler_drag.drag.DragElement
import com.liarstudio.recycler_drag.drag.shadow.MediaDragShadowBuilder
import com.liarstudio.recycler_drag_helper.viewholder.Draggable
import ru.surfstudio.android.easyadapter.controller.BindableItemController
import ru.surfstudio.android.easyadapter.holder.BindableViewHolder

class DraggableController(
    private val onDragStarted: (id: String) -> Unit
) : BindableItemController<DragElement, DraggableController.Holder>() {

    override fun getItemId(data: DragElement) = data.id

    override fun createViewHolder(parent: ViewGroup) = Holder(parent)

    inner class Holder(parent: ViewGroup) :
        BindableViewHolder<DragElement>(parent, R.layout.list_item_draggable), Draggable {

        private lateinit var element: DragElement

        init {
            itemView.setOnLongClickListener {
                val clipData = ClipData.newPlainText("", "")
                val shadowBuilder =
                    MediaDragShadowBuilder(itemView)
                ViewCompat.startDragAndDrop(itemView, clipData, shadowBuilder, null, 0)
                itemView.alpha = 0.2f
                onDragStarted(getId())
                return@setOnLongClickListener true
            }

        }

        override fun bind(data: DragElement) {
            element = data
            (itemView as TextView).text = data.id
            itemView.alpha = if (data.isDragged) .2f else 1f
        }

        override fun getId(): String = element.id
    }

}