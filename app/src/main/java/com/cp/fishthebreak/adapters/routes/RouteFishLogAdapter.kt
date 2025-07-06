package com.cp.fishthebreak.adapters.routes

import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.base.BaseAdapter
import com.cp.fishthebreak.databinding.ItemRouteFishLogBinding
import com.cp.fishthebreak.models.points.SaveFishLogData
import com.cp.fishthebreak.utils.setOnSingleClickListener

class RouteFishLogAdapter(private val list: List<SaveFishLogData>, private val mListener: RouteClickListener): BaseAdapter<ItemRouteFishLogBinding, SaveFishLogData>(list) {
    override val layoutId: Int
        get() = R.layout.item_route_fish_log

    override fun bind(binding: ItemRouteFishLogBinding, item: SaveFishLogData) {
        binding.apply {
            data = item
            listener = mListener
            executePendingBindings()
            ivSelect.setOnClickListener {
                mListener.onSelectFishLog(item)
                invalidateAll()
            }
        }
    }
}