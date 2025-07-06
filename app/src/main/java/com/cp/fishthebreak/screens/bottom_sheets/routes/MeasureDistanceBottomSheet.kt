package com.cp.fishthebreak.screens.bottom_sheets.routes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.FragmentMeasureDistanceBottomSheetBinding
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MeasureDistanceBottomSheet(private val listener: OnClickListeners) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentMeasureDistanceBottomSheetBinding
    private var isButtonClick = false
    @Inject
    lateinit var sharePreferenceHelper: SharePreferenceHelper
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        try {
            (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
            //(dialog as? BottomSheetDialog)?.behavior?.isDraggable = false
        } catch (ex: Exception) {
        }
        binding = FragmentMeasureDistanceBottomSheetBinding.inflate(layoutInflater,container,false)
        initListeners()
        return binding.root
    }

    private fun initListeners(){
        binding.ivCrossLocation.setOnClickListener {
            dismiss()
        }
        binding.buttonStart.setOnClickListener {
            lifecycleScope.launch {
                if (binding.checkBox.isChecked) {
                    sharePreferenceHelper.doNotAskAgainRouteDialog()
                }
                isButtonClick = true
                listener.startMeasureDistanceClick()
                dismiss()
            }
        }
        binding.buttonLibrary.setOnClickListener {
            lifecycleScope.launch {
                if (binding.checkBox.isChecked) {
                    sharePreferenceHelper.doNotAskAgainRouteDialog()
                }
                isButtonClick = true
                listener.startMeasureFromLibraryClick()
                dismiss()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    override fun onDestroy() {
        if(!isButtonClick){
            listener.onCancel()
        }
        super.onDestroy()
    }

    interface OnClickListeners{
        fun onCancel()
        fun startMeasureDistanceClick()
        fun startMeasureFromLibraryClick()
    }
}