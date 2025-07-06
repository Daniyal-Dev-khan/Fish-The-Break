package com.cp.fishthebreak.screens.bottom_sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.FragmentLogoutBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class LogoutBottomSheet(private val listener: LogoutClickListener) : BottomSheetDialogFragment() {
   private lateinit var binding: FragmentLogoutBottomSheetBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLogoutBottomSheetBinding.inflate(layoutInflater,container,false)
        initListeners()
        return binding.root
    }

    private fun initListeners(){
        binding.apply {
            buttonLogout.setOnClickListener {
                listener.onLogoutClick()
                dismiss()
            }
            buttonCancel.setOnClickListener {
                listener.onCancelClick()
                dismiss()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    interface LogoutClickListener{
        fun onLogoutClick()
        fun onCancelClick()
    }


}