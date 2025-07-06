package com.cp.fishthebreak.screens.bottom_sheets.trolling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.FragmentRecordTrollingBottomSheetBinding
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RecordTrollingBottomSheet(private val listener: OnTrollingListeners) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentRecordTrollingBottomSheetBinding
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
        binding = FragmentRecordTrollingBottomSheetBinding.inflate(layoutInflater,container,false)
        initListeners()
        return binding.root
    }

    private fun initListeners(){
        binding.ivCrossLocation.setOnClickListener {
            dismiss()
        }
        binding.buttonSave.setOnClickListener {
            if (binding.checkBox.isChecked) {
                lifecycleScope.launch {
                    sharePreferenceHelper.doNotAskAgainTrollingDialog()
                    listener.startTrolling()
                    dismiss()
                }
            }else{
                listener.startTrolling()
                dismiss()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    interface OnTrollingListeners{
        fun startTrolling()
    }

}