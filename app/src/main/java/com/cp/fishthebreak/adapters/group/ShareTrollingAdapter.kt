package com.cp.fishthebreak.adapters.group

import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.base.BaseAdapter
import com.cp.fishthebreak.adapters.saved.SavedTrollingClickListener
import com.cp.fishthebreak.databinding.ItemShareTrollingBinding
import com.cp.fishthebreak.models.trolling.TrollingResponseData

class ShareTrollingAdapter(private val list: List<TrollingResponseData>, private val mListener: SavedTrollingClickListener): BaseAdapter<ItemShareTrollingBinding, TrollingResponseData>(list) {
    override val layoutId: Int
        get() = R.layout.item_share_trolling

    override fun bind(binding: ItemShareTrollingBinding, item: TrollingResponseData) {
        binding.apply {
            data = item
            listener = mListener
            executePendingBindings()
        }
    }
}