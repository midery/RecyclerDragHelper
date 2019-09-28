package com.liarstudio.recycler_drag

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.liarstudio.recycler_drag.controller.DraggableController
import com.liarstudio.recycler_drag.controller.SeparatrorController
import com.liarstudio.recycler_drag_helper.RecyclerDragHelper
import kotlinx.android.synthetic.main.activity_main.*
import ru.surfstudio.android.easyadapter.EasyAdapter
import ru.surfstudio.android.easyadapter.ItemList

/**
 * Main Screen
 */
class MainActivity : AppCompatActivity() {

    private val screenModel = MainScreenModel()
    private lateinit var dragHelper: RecyclerDragHelper

    private val adapter = EasyAdapter()
    private val draggableController = DraggableController(
        onDragStarted = { dragHelper.initializeDrag(it) }
    )
    private val separatorController = SeparatrorController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
    }

    private fun initViews() {
        content_rv.layoutManager = LinearLayoutManager(this)
        content_rv.adapter = adapter
        dragHelper = RecyclerDragHelper(
            content_rv,
            ::onDragStarted,
            ::onDragged,
            ::onDragCompleted,
            ::onDragCompleted
        )
        content_rv.setOnDragListener(dragHelper.onDragListener)
        toolbar.setOnDragListener(dragHelper.onDragListener)
        recycler_drag_bottom.setOnDragListener(dragHelper.onDragListener)
        render(screenModel)
    }

    private fun onDragStarted(id: String) {
        screenModel.startDrag(id)
        render(screenModel)
    }

    private fun onDragged(id: String, isPlacedUp: Boolean) {
        screenModel.updateDragState(id, isPlacedUp)
    }

    private fun onDragCompleted() {
        screenModel.endDrag()
        render(screenModel)
    }

    fun render(screenModel: MainScreenModel) {
        adapter.setItems(createItemList(screenModel))
    }

    private fun createItemList(screenModel: MainScreenModel): ItemList {
        val itemList = ItemList.create()
        screenModel.draggableElements.forEachIndexed { i, item ->
            itemList.add(i, separatorController)
            itemList.add(item, draggableController)
        }
        return itemList
    }
}
