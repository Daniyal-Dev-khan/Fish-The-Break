package com.cp.fishthebreak.screens.bottom_sheets

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.layers.MapLayersAdapter
import com.cp.fishthebreak.adapters.layers.MapStyleAdapter
import com.cp.fishthebreak.databinding.FragmentLayerBottomSheetBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.models.map.MapLayer
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.cp.fishthebreak.utils.Constants.Companion.START_DESTINATION
import com.cp.fishthebreak.utils.NavigationDirections
import com.cp.fishthebreak.utils.StartDestination
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.map.LayersViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LayerBottomSheet(private val listener: OnBottomSheetDismissListener) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentLayerBottomSheetBinding
    val viewModel: LayersViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        try {
            (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
            (dialog as? BottomSheetDialog)?.behavior?.isDraggable = false
        } catch (ex: Exception) {
        }
        binding = FragmentLayerBottomSheetBinding.inflate(layoutInflater,container,false)
        viewModel.getRecentLayers()// show selected layers in bottom sheet first
        initViewModelResponse()
        initListeners()
        initDataBinding()
        return binding.root
    }

    private fun initListeners(){
        binding.ivCross.setOnSingleClickListener {
            dismiss()
        }
        binding.viewAllButton.setOnSingleClickListener {
//            val sIntent = Intent(requireContext(),NavGraphActivity::class.java)
//            sIntent.putExtra(START_DESTINATION,StartDestination.ApplyLayers())
//            startActivity(sIntent)
            listener.onViewAll()
            dismiss()
        }
    }

    private fun initDataBinding(){
        binding.lifecycleOwner = this@LayerBottomSheet
        binding.viewModel = viewModel
        binding.mapAdapter = MapStyleAdapter(listOf(),viewModel)
        binding.layersAdapter = MapLayersAdapter(listOf(),viewModel)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    private fun showHideLoader(visibility: Boolean) {
        if (visibility) {
            binding.loaderLayout.viewVisible()
        } else {
            binding.loaderLayout.viewGone()
        }
    }
    private fun initViewModelResponse() {
        lifecycleScope.launch {
            viewModel.layersResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true) {

                            } else {
                                requireActivity().showToast(
                                    response.data?.message ?: resources.getString(
                                        R.string.something_went_wrong
                                    ), false
                                )
                                viewModel.resetResponse()
                            }
                        }

                        is NetworkResult.Error -> {
                            showHideLoader(false)
                            requireActivity().showToast(
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
                            requireActivity().showToast(
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
        lifecycleScope.launch {
            viewModel.filterClickResponse
                ?.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                ?.distinctUntilChanged()
                ?.collect { response ->
                    if (response != null) {
                        when(response){
                            is NavigationDirections.LayerFilterScreen->{
                                val data = response
                                viewModel.resetFilterClickResponse()
//                                val dialog = LayerOpacityBottomSheet(data.data.opacity ?: 100,
//                                    object : LayerOpacityBottomSheet.LayerFilterClickListeners {
//                                        override fun onApplyFilter(opacityValue: Int) {
//                                            data.data.opacity = opacityValue
//                                        }
//
//                                    })
//                                dialog.show(childFragmentManager, "LayerOpacityBottomSheet")
                                listener.onFilterApply(data.data)
                                dismiss()
                            }
                            is NavigationDirections.ReadMoreFilterScreen -> {
                                val data = response
                                viewModel.resetFilterClickResponse()
                                val sIntent = Intent(requireContext(),NavGraphActivity::class.java)
                                sIntent.putExtra(START_DESTINATION,StartDestination.ReadMoreAboutLayer(data.data.base_url_webpage+data.data.layer_calling_name))
                                startActivity(sIntent)
                                dismiss()
                            }
                            else->{}
                        }
                    }
                }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        listener.onDismiss()
        super.onDismiss(dialog)
    }

    interface OnBottomSheetDismissListener{
        fun onDismiss()
        fun onFilterApply(data: MapLayer)
        fun onViewAll()
    }

}