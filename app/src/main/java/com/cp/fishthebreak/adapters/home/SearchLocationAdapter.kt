package com.cp.fishthebreak.adapters.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.ItemSearchLocationBinding
import com.cp.fishthebreak.models.home.SearchData
import com.cp.fishthebreak.utils.setOnSingleClickListener

class SearchLocationAdapter(private val list: ArrayList<SearchData>, private val mListener: OnItemClickListener) : RecyclerView.Adapter<SearchLocationAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ItemSearchLocationBinding, val listener: OnItemClickListener) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(index: Int){
            binding.tvName.text = list[index].search_text?:list[index].name?:""
            //binding.tvAddress.text = ""
            binding.root.setOnSingleClickListener {
                listener.onItemClick(index)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemSearchLocationBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.item_search_location, parent, false
        )
        return ViewHolder(binding, mListener)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(position)
    }

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }
}