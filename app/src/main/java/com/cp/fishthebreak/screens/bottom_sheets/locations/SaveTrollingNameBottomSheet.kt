package com.cp.fishthebreak.screens.bottom_sheets.locations

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.FragmentSaveTrollingNameBottomSheetBinding
import com.cp.fishthebreak.utils.hideKeyboardFrom
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SaveTrollingNameBottomSheet(private val listener: OnItemClickListener, private val trollingName: String? = null) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentSaveTrollingNameBottomSheetBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        try {
            (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
            (dialog as? BottomSheetDialog)?.behavior?.isDraggable = false
            if(trollingName.isNullOrEmpty()) {
                isCancelable = false
            }
        } catch (ex: Exception) {
        }
        binding = FragmentSaveTrollingNameBottomSheetBinding.inflate(layoutInflater,container,false)
        binding.etName.setText(trollingName?:"")
        initListener()
        return binding.root
    }

    private fun initListener(){
        binding.buttonSave.setOnClickListener {
            if(binding.etName.text.toString().trim().isEmpty()){
                binding.tfName.isErrorEnabled = true
                binding.tfName.error = resources.getString(R.string.error_trolling_name)
            }else{
                listener.onSaveClick(binding.etName.text.toString())
                requireContext().hideKeyboardFrom(binding.buttonSave)
                dismiss()
            }
        }
        binding.etName.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0.isNullOrEmpty()){
                    binding.tfName.isErrorEnabled = true
                    binding.tfName.error = resources.getString(R.string.error_trolling_name)
                }else{
                    binding.tfName.isErrorEnabled = false
                    binding.tfName.error = null
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    interface OnItemClickListener{
        fun onSaveClick(name: String)
    }

}