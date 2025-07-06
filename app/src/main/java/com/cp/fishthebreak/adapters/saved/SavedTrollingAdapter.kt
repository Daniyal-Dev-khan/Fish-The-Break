package com.cp.fishthebreak.adapters.saved

import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.base.BaseAdapter
import com.cp.fishthebreak.databinding.ItemSavedTrollingBinding
import com.cp.fishthebreak.models.trolling.TrollingResponseData

class SavedTrollingAdapter(private val list: List<TrollingResponseData>, private val mListener: SavedTrollingClickListener): BaseAdapter<ItemSavedTrollingBinding, TrollingResponseData>(list) {
    override val layoutId: Int
        get() = R.layout.item_saved_trolling

    override fun bind(binding: ItemSavedTrollingBinding, item: TrollingResponseData) {
        binding.apply {
            data = item
            listener = mListener
            executePendingBindings()
        }
    }
}

interface SavedTrollingClickListener{
    fun onViewClick(data: TrollingResponseData)
    fun onEditClick(data: TrollingResponseData)
    fun onDeleteClick(data: TrollingResponseData)
    fun onShareClick(data: TrollingResponseData)
}