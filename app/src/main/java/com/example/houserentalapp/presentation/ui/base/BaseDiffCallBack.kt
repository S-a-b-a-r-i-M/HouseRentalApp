package com.example.houserentalapp.presentation.ui.base

import androidx.recyclerview.widget.DiffUtil

abstract class BaseDiffCallBack<T> (
    private val oldList: List<LoadingAdapterData<T>>,
    private val newList: List<LoadingAdapterData<T>>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        return when {
            oldItem is LoadingAdapterData.Data && newItem is LoadingAdapterData.Data ->
                areDataItemsSame(oldItem.data, newItem.data)
            oldItem is LoadingAdapterData.Loader && newItem is LoadingAdapterData.Loader -> true
            else -> false
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    // Abstract method for subclasses to implement item comparison logic
    abstract fun areDataItemsSame(oldData: T, newData: T): Boolean
}