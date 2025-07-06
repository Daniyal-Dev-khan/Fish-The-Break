package com.cp.fishthebreak.adapters.routes

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.ItemMeasureDistanceBinding
import com.cp.fishthebreak.databinding.ItemMeasureDistanceSaveBinding
import com.cp.fishthebreak.databinding.ItemSelectedLibraryBinding
import com.cp.fishthebreak.models.home.SearchData
import com.cp.fishthebreak.models.points.SaveFishLogData
import com.cp.fishthebreak.models.points.SavePointsData
import com.cp.fishthebreak.models.routes.MeasureDistanceModel
import com.cp.fishthebreak.utils.setOnSingleClickListener

class SaveMeasureDistanceAdapter(private val list: ArrayList<MeasureDistanceModel>): RecyclerView.Adapter<SaveMeasureDistanceAdapter.ViewHolder>() {
    private lateinit var mContext: Context
    inner class ViewHolder(private val binding: ItemMeasureDistanceSaveBinding):RecyclerView.ViewHolder(binding.root) {
        fun onBind(index: Int){
            val item = list[index]
            var name = ""
            name = when(item.point1){
                is SavePointsData->{
                    binding.tvLat1.text = "Lat: ${item.point1.getLatitudeFormat()}"
                    binding.tvLang1.text = "Long: ${item.point1.getLongitudeFormat()}"
                    binding.tvDescription.text = item.point1.description?:""
                    item.point1.point_name?:""
                }
                is SaveFishLogData->{
                    binding.tvLat1.text = "Lat: ${item.point1.getLatitudeFormat()}"
                    binding.tvLang1.text = "Long: ${item.point1.getLongitudeFormat()}"
                    binding.tvDescription.text = item.point1.description?:""
                    item.point1.fish_name?:""
                }
                is SearchData->{
                    binding.tvLat1.text = "Lat: ${item.point1.getLatitudeFormat()}"
                    binding.tvLang1.text = "Long: ${item.point1.getLongitudeFormat()}"
                    binding.tvDescription.text = item.point1.description?:""
                    item.point1.search_text?:item.point1.name?:""
                }
                else->{""}
            }
            name = "$name to " + when(item.point2){
                is SavePointsData->{
                    binding.tvLat2.text = "Lat: ${item.point2.getLatitudeFormat()}"
                    binding.tvLang2.text = "Long: ${item.point2.getLongitudeFormat()}"
                    item.point2.point_name?:""
                }
                is SaveFishLogData->{
                    binding.tvLat2.text = "Lat: ${item.point2.getLatitudeFormat()}"
                    binding.tvLang2.text = "Long: ${item.point2.getLongitudeFormat()}"
                    item.point2.fish_name?:""
                }
                is SearchData->{
                    binding.tvLat2.text = "Lat: ${item.point2.getLatitudeFormat()}"
                    binding.tvLang2.text = "Long: ${item.point2.getLongitudeFormat()}"
                    item.point2.search_text?:item.point2.name?:""
                }
                else->{""}
            }
            binding.tvName.text = "$name (${item.distance})"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mContext = parent.context
        val binding: ItemMeasureDistanceSaveBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_measure_distance_save,
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