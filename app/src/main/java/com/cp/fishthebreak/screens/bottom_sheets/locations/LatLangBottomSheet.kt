package com.cp.fishthebreak.screens.bottom_sheets.locations

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.FragmentLatLangBottomSheetBinding
import com.cp.fishthebreak.utils.convertCoordinatesLangToPickerData
import com.cp.fishthebreak.utils.convertCoordinatesLatToPickerData
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class LatLangBottomSheet(val text: String, private val isLongitude: Boolean, private val listener: OnItemClickListener) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentLatLangBottomSheetBinding
    private val degreeList = ArrayList<String>()
    private val minutesList = ArrayList<String>()
    private val secondsList = ArrayList<String>()
    private val directionList = ArrayList<String>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        try {
            (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
            (dialog as? BottomSheetDialog)?.behavior?.isDraggable = false
        } catch (ex: Exception) {
        }
        binding = FragmentLatLangBottomSheetBinding.inflate(layoutInflater,container,false)
        lifecycleScope.launch {
            initLoopView()
            initListeners()
        }
        return binding.root
    }

    private fun initListeners(){
        binding.backButton.setOnClickListener {
            dismiss()
        }
        binding.saveButton.setOnClickListener {

            listener.onSelect(degreeList[binding.degreeView.selectedItem]+minutesList[binding.minutesView.selectedItem]+secondsList[binding.secondsView.selectedItem]+directionList[binding.directionView.selectedItem])
            dismiss()
        }
        binding.degreeView.setListener {

        }
    }

    private fun initLoopView(){
        val maxDegree = if(isLongitude){
            binding.tvTitle.text = resources.getString(R.string.longitude_)
            directionList.add("E")
            directionList.add("W")
            180
        }else{
            binding.tvTitle.text = resources.getString(R.string.latitude_)
            directionList.add("N")
            directionList.add("S")
            90
        }
        for (i in 0..maxDegree){
            degreeList.add("${i}${resources.getString(R.string.degree_sign)}")
        }
        for (i in 0..59){
            minutesList.add("${i}${resources.getString(R.string.minute_sign)}")
            secondsList.add("${i}${resources.getString(R.string.seconds_sign)}")
        }
        binding.degreeView.setItems(degreeList)
        binding.minutesView.setItems(minutesList)
        binding.secondsView.setItems(secondsList)
        binding.directionView.setItems(directionList)
        if (text.isNotEmpty()) {
            if (isLongitude) {
                val lang = convertCoordinatesLangToPickerData(text)?:return
                binding.degreeView.setCurrentPosition(lang.degree)
                binding.minutesView.setCurrentPosition(lang.minute)
                binding.secondsView.setCurrentPosition(lang.seconds)
                if (lang.directions == "E"){
                    binding.directionView.setCurrentPosition(0)
                }else if (lang.directions == "W"){
                    binding.directionView.setCurrentPosition(1)
                }
            } else {
                val lat = convertCoordinatesLatToPickerData(text)?:return
                binding.degreeView.setCurrentPosition(lat.degree)
                binding.minutesView.setCurrentPosition(lat.minute)
                binding.secondsView.setCurrentPosition(lat.seconds)
                if (lat.directions == "N"){
                    binding.directionView.setCurrentPosition(0)
                }else if (lat.directions == "S"){
                    binding.directionView.setCurrentPosition(1)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    interface OnItemClickListener{
        fun onSelect(data: String)
    }
}