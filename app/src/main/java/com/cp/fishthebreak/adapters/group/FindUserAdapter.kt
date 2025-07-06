package com.cp.fishthebreak.adapters.group

import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.base.BaseAdapter
import com.cp.fishthebreak.databinding.ItemFindUserBinding
import com.cp.fishthebreak.models.auth.User
import com.cp.fishthebreak.models.group.ChatListData
import com.cp.fishthebreak.utils.setOnSingleClickListener

class FindUserAdapter(private val list: List<User>, private val mListener: OnFindUserClickListeners): BaseAdapter<ItemFindUserBinding, User>(list) {
    override val layoutId: Int
        get() = R.layout.item_find_user

    override fun bind(binding: ItemFindUserBinding, item: User) {
        binding.apply {
            data = item
            listener = mListener
            executePendingBindings()
            binding.root.setOnClickListener {
                mListener.onUserClick(item)
                invalidateAll()
            }
        }
    }
}

interface OnFindUserClickListeners{
    fun onUserClick(data: User)
    fun onCrossClick(data: User)
}