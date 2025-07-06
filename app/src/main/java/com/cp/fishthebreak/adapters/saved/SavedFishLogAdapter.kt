package com.cp.fishthebreak.adapters.saved

import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.base.BaseAdapter
import com.cp.fishthebreak.databinding.ItemSavedFishLogBinding
import com.cp.fishthebreak.models.points.SaveFishLogData

class SavedFishLogAdapter(private val list: List<SaveFishLogData>, private val mListener: SavedFishLogClickListener): BaseAdapter<ItemSavedFishLogBinding, SaveFishLogData>(list) {
    override val layoutId: Int
        get() = R.layout.item_saved_fish_log

    override fun bind(binding: ItemSavedFishLogBinding, item: SaveFishLogData) {
        binding.apply {
            data = item
            listener = mListener
            executePendingBindings()
        }
    }
}

interface SavedFishLogClickListener{
    fun onViewClick(data: SaveFishLogData)
    fun onViewOnMapClick(data: SaveFishLogData)
    fun onDeleteClick(data: SaveFishLogData)
    fun onShareClick(data: SaveFishLogData)
}