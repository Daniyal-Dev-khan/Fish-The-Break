package com.cp.fishthebreak.adapters.group

import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.base.BaseAdapter
import com.cp.fishthebreak.adapters.saved.SavedRouteClickListener
import com.cp.fishthebreak.databinding.ItemShareRouteBinding
import com.cp.fishthebreak.models.routes.SaveRouteData

class ShareRouteAdapter(private val list: List<SaveRouteData>, private val mListener: SavedRouteClickListener): BaseAdapter<ItemShareRouteBinding, SaveRouteData>(list) {
    override val layoutId: Int
        get() = R.layout.item_share_route

    override fun bind(binding: ItemShareRouteBinding, item: SaveRouteData) {
        binding.apply {
            data = item
            listener = mListener
            executePendingBindings()
        }
    }
}
