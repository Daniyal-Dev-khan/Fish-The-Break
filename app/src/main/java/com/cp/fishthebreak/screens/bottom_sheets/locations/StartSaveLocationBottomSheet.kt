package com.cp.fishthebreak.screens.bottom_sheets.locations

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.FragmentStartSaveLocationBottomSheetBinding
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StartSaveLocationBottomSheet(private val lat:Double?, private val long: Double?, private val listener: OnStartClickListener) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentStartSaveLocationBottomSheetBinding
    private var isSaveClick = false
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
        binding = FragmentStartSaveLocationBottomSheetBinding.inflate(layoutInflater,container,false)
        initListeners()
        return binding.root
    }

    private fun initListeners(){
        binding.ivCrossLocation.setOnClickListener {
            dismiss()
        }
        binding.buttonSave.setOnClickListener {
            lifecycleScope.launch {
                if (binding.checkBox.isChecked) {
                    sharePreferenceHelper.doNotAskAgainLocationDialog()
                }
                isSaveClick = true
                dismiss()
            }
        }
    }

    override fun onDestroy() {
        if(isSaveClick){
            listener.onStartClick(lat,long)
        }else{
            listener.onCancelClick()
        }
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    interface OnStartClickListener{
        fun onStartClick(lat:Double?, long: Double?)
        fun onCancelClick()
    }

}