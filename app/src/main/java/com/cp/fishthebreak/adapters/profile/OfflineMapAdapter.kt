package com.cp.fishthebreak.adapters.profile

import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.base.BaseAdapter
import com.cp.fishthebreak.databinding.ItemOfflineMapsBinding
import com.cp.fishthebreak.models.map.OfflineMap
import com.cp.fishthebreak.models.profile.OfflineMapModel
import com.cp.fishthebreak.utils.setOnSingleClickListener

class OfflineMapAdapter(private val list: List<OfflineMap>, private val mListener: OfflineMapListener): BaseAdapter<ItemOfflineMapsBinding, OfflineMap>(list) {
    override val layoutId: Int
        get() = R.layout.item_offline_maps

    override fun bind(binding: ItemOfflineMapsBinding, item: OfflineMap) {
        binding.apply {
            data = item
            listener = mListener
            executePendingBindings()
//            binding.ivDelete.setOnSingleClickListener {
//                mListener.onMapDeleteClick(item)
//                notifyDataSetChanged()
//            }
        }
    }
}

interface OfflineMapListener{
    fun onMapDeleteClick(data: OfflineMap)
    fun onMapClick(data: OfflineMap)
}