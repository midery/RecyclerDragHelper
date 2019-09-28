package com.liarstudio.recycler_drag.controller

import android.graphics.Color
import android.view.ViewGroup
import com.liarstudio.recycler_drag.R
import com.liarstudio.recycler_drag_helper.viewholder.DragSeparator
import ru.surfstudio.android.easyadapter.controller.BindableItemController
import ru.surfstudio.android.easyadapter.holder.BindableViewHolder

class SeparatrorController : BindableItemController<Int, SeparatrorController.Holder>() {

    override fun getItemId(data: Int) = data.toString()

    override fun createViewHolder(parent: ViewGroup) = Holder(parent)

    inner class Holder(parent: ViewGroup) :
        BindableViewHolder<Int>(parent, R.layout.list_item_separator), DragSeparator {

        override fun bind(data: Int) {
            clearHighlight()
        }

        override fun highlight() {
            itemView.setBackgroundResource(R.drawable.bg_separator)
        }

        override fun clearHighlight() {
            itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

}