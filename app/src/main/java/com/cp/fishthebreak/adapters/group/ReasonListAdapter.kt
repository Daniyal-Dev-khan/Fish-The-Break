package com.cp.fishthebreak.adapters.group

import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.base.BaseAdapter
import com.cp.fishthebreak.databinding.ItemSelectReasonBinding
import com.cp.fishthebreak.models.group.LeaveReasonData

class ReasonListAdapter(private val list: List<LeaveReasonData>, private val mListener: OnReasonClickListeners): BaseAdapter<ItemSelectReasonBinding, LeaveReasonData>(list) {
    override val layoutId: Int
        get() = R.layout.item_select_reason

    override fun bind(binding: ItemSelectReasonBinding, item: LeaveReasonData) {
        binding.apply {
            data = item
            listener = mListener
            executePendingBindings()
        }
    }
}

interface OnReasonClickListeners{
    fun onClick(data: LeaveReasonData)
}