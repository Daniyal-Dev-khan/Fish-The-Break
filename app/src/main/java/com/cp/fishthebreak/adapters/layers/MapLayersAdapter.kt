package com.cp.fishthebreak.adapters.layers

import android.view.View
import androidx.navigation.fragment.findNavController
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.base.BaseAdapter
import com.cp.fishthebreak.databinding.ItemLayersBinding
import com.cp.fishthebreak.di.MyApplication
import com.cp.fishthebreak.models.map.MapLayer
import com.cp.fishthebreak.screens.fragments.auth.RegisterFragmentDirections
import com.cp.fishthebreak.utils.Constants
import com.cp.fishthebreak.utils.makeLinks

class MapLayersAdapter(private val list: List<MapLayer>, private val mListener: MapLayerListener): BaseAdapter<ItemLayersBinding, MapLayer>(list) {
    override val layoutId: Int
        get() = R.layout.item_layers

    override fun bind(binding: ItemLayersBinding, item: MapLayer) {
        binding.apply {
            data = item
            listener = mListener
            executePendingBindings()
            binding.btnLayerOnOff.setOnCheckedChangeListener { compoundButton, b ->
                mListener.onLayerToggle(item,b)
                invalidateAll()
            }
            binding.tvReadMore.makeLinks(
                Pair(MyApplication.appContext.resources.getString(R.string.read_more_), View.OnClickListener {
                    mListener.onReadMoreClick(item)
                }),
            )
        }
    }
}

interface MapLayerListener{
    fun onLayerToggle(data: MapLayer, isChecked: Boolean)
    fun onFilterClick(data: MapLayer)
    fun onReadMoreClick(data: MapLayer)
}