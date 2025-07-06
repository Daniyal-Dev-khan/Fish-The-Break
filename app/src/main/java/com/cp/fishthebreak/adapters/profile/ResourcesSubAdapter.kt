package com.cp.fishthebreak.adapters.profile

import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.base.BaseAdapter
import com.cp.fishthebreak.databinding.ItemResourceAllSubListBinding
import com.cp.fishthebreak.models.profile.ResourceDetails


class ResourcesSubAdapter(private val list: List<ResourceDetails>, private val mListener: ResourceDataClickListener): BaseAdapter<ItemResourceAllSubListBinding, ResourceDetails>(list) {
    override val layoutId: Int
        get() = R.layout.item_resource_all_sub_list

    override fun bind(binding: ItemResourceAllSubListBinding, item: ResourceDetails) {
        binding.apply {
            data = item
            listener = mListener
            executePendingBindings()
        }
    }
}