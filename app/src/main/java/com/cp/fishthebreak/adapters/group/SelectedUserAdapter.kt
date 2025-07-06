package com.cp.fishthebreak.adapters.group

import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.base.BaseAdapter
import com.cp.fishthebreak.databinding.ItemSelectedUserBinding
import com.cp.fishthebreak.models.auth.User

class SelectedUserAdapter(private val list: List<User>, private val mListener: OnFindUserClickListeners): BaseAdapter<ItemSelectedUserBinding, User>(list) {
    override val layoutId: Int
        get() = R.layout.item_selected_user

    override fun bind(binding: ItemSelectedUserBinding, item: User) {
        binding.apply {
            data = item
            listener = mListener
            executePendingBindings()
        }
    }
}