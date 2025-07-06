package com.cp.fishthebreak.screens.bottom_sheets.group

import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.group.FindUserAdapter
import com.cp.fishthebreak.adapters.group.SelectedUserAdapter
import com.cp.fishthebreak.adapters.group.ShareInGroupAdapter
import com.cp.fishthebreak.databinding.FragmentShareInGroupBottomSheetBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.utils.Constants.Companion.FISH_LOG_TYPE
import com.cp.fishthebreak.utils.Constants.Companion.LOCATION_TYPE
import com.cp.fishthebreak.utils.Constants.Companion.ROUTE_TYPE
import com.cp.fishthebreak.utils.Constants.Companion.TROLLING_TYPE
import com.cp.fishthebreak.utils.MapUiData
import com.cp.fishthebreak.utils.NavigationDirections
import com.cp.fishthebreak.utils.createUrlANdShare
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.hideKeyboardFrom
import com.cp.fishthebreak.utils.onSearch
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.group.ShareInGroupViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShareInGroupBottomSheet(val data: NavigationDirections) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentShareInGroupBottomSheetBinding
    val viewModel: ShareInGroupViewModel by viewModels()
    private var isPagination = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentShareInGroupBottomSheetBinding.inflate(layoutInflater, container, false)
        try {
            (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
            (dialog as? BottomSheetDialog)?.behavior?.isDraggable = false
            isCancelable = false
        } catch (ex: Exception) {
        }
        binding.extraSpace.minimumHeight = (Resources.getSystem().displayMetrics.heightPixels) / 2
        initDataBinding()
        initViewModelResponse()
        initListeners()
        return binding.root
    }

    private fun initListeners() {
        binding.backButton.setOnClickListener {
            dismiss()
        }
        binding.buttonShareLink.setOnClickListener {
            when (data) {
                is NavigationDirections.Share -> {
                    when (data.data) {
                        is MapUiData.FishLogData -> {
                            requireContext().createUrlANdShare(FISH_LOG_TYPE, data.data.data.id)
                        }

                        is MapUiData.LocationData -> {
                            requireContext().createUrlANdShare(LOCATION_TYPE, data.data.data.id)
                        }

                        is MapUiData.RouteData -> {
                            requireContext().createUrlANdShare(ROUTE_TYPE, data.data.data.id)
                        }

                        is MapUiData.TrollingData -> {
                            requireContext().createUrlANdShare(TROLLING_TYPE, data.data.data.id)
                        }

                        else -> {}
                    }
                }

                else -> {}
            }
        }
    }

    private fun initDataBinding() {
        binding.lifecycleOwner = this@ShareInGroupBottomSheet
        binding.viewModel = viewModel
        viewModel.onShareDataChange(data)
        val mLayoutManager = LinearLayoutManager(requireContext())
        binding.rvUserList.layoutManager = mLayoutManager
        binding.userListAdapter = ShareInGroupAdapter(listOf(), viewModel)
        var pastVisiblesItems = 0
        var visibleItemCount: Int = 0
        var totalItemCount: Int = 0
        binding.rvUserList.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    visibleItemCount = mLayoutManager.getChildCount()
                    totalItemCount = mLayoutManager.getItemCount()
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition()
                    if (!isPagination && viewModel.nextPage.value != null) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            isPagination = true
                            viewModel.getAllGroups()
                        }
                    }
                }

            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    private fun initViewModelResponse() {
        lifecycleScope.launch {
            viewModel.groupListResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    isPagination = false
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
            viewModel.shareResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            if (response.data?.status == true && response.data.statusCode == 200 && response.data.data?.sent == true) {
                                showHideLoader(false)
                                requireActivity().showToast(
                                    response.data.message, true
                                )
                                dismiss()
                            } else {
                                showHideLoader(false)
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
        lifecycleScope.launch {
            viewModel.loadingResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    showHideLoader(response)
                }
        }
        lifecycleScope.launch {
            viewModel.notifyAdapter
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    if (response) {
                        binding.rvUserList.adapter?.notifyDataSetChanged()
                        viewModel.resetNotifyResponse()
                    }
                }
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

}