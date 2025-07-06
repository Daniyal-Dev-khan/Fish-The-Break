package com.cp.fishthebreak.adapters.group

import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.base.BaseAdapter
import com.cp.fishthebreak.adapters.saved.SavedClickListener
import com.cp.fishthebreak.databinding.ItemShareLocationsBinding
import com.cp.fishthebreak.models.points.SavePointsData

class ShareLocationAdapter(private val list: List<SavePointsData>, private val mListener: SavedClickListener): BaseAdapter<ItemShareLocationsBinding, SavePointsData>(list) {
    override val layoutId: Int
        get() = R.layout.item_share_locations

    override fun bind(binding: ItemShareLocationsBinding, item: SavePointsData) {
        binding.apply {
            data = item
            listener = mListener
            executePendingBindings()
        }
    }
}