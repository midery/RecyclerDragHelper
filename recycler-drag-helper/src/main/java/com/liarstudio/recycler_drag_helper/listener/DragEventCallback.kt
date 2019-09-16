package com.liarstudio.recycler_drag_helper.listener

import android.view.DragEvent
import android.view.View

typealias DragEventCallback = (targetView: View, draggedView: View?, dragEvent: DragEvent) -> Unit
