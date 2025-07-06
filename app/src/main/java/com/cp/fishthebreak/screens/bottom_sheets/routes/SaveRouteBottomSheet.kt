package com.cp.fishthebreak.screens.bottom_sheets.routes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.routes.MeasureDistanceAdapter
import com.cp.fishthebreak.databinding.FragmentSaveRouteBottomSheetBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.models.routes.MeasureDistanceModel
import com.cp.fishthebreak.models.routes.SaveRouteData
import com.cp.fishthebreak.screens.activities.BaseActivityResult
import com.cp.fishthebreak.utils.SelectImageListener
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.loadImage
import com.cp.fishthebreak.utils.selectImage
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.routes.RouteViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SaveRouteBottomSheet(private val list: ArrayList<MeasureDistanceModel>, private val listener: OnClickListener, private val data: SaveRouteData? = null) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentSaveRouteBottomSheetBinding
    val viewModel: RouteViewModel by viewModels()

    private val activityLauncher: BaseActivityResult<Intent, ActivityResult> =
        BaseActivityResult.registerActivityForResult(this)

    private var isSaveButtonClick = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        try {
            (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
            (dialog as? BottomSheetDialog)?.behavior?.isDraggable = false
//            isCancelable = false
        } catch (ex: Exception) {
        }
        binding = FragmentSaveRouteBottomSheetBinding.inflate(layoutInflater,container,false)
        binding.lifecycleOwner = this
        binding.model = viewModel
        viewModel.onLocationsChangeEvent(list)
        initViewModelResponse()
        initListeners()
        initAdapter()
        if(data != null){
            initViews()
        }
        return binding.root
    }

    private fun initAdapter(){
        binding.rvMeasureDistance.adapter = MeasureDistanceAdapter(list)
    }
    private fun initViews(){
        viewModel.onRouteIdChangeEvent(data?.id)
        viewModel.onNameChangeEvent(data?.name?:"")
        viewModel.onDescriptionChangeEvent(data?.description?:"")
        binding.iv.loadImage(
            (data?.base_url?:"")+(data?.image?:""),
            R.drawable.place_holder_square,
            R.drawable.place_holder_square
        )
    }
    private fun initListeners(){
        binding.ivAddImage.setOnSingleClickListener {
            imagePickerAvatar()
        }
        binding.ivCrossLocation.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroy() {
        if(isSaveButtonClick) {
            listener.onClose()
        }
        super.onDestroy()
    }

    private fun imagePickerAvatar() {
        requireActivity().selectImage(
            childFragmentManager,
            activityLauncher,
            object : SelectImageListener {
                override fun onImageSelect(path: String?) {
                    if (path.isNullOrEmpty()) {
                        return
                    }
                    binding.iv.loadImage(
                        path,
                        R.drawable.place_holder_square,
                        R.drawable.place_holder_square
                    )
                    viewModel.onImageChangeEvent(path)
                }

                override fun onImageCancel() {
                }

            })
    }

    private fun initViewModelResponse() {
        lifecycleScope.launch {
            viewModel.routeUIStates
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    showHideError(resources.getString(R.string.error_location_name),binding.tfName,!response.nameError)
                    showHideError(resources.getString(R.string.error_location_description),binding.tfDescription,!response.descriptionError)
                }
        }
        lifecycleScope.launch {
            viewModel.routeResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true && response.data.statusCode == 200) {
                                if(data != null){
                                    data.name = response.data.data?.name?:""
                                    data.description = response.data.data?.description?:""
                                    data.image = response.data.data?.image?:""
                                }
                                requireActivity().showToast(
                                    response.data.message, true
                                )
                                isSaveButtonClick = true
                                dismiss()
                            } else {
                                dialog?.showToast(
                                    requireContext(),
                                    response.data?.message ?: resources.getString(
                                        R.string.something_went_wrong
                                    ), false
                                )
                                viewModel.resetResponse()
                            }
                        }

                        is NetworkResult.Error -> {
                            showHideLoader(false)
                            dialog?.showToast(
                                requireContext(),
                                response.message.getOnErrorMessage(),
                                false
                            )
                            viewModel.resetResponse()
                        }

                        is NetworkResult.Loading -> {
                            showHideLoader(true)
                        }

                        is NetworkResult.NoInternet -> {
                            showHideLoader(false)
                            dialog?.showToast(
                                requireContext(),
                                resources.getString(R.string.no_internet),
                                false
                            )
                            viewModel.resetResponse()
                        }

                        is NetworkResult.NoCallYet -> {
                        }
                    }
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

    private fun showHideLoader(visibility: Boolean) {
        if (visibility) {
            binding.loaderLayout.viewVisible()
        } else {
            binding.loaderLayout.viewGone()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    interface OnClickListener{
        fun onClose()
    }

}