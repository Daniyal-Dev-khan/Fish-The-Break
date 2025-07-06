package com.cp.fishthebreak.screens.bottom_sheets

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.FragmentConfirmBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ConfirmBottomSheet(private val title: String, private val description: String, private val listener: OnItemClickListener, private val positiveButtonText: String? = null) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentConfirmBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentConfirmBottomSheetBinding.inflate(layoutInflater,container,false)
        initListeners()
        binding.tvTitle.text = title
        binding.tvDescription.text = description
        if(!positiveButtonText.isNullOrEmpty()){
            binding.buttonYes.text = positiveButtonText
        }
        return binding.root
    }

    private fun initListeners(){
        binding.buttonYes.setOnClickListener {
            listener.onYesClick()
            dismiss()
        }
        binding.buttonCancel.setOnClickListener {
            listener.onCancelClick()
            dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    interface OnItemClickListener{
        fun onYesClick()
        fun onCancelClick()
    }

}