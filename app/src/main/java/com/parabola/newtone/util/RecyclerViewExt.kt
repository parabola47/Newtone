package com.parabola.newtone.util

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


/**
 * Производит скроллинг к [position], только если первый видимый элемент больше [position].
 * То есть если текущая позиция верхнего видимого элемента ниже [position].
 * Использовать только в случае, если [RecyclerView.getLayoutManager] имеет тип [LinearLayoutManager]
 *
 * @param position позиция, к которой будет производиться скроллинг
 * @return true, если в результате был выполнен скроллинг; false, если нет
 */
fun RecyclerView.scrollUp(position: Int): Boolean {
    if (layoutManager !is LinearLayoutManager) {
        return false
    }

    val linearLayoutManager = layoutManager as LinearLayoutManager
    val firstVisibleItemPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition()

    if (firstVisibleItemPosition > position) {
        scrollToPosition(position)
        return true
    }

    return false
}


fun RecyclerView.smoothScrollToTop() {
    smoothScrollToPosition(0)
}


// Отображаемое на экране количество элементов.
// Если элемент виден только частично, то он тоже считается видимым
fun LinearLayoutManager.visibleItemsCount(): Int =
    findLastVisibleItemPosition() - findFirstVisibleItemPosition()
