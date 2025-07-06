package com.cp.fishthebreak.screens.bottom_sheets

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.FragmentImagePickerBottomSheetBinding
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class ImagePickerBottomSheet(private val listener: OnItemClickListener) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentImagePickerBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentImagePickerBottomSheetBinding.inflate(layoutInflater,container,false)
        initListener()
        return binding.root
    }

    private fun initListener(){

        binding.tvCamera.setOnSingleClickListener {
            listener.onCameraSelect()
            dismiss()
        }
        binding.tvGallery.setOnSingleClickListener {
            listener.onGallerySelect()
            dismiss()
        }
        binding.ivCross.setOnSingleClickListener {
            listener.onCancel()
            dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    interface OnItemClickListener{
        fun onGallerySelect()
        fun onCameraSelect()
        fun onCancel()
    }

}