package com.cp.fishthebreak.screens.bottom_sheets.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.FragmentGroupOptionBottomSheetBinding
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class GroupOptionBottomSheet(private val isAdmin: Boolean, private val listener: OnClickListeners) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentGroupOptionBottomSheetBinding
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
        binding = FragmentGroupOptionBottomSheetBinding.inflate(layoutInflater,container,false)
        if(isAdmin){
            binding.tvDeleteGroup.text = resources.getString(R.string.delete_group)
            binding.tvEditGroup.text = resources.getString(R.string.edit_group)
        }else{
            binding.tvDeleteGroup.text = resources.getString(R.string.leave_group)
            binding.tvEditGroup.text = resources.getString(R.string.view_group)
        }
        initListeners()
        return binding.root
    }

    private fun initListeners(){
        binding.tvEditGroup.setOnSingleClickListener {
            listener.onEditClick()
            dismiss()
        }
        binding.tvDeleteGroup.setOnSingleClickListener {
            listener.onDeleteClick()
            dismiss()
        }
        binding.tvCancel.setOnSingleClickListener {
            listener.onCancelClick()
            dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    interface OnClickListeners{
        fun onEditClick()
        fun onDeleteClick()
        fun onCancelClick()
    }

}