package com.cp.fishthebreak.adapters.trolling

import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.base.BaseAdapter
import com.cp.fishthebreak.adapters.routes.RouteClickListener
import com.cp.fishthebreak.databinding.ItemTrollingFishLogBinding
import com.cp.fishthebreak.models.points.SaveFishLogData

class TrollingFishLogAdapter(private val list: List<SaveFishLogData>, private val mListener: RouteClickListener): BaseAdapter<ItemTrollingFishLogBinding, SaveFishLogData>(list) {
    override val layoutId: Int
        get() = R.layout.item_trolling_fish_log

    override fun bind(binding: ItemTrollingFishLogBinding, item: SaveFishLogData) {
        binding.apply {
            data = item
            listener = mListener
            executePendingBindings()
        }
    }
}