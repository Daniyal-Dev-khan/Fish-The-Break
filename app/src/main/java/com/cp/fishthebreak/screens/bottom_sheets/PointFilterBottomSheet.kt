package com.cp.fishthebreak.screens.bottom_sheets

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.FragmentPointFilterBottomSheetBinding
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PointFilterBottomSheet(private val filterType: Int?, private val listener: OnApplyFilterListener) : BottomSheetDialogFragment() {
    // filterType null for all, 0 for my items, 1 for shared items
    private lateinit var binding: FragmentPointFilterBottomSheetBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        try {
            (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
            (dialog as? BottomSheetDialog)?.behavior?.isDraggable = false
            isCancelable = false
        } catch (ex: Exception) {
        }
        binding = FragmentPointFilterBottomSheetBinding.inflate(layoutInflater,container,false)
        initViews()
        initListeners()
        return binding.root
    }

    private fun initViews(){
        when(filterType){
            null->{
                binding.rbAllItems.isChecked = true
            }
            0->{
                binding.rbMyItems.isChecked = true
            }
            1->{
                binding.rbSharedItems.isChecked = true
            }
        }
    }

    private fun initListeners(){
        binding.ivCross.setOnSingleClickListener {
            dismiss()
        }
        binding.buttonAplyFilter.setOnClickListener {
            listener.onApplyClick(
                when(binding.radioGroup.checkedRadioButtonId){
                    binding.rbAllItems.id->{ null }
                    binding.rbMyItems.id->{ 0 }
                    binding.rbSharedItems.id->{ 1 }
                    else->{
                        null
                    }
                }
            )
            dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    interface OnApplyFilterListener{
        fun onApplyClick(filterType: Int?)
    }

}