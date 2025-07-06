package com.cp.fishthebreak.adapters.group

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.base.BaseAdapter
import com.cp.fishthebreak.databinding.ItemGroupUserBinding
import com.cp.fishthebreak.models.group.GroupMember
import com.cp.fishthebreak.utils.loadImage
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible

class FindGroupMemberAdapter(private val list: List<GroupMember>): RecyclerView.Adapter<FindGroupMemberAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemGroupUserBinding): RecyclerView.ViewHolder(binding.root) {
        fun onBind(index: Int){
            binding.tvName.text = list[index].name
            binding.tvUserName.text = "@${list[index].username}"
            binding.iv.loadImage(list[index].base_url+list[index].profile_pic)
            if(list[index].is_admin == 1){
                binding.tvAdmin.viewVisible()
            }else{
                binding.tvAdmin.viewGone()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemGroupUserBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_group_user,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(position)
    }
}