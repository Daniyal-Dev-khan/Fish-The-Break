package com.cp.fishthebreak.adapters.profile

import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.base.BaseAdapter
import com.cp.fishthebreak.databinding.ItemSelectOfflineMapBinding
import com.cp.fishthebreak.models.map.OfflineMap
import com.cp.fishthebreak.utils.loadImage

class SelectOfflineMapAdapter(private val list: List<OfflineMap>, private val mListener: OnItemClickListener): BaseAdapter<ItemSelectOfflineMapBinding, OfflineMap>(list) {
    override val layoutId: Int
        get() = R.layout.item_select_offline_map

    override fun bind(binding: ItemSelectOfflineMapBinding, item: OfflineMap) {
        binding.apply {
            data = item
            listener = mListener
            executePendingBindings()
            if(item.placeholder == null) {
                iv.loadImage(item.image)
            }else{
                item.placeholder?.let { iv.setImageResource(it) }
            }
        }
    }
}
interface OnItemClickListener{
    fun onMapClick(data: OfflineMap)
}