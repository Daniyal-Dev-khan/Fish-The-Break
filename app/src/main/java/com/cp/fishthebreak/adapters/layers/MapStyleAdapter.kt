package com.cp.fishthebreak.adapters.layers

import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.base.BaseAdapter
import com.cp.fishthebreak.databinding.ItemMapBinding
import com.cp.fishthebreak.models.map.MapStyle
import com.cp.fishthebreak.utils.setOnSingleClickListener

class MapStyleAdapter(private val list: List<MapStyle>, private val mListener: MapStyleListener): BaseAdapter<ItemMapBinding, MapStyle>(list) {
    override val layoutId: Int
        get() = R.layout.item_map

    override fun bind(binding: ItemMapBinding, item: MapStyle) {
        binding.apply {
            data = item
            listener = mListener
            executePendingBindings()
            when(item.name){
                "ARCGIS NOVA"->{
                    binding.iv.setImageResource(R.drawable.ic_map_nova)
                }
                "ARCGIS STREETS"->{
                    binding.iv.setImageResource(R.drawable.ic_map_streets)
                }
                "ARCGIS IMAGERY"->{
                    binding.iv.setImageResource(R.drawable.ic_map_imagery)
                }
                "ARCGIS OCEANS"->{
                    binding.iv.setImageResource(R.drawable.ic_map_oceans)
                }
            }
            binding.root.setOnSingleClickListener {
                mListener.onMapSelect(item)
                notifyDataSetChanged()
            }
        }
    }
}

interface MapStyleListener {
    fun onMapSelect(data: MapStyle)
}