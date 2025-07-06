package com.cp.fishthebreak.adapters.routes

import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.base.BaseAdapter
import com.cp.fishthebreak.databinding.ItemRouteLocationsBinding
import com.cp.fishthebreak.models.points.SaveFishLogData
import com.cp.fishthebreak.models.points.SavePointsData

class RouteLocationAdapter(private val list: List<SavePointsData>, private val mListener: RouteClickListener): BaseAdapter<ItemRouteLocationsBinding, SavePointsData>(list) {
    override val layoutId: Int
        get() = R.layout.item_route_locations

    override fun bind(binding: ItemRouteLocationsBinding, item: SavePointsData) {
        binding.apply {
            data = item
            listener = mListener
            executePendingBindings()
            ivSelect.setOnClickListener {
                mListener.onSelectLocation(item)
                invalidateAll()
            }
        }
    }
}

interface RouteClickListener{
    fun onSelectLocation(data: SavePointsData)
    fun onSelectFishLog(data: SaveFishLogData)
}