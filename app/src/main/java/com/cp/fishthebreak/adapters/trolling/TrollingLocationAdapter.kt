package com.cp.fishthebreak.adapters.trolling

import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.base.BaseAdapter
import com.cp.fishthebreak.adapters.routes.RouteClickListener
import com.cp.fishthebreak.databinding.ItemTrollingLocationsBinding
import com.cp.fishthebreak.models.points.SavePointsData

class TrollingLocationAdapter(private val list: List<SavePointsData>, private val mListener: RouteClickListener): BaseAdapter<ItemTrollingLocationsBinding, SavePointsData>(list) {
    override val layoutId: Int
        get() = R.layout.item_trolling_locations

    override fun bind(binding: ItemTrollingLocationsBinding, item: SavePointsData) {
        binding.apply {
            data = item
            listener = mListener
            executePendingBindings()
        }
    }
}