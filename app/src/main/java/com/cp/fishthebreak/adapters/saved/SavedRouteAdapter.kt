package com.cp.fishthebreak.adapters.saved

import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.base.BaseAdapter
import com.cp.fishthebreak.databinding.ItemSavedRouteBinding
import com.cp.fishthebreak.models.routes.SaveRouteData

class SavedRouteAdapter(private val list: List<SaveRouteData>, private val mListener: SavedRouteClickListener): BaseAdapter<ItemSavedRouteBinding, SaveRouteData>(list) {
    override val layoutId: Int
        get() = R.layout.item_saved_route

    override fun bind(binding: ItemSavedRouteBinding, item: SaveRouteData) {
        binding.apply {
            data = item
            listener = mListener
            executePendingBindings()
        }
    }
}

interface SavedRouteClickListener{
    fun onViewClick(data: SaveRouteData)
    fun onEditClick(data: SaveRouteData)
    fun onDeleteClick(data: SaveRouteData)
    fun onShareClick(data: SaveRouteData)
}