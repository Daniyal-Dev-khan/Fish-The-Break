package com.cp.fishthebreak.adapters.routes

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.ItemSelectedLibraryBinding
import com.cp.fishthebreak.models.points.SaveFishLogData
import com.cp.fishthebreak.models.points.SavePointsData
import com.cp.fishthebreak.utils.setOnSingleClickListener

class SelectedLibraryAdapter(private val list: ArrayList<Any>, private val mListener: OnItemClickListener): RecyclerView.Adapter<SelectedLibraryAdapter.ViewHolder>() {
    private lateinit var mContext: Context
    inner class ViewHolder(private val binding: ItemSelectedLibraryBinding, private val listener: OnItemClickListener):RecyclerView.ViewHolder(binding.root) {
        fun onBind(index: Int){
            when(list[index]){
                is SavePointsData->{
                    binding.tvName.text = (list[index] as SavePointsData).point_name
                    binding.iv.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.ic_location_pin_library))
                }
                is SaveFishLogData->{
                    binding.tvName.text = (list[index] as SaveFishLogData).fish_name
                    binding.iv.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.ic_fish_library))
                }
            }
            binding.ivCross.setOnSingleClickListener {
                listener.onCrossClick(index)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mContext = parent.context
        val binding: ItemSelectedLibraryBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_selected_library,
            parent,
            false
        )
        return ViewHolder(binding, mListener)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(position)
    }

    interface OnItemClickListener{
        fun onCrossClick(position: Int)
    }
}