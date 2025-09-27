package com.example.houserentalapp.presentation.ui.base

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class BaseLoadingAdapter<T> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val LOADER_VIEW_TYPE = 1
        const val DATA_VIEW_TYPE = 2
    }

    // Data
    protected var itemList = mutableListOf<LoadingAdapterData<T>>()

    // Loading state management
    var isLoading: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                if (value) addLoader() else removeLoader()
            }
        }

    private fun addLoader() {
        if (itemList.lastOrNull() != LoadingAdapterData.Loader) {
            itemList.add(LoadingAdapterData.Loader)
            notifyItemInserted(itemList.size - 1)
        }
    }

    private fun removeLoader() {
        val lastIndex = itemList.indexOfLast { it == LoadingAdapterData.Loader }
        if (lastIndex != -1) {
            itemList.removeAt(lastIndex)
            notifyItemRemoved(lastIndex)
        }
    }

    // RecyclerView.Adapter partial implementation
    override fun getItemViewType(position: Int) = when(itemList[position]) {
        is LoadingAdapterData.Data -> DATA_VIEW_TYPE
        LoadingAdapterData.Loader -> LOADER_VIEW_TYPE
    }

    override fun getItemCount() = itemList.size

    open fun setDataList(newDataList: List<T>, hasMore: Boolean = false) {
        val newAdapterDataList: MutableList<LoadingAdapterData<T>> = newDataList.map {
            LoadingAdapterData.Data(it)
        }.toMutableList()
        if (hasMore) newAdapterDataList.add(LoadingAdapterData.Loader)

        val diffCallBack = createDiffCallBack(itemList, newAdapterDataList)
        val diffResult = DiffUtil.calculateDiff(diffCallBack)
        itemList.clear()
        itemList.addAll(newAdapterDataList)
        diffResult.dispatchUpdatesTo(this)
    }

    open fun addDataList(newDataList: List<T>) {
        val startPosition = itemList.size
        val newAdapterDataList = newDataList.map { LoadingAdapterData.Data(it) }
        itemList.addAll(newAdapterDataList)
        notifyItemRangeInserted(startPosition, newDataList.size)
    }

    // abstract methods for subclasses
    // TODO: param can be layoutInflater
    abstract fun onCreateDataViewHolder(parent: ViewGroup): RecyclerView.ViewHolder
    abstract fun onCreateLoaderViewHolder(parent: ViewGroup): RecyclerView.ViewHolder
    abstract fun createDiffCallBack(
        oldList: List<LoadingAdapterData<T>>, newList: List<LoadingAdapterData<T>>
    ) : BaseDiffCallBack<T>
}