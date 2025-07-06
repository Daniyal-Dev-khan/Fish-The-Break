package com.cp.fishthebreak.adapters.routes

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.ItemMeasureDistanceBinding
import com.cp.fishthebreak.models.home.SearchData
import com.cp.fishthebreak.models.points.SaveFishLogData
import com.cp.fishthebreak.models.points.SavePointsData
import com.cp.fishthebreak.models.routes.MeasureDistanceModel
import com.cp.fishthebreak.models.routes.SaveRoutePoint
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible

class MeasureDistanceAdapter(private val list: ArrayList<MeasureDistanceModel>): RecyclerView.Adapter<MeasureDistanceAdapter.ViewHolder>() {
    private lateinit var mContext: Context
    inner class ViewHolder(private val binding: ItemMeasureDistanceBinding):RecyclerView.ViewHolder(binding.root) {
        fun onBind(index: Int){
            val item = list[index]
            var name = ""
            name = when(item.point1){
                is SavePointsData->{
                    binding.tvLat1.text = "Lat: ${item.point1.getLatitudeFormat()}"
                    binding.tvLang1.text = "Long: ${item.point1.getLongitudeFormat()}"
                    item.point1.point_name?:""
                }
                is SaveFishLogData->{
                    binding.tvLat1.text = "Lat: ${item.point1.getLatitudeFormat()}"
                    binding.tvLang1.text = "Long: ${item.point1.getLongitudeFormat()}"
                    item.point1.fish_name?:""
                }
                is SearchData->{
                    binding.tvLat1.text = "Lat: ${item.point1.getLatitudeFormat()}"
                    binding.tvLang1.text = "Long: ${item.point1.getLongitudeFormat()}"
                    item.point1.search_text?:item.point1.name?:""
                }
                is SaveRoutePoint->{
                    binding.tvLat1.text = "Lat: ${item.point1.getLatitudeFormat()}"
                    binding.tvLang1.text = "Long: ${item.point1.getLongitudeFormat()}"
                    item.point1.name?:""
                }
                else->{""}
            }
            val name1 = when(item.point2){
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
                is SaveRoutePoint->{
                    binding.tvLat2.text = "Lat: ${item.point2.getLatitudeFormat()}"
                    binding.tvLang2.text = "Long: ${item.point2.getLongitudeFormat()}"
                    item.point2.name?:""
                }
                else->{""}
            }
            binding.tvDistance.text = item.distance
            binding.tvTitle.text = name
            binding.tvTitle1.text = name1
            if (index == 0){
                binding.tvName.viewVisible()
                binding.tvTitle.viewVisible()
                binding.tvLat1.viewVisible()
                binding.tvLang1.viewVisible()
                binding.iv.viewVisible()
                binding.ivDotedLine.viewVisible()
                binding.ivDotedLineTop.viewGone()
                binding.tvName.text = "Point ${(index+1)}"
                binding.tvName1.text = "Point ${(index+2)}"
            }else{
                binding.tvName.viewGone()
                binding.tvTitle.viewGone()
                binding.tvLat1.viewGone()
                binding.tvLang1.viewGone()
                binding.iv.viewGone()
                binding.ivDotedLine.viewGone()
                binding.ivDotedLineTop.viewVisible()
                binding.tvName.text = "Point ${(index+2)}"
                binding.tvName1.text = "Point ${(index+2)}"
            }
            if (index == list.size -1){
                binding.ivDotedLineBottom.viewGone()
            }else{
                binding.ivDotedLineBottom.viewVisible()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mContext = parent.context
        val binding: ItemMeasureDistanceBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_measure_distance,
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