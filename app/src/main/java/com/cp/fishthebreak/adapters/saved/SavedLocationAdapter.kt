package com.cp.fishthebreak.adapters.saved

import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.base.BaseAdapter
import com.cp.fishthebreak.databinding.ItemSavedLocationsBinding
import com.cp.fishthebreak.models.points.SavePointsData

class SavedLocationAdapter(private val list: List<SavePointsData>, private val mListener: SavedClickListener): BaseAdapter<ItemSavedLocationsBinding, SavePointsData>(list) {
    override val layoutId: Int
        get() = R.layout.item_saved_locations

    override fun bind(binding: ItemSavedLocationsBinding, item: SavePointsData) {
        binding.apply {
            data = item
            listener = mListener
            executePendingBindings()
        }
    }
}

interface SavedClickListener{
    fun onViewClick(data: SavePointsData)
    fun onDeleteClick(data: SavePointsData)
    fun onShareClick(data: SavePointsData)
    fun onViewOnMapClick(data: SavePointsData)
}