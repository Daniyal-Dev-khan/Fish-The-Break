package com.cp.fishthebreak.adapters.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.ItemAddDestinationBinding
import com.cp.fishthebreak.models.home.SearchData
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible

class AddDestinationAdapter(private val list: ArrayList<Any?>, private val mListener: OnItemClickListener) : RecyclerView.Adapter<AddDestinationAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ItemAddDestinationBinding, val listener: OnItemClickListener) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(index: Int){
            binding.tvAddDestination.text = ""
            if(index == 0){
                binding.ivLineAddDestinationTop.viewGone()
            }else{
                binding.ivLineAddDestinationTop.viewVisible()
            }
            if(index == list.size-1){
                binding.ivLineAddDestinationBottom.viewGone()
                binding.ivRadioDestination.setImageResource(R.drawable.ic_location_destination)
            }else{
                binding.ivRadioDestination.setImageResource(R.drawable.ic_radio_destination)
                binding.ivLineAddDestinationBottom.viewVisible()
            }
            binding.ivCross.setOnClickListener {
                if(list[index] is SearchData) {
                    listener.onItemCrossClick(index)
                }else{
                    listener.onItemClick(index)
                    //listener.onItemMapClick(index)
                }
            }
            if (list[index] is SearchData){
                binding.ivCross.setImageResource(R.drawable.ic_cross_destination)
                val item = list[index] as SearchData
                binding.tvAddDestination.text = item.search_text?:item.name?:""
            }else{
//                binding.ivCross.setImageResource(R.drawable.ic_map_destination_icon)
                binding.ivCross.setImageResource(R.drawable.ic_search_route)
            }
            binding.layoutAddDestination.setOnSingleClickListener {
                //listener.onItemClick(index)
                listener.onItemMapClick(index)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemAddDestinationBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.item_add_destination, parent, false
        )
        return ViewHolder(binding, mListener)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(position)
    }

    interface OnItemClickListener{
        fun onItemClick(position: Int)
        fun onItemCrossClick(position: Int)
        fun onItemMapClick(position: Int)
    }
}