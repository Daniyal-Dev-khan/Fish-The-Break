package com.cp.fishthebreak.screens.fragments.home

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.layers.MapLayersAdapter
import com.cp.fishthebreak.databinding.FragmentApplyLayersBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.cp.fishthebreak.screens.fragments.home.HomeFragment.Companion.sharedLayerViewModel
import com.cp.fishthebreak.utils.NavigationDirections
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.map.LayersViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ApplyLayersFragment : Fragment() {

    private lateinit var binding: FragmentApplyLayersBinding

    //    val viewModel: LayersViewModel by activityViewModels()
    var viewModel: LayersViewModel? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentApplyLayersBinding.inflate(layoutInflater, container, false)
        viewModel = sharedLayerViewModel
        initViewModelResponse()
        initListeners()
        initDataBinding()
        return binding.root
    }

    private fun initListeners() {
        binding.backButton.setOnClickListener {
            if (requireActivity() is NavGraphActivity) {
                (requireActivity() as NavGraphActivity).onBack()
            }
        }
    }

    private fun initDataBinding() {
        binding.lifecycleOwner = this@ApplyLayersFragment
        binding.viewModel = viewModel
        viewModel?.let { binding.layersAdapter = MapLayersAdapter(listOf(), it) }
    }

    private fun showHideLoader(visibility: Boolean) {
        if (visibility) {
            binding.loaderLayout.viewVisible()
        } else {
            binding.loaderLayout.viewGone()
            binding.pullToRefresh.isRefreshing = false
        }
    }

    private fun initViewModelResponse() {
        lifecycleScope.launch {
            viewModel?.layersResponse
                ?.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                ?.distinctUntilChanged()
                ?.collect { response ->
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
                                viewModel?.resetResponse()
                            }
                        }

                        is NetworkResult.Error -> {
                            showHideLoader(false)
                            requireActivity().showToast(
                                response.message.getOnErrorMessage(),
                                false
                            )
                            viewModel?.resetResponse()
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
                            viewModel?.resetResponse()
                        }

                        is NetworkResult.NoCallYet -> {
                        }
                    }
                }
        }
        lifecycleScope.launch {
            viewModel?.filterClickResponse
                ?.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                ?.distinctUntilChanged()
                ?.collect { response ->
                    if (response != null) {
                        when (response) {
                            is NavigationDirections.LayerFilterScreen -> {
                                val data = response
                                viewModel?.resetFilterClickResponse()
//                                val dialog = LayerOpacityBottomSheet(data.data.opacity ?: 100,
//                                    object : LayerOpacityBottomSheet.LayerFilterClickListeners {
//                                        override fun onApplyFilter(opacityValue: Int) {
//                                            data.data.opacity = opacityValue
//                                        }
//
//                                    })
//                                dialog.show(childFragmentManager, "LayerOpacityBottomSheet")
                                val sIntent = Intent()
                                sIntent.putExtra("layer_name", data.data.layer_calling_name)
                                requireActivity().setResult(RESULT_OK, sIntent)
                                requireActivity().finish()
                            }

                            is NavigationDirections.ReadMoreFilterScreen -> {
                                val data = response
                                viewModel?.resetFilterClickResponse()
                                findNavController().navigate(
                                    ApplyLayersFragmentDirections.actionApplyLayersFragmentToSingleResourceFragment(
                                        data.data.base_url_webpage + data.data.layer_calling_name
                                    )
                                )
                            }

                            else -> {}
                        }
                    }
                }
        }

    }

}