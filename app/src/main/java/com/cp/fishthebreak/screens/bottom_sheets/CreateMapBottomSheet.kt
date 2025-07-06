package com.cp.fishthebreak.screens.bottom_sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.FragmentCreateMapBottomSheetBinding
import com.cp.fishthebreak.utils.hideKeyboardFrom
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.cp.fishthebreak.viewModels.map.OfflineMapViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateMapBottomSheet(private val name: String, private val description: String, private val listener: OnClickListener) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentCreateMapBottomSheetBinding
    val viewModel: OfflineMapViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        try {
            (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
            (dialog as? BottomSheetDialog)?.behavior?.isDraggable = false
        } catch (_: Exception) {
        }
        binding = FragmentCreateMapBottomSheetBinding.inflate(layoutInflater,container,false)
        binding.lifecycleOwner = this
        binding.model = viewModel
        initViewModelResponse()
        initViews()
        initListeners()
        return binding.root
    }

    private fun initViews(){
        if(name.isNotEmpty()){
            viewModel.onNameChangeEvent(name)
            viewModel.onDescriptionChangeEvent(description)
        }
    }

    private fun initListeners(){
        binding.buttonSave.setOnClickListener {
            if(viewModel.validateUIDataWithRules()){
                requireContext().hideKeyboardFrom(binding.buttonSave)
                listener.onSave(name = binding.etName.text.toString().trim(), description = binding.etDescription.text.toString().trim())
                dismiss()
            }
        }
        binding.ivCross.setOnSingleClickListener {
            dismiss()
        }
    }

    private fun initViewModelResponse() {
        lifecycleScope.launch {
            viewModel.offlineMapUIStates
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    showHideError(resources.getString(R.string.error_map_name),binding.tfName,!response.nameError)
                    showHideError(resources.getString(R.string.error_map_description),binding.tfDescription,!response.descriptionError)
                }
        }
    }

    private fun showHideError(error: String, tf: TextInputLayout, showError: Boolean){
        if(showError){
            tf.error = error
        }else{
            tf.error = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    interface OnClickListener {
        fun onSave(name: String, description: String)
    }

}