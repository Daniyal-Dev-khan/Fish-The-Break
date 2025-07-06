package com.cp.fishthebreak.screens.bottom_sheets

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.FragmentPermissionBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class PermissionBottomSheet(private val title: String, private val listener: OnItemClickListeners) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentPermissionBottomSheetBinding
    private var isSettingsClick = false
    private var isCancelClick = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPermissionBottomSheetBinding.inflate(layoutInflater,container,false)
        binding.tvDescription.text = title
        initListeners()
        return binding.root
    }

    private fun initListeners(){
        binding.apply {
            buttonOpenSettings.setOnClickListener {
                isSettingsClick = true
                listener.onSettingsClick()
                dismiss()
            }
            buttonCancel.setOnClickListener {
                isCancelClick = true
                listener.onCancelClick()
                dismiss()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    override fun onDestroy() {
        if(!isSettingsClick && !isCancelClick){
            listener.onCancelClick()
        }
        super.onDestroy()
    }

    interface OnItemClickListeners{
        fun onSettingsClick()
        fun onCancelClick()
    }

}