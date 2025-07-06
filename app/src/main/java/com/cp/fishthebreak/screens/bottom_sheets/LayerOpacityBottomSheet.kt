package com.cp.fishthebreak.screens.bottom_sheets

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.FragmentLayerOpacityBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LayerOpacityBottomSheet(private val layerName:String, private val opacity: Int, private val listener: LayerFilterClickListeners) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentLayerOpacityBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        try {
            (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
            (dialog as? BottomSheetDialog)?.behavior?.isDraggable = false
            isCancelable = false
            dialog?.window?.setDimAmount(0F)
        } catch (ex: Exception) {
        }
        binding = FragmentLayerOpacityBottomSheetBinding.inflate(layoutInflater,container,false)
        binding.slider.value = opacity.toFloat()
        "$opacity${resources.getString(R.string._percent)}".also { binding.tvOpacityPercent.text = it }
        binding.tvTitle.text = layerName
        initListener()
        return binding.root
    }

    private fun initListener(){
        binding.buttonApply.setOnClickListener {
            listener.onApplyFilterClick(binding.slider.value.toInt())
            dismiss()
        }
        binding.slider.addOnChangeListener { slider, value, fromUser ->
            "${value.toInt()}${resources.getString(R.string._percent)}".also { binding.tvOpacityPercent.text = it }
            if(fromUser) {
                listener.onApplyFilter(value.toInt())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
//        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    interface LayerFilterClickListeners {
        fun onApplyFilter(opacityValue: Int)
        fun onApplyFilterClick(opacityValue: Int)
    }

}