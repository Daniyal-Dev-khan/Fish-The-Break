package com.cp.fishthebreak.adapters.profile

import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.base.BaseAdapter
import com.cp.fishthebreak.databinding.ItemResourceAllListBinding
import com.cp.fishthebreak.models.profile.ResourceData
import com.cp.fishthebreak.models.profile.ResourceDetails
import com.cp.fishthebreak.viewModels.profile.resources.ResourcesViewModel


class ResourcesAdapter(private val list: List<ResourceData>, private val mListener: ResourceDataClickListener, private val viewModel: ResourcesViewModel): BaseAdapter<ItemResourceAllListBinding, ResourceData>(list) {
    override val layoutId: Int
        get() = R.layout.item_resource_all_list

    override fun bind(binding: ItemResourceAllListBinding, item: ResourceData) {
        binding.apply {
            data = item
            listener = mListener
            resourcesAdapter = ResourcesSubAdapter(listOf(),viewModel)
            executePendingBindings()
        }
    }
}

interface ResourceDataClickListener {
    fun onViewMoreClick(data: ResourceData)
    fun onResourceClick(data: ResourceDetails)
}