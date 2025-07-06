package com.cp.fishthebreak.adapters.group

import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.base.BaseAdapter
import com.cp.fishthebreak.adapters.saved.SavedFishLogClickListener
import com.cp.fishthebreak.databinding.ItemShareFishLogBinding
import com.cp.fishthebreak.models.points.SaveFishLogData

class ShareFishLogAdapter(private val list: List<SaveFishLogData>, private val mListener: SavedFishLogClickListener): BaseAdapter<ItemShareFishLogBinding, SaveFishLogData>(list) {
    override val layoutId: Int
        get() = R.layout.item_share_fish_log

    override fun bind(binding: ItemShareFishLogBinding, item: SaveFishLogData) {
        binding.apply {
            data = item
            listener = mListener
            executePendingBindings()
        }
    }
}