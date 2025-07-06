package com.cp.fishthebreak.adapters.group

import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.base.BaseAdapter
import com.cp.fishthebreak.databinding.ItemChatListBinding
import com.cp.fishthebreak.models.group.ChatListData

class ChatListAdapter(private val list: List<ChatListData>, private val mListener: OnChatClickListeners): BaseAdapter<ItemChatListBinding, ChatListData>(list) {
    override val layoutId: Int
        get() = R.layout.item_chat_list

    override fun bind(binding: ItemChatListBinding, item: ChatListData) {
        binding.apply {
            data = item
            listener = mListener
            executePendingBindings()
        }
    }
}

interface OnChatClickListeners{
    fun onChatClick(data: ChatListData)
    fun onAcceptClick(data: ChatListData)
    fun onRejectClick(data: ChatListData)
}