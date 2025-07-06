package com.cp.fishthebreak.adapters.group

import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.base.BaseAdapter
import com.cp.fishthebreak.databinding.ItemShareBinding
import com.cp.fishthebreak.models.group.ChatListData

class ShareInGroupAdapter(private val list: List<ChatListData>, private val mListener: OnSelectGroupClickListeners): BaseAdapter<ItemShareBinding, ChatListData>(list) {
    override val layoutId: Int
        get() = R.layout.item_share

    override fun bind(binding: ItemShareBinding, item: ChatListData) {
        binding.apply {
            data = item
            listener = mListener
            executePendingBindings()
            binding.root.setOnClickListener {
                mListener.onSelectClick(item)
                invalidateAll()
            }
        }
    }
}

interface OnSelectGroupClickListeners{
    fun onSelectClick(data: ChatListData)
}