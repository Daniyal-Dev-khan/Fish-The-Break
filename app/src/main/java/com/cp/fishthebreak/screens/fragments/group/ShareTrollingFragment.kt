package com.cp.fishthebreak.screens.fragments.group

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.group.ShareTrollingAdapter
import com.cp.fishthebreak.databinding.FragmentShareTrollingBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.cp.fishthebreak.screens.bottom_sheets.ConfirmBottomSheet
import com.cp.fishthebreak.screens.bottom_sheets.locations.SaveTrollingNameBottomSheet
import com.cp.fishthebreak.utils.Constants
import com.cp.fishthebreak.utils.MapUiData
import com.cp.fishthebreak.utils.NavigationDirections
import com.cp.fishthebreak.utils.StartDestination
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.group.SharePointInGroupViewModel
import com.cp.fishthebreak.viewModels.saved.SavedTrollingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "groupId"
@AndroidEntryPoint
class ShareTrollingFragment : Fragment() {
    private var groupId: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupId = it.getInt(ARG_PARAM1)
        }
    }
    private lateinit var binding: FragmentShareTrollingBinding
    private var isPagination = false
    val viewModel: SavedTrollingViewModel by activityViewModels()
    val viewModelSharePoint: SharePointInGroupViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentShareTrollingBinding.inflate(layoutInflater,container,false)
        initDataBinding()
        initViewModelResponse()
        return binding.root
    }


    private fun initDataBinding() {
        binding.lifecycleOwner = this@ShareTrollingFragment
        binding.viewModel = viewModel
        val mLayoutManager = LinearLayoutManager(requireContext())
        binding.rv.layoutManager = mLayoutManager
        binding.locationAdapter = ShareTrollingAdapter(listOf(), viewModel)
        var pastVisiblesItems = 0
        var visibleItemCount: Int = 0
        var totalItemCount: Int = 0
        binding.rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    visibleItemCount = mLayoutManager.getChildCount()
                    totalItemCount = mLayoutManager.getItemCount()
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition()
                    if (!isPagination && viewModel.nextPage.value != null) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            isPagination = true
                            viewModel.getAllSavedTrolling()
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
            viewModel.trollingResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    isPagination = false
                    when (response) {
                        is NetworkResult.Success -> {
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
            viewModel.deleteResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true) {
                                requireActivity().showToast(
                                    response.data.message , true
                                )
                                binding.rv.adapter?.notifyDataSetChanged()
                                viewModel.resetResponse()
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
                    if(response){
                        binding.rv.adapter?.notifyDataSetChanged()
                        viewModel.resetNotifyResponse()
                    }
                }
        }
        lifecycleScope.launch {
            viewModel.navigationResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    if(response != null){
                        when(response){
                            is NavigationDirections.CommonMap->{
                                val sIntent = Intent(requireContext(), NavGraphActivity::class.java)
                                sIntent.putExtra(
                                    Constants.START_DESTINATION,
                                    StartDestination.CommonMap(response.data)
                                )
                                startActivity(sIntent)
                            }
                            is NavigationDirections.DeletePoints->{
                                if(response.data is MapUiData.TrollingData) {
                                    val dialog = ConfirmBottomSheet(
                                        resources.getString(R.string.confirmation),
                                        resources.getString(R.string.delete_point_alert),
                                        object : ConfirmBottomSheet.OnItemClickListener {
                                            override fun onYesClick() {
                                                viewModel.deleteLocations((response.data.data.id))
                                            }

                                            override fun onCancelClick() {

                                            }

                                        })
                                    dialog.show(childFragmentManager, "ConfirmBottomSheet")
                                }
                            }
                            is NavigationDirections.Share->{
                                groupId?.let { messageId->
                                    if (response.data is MapUiData.TrollingData) {
                                        viewModelSharePoint.shareInGroup(messageId,"trolling",response.data.data.id)
                                    }
                                }
                            }
                            is NavigationDirections.ViewPoints->{
                                if(response.data is MapUiData.TrollingData) {
                                    val dialog = SaveTrollingNameBottomSheet(object :
                                        SaveTrollingNameBottomSheet.OnItemClickListener {
                                        override fun onSaveClick(name: String) {
                                            viewModel.updateTrolling(response.data.data, name)
                                        }

                                    },trollingName = response.data.data.trolling_name)
                                    dialog.show(childFragmentManager, "SaveTrollingNameBottomSheet")
                                }
                            }
                            else->{}
                        }
                        viewModel.resetNavigationResponse()
                    }
                }
        }
    }

    private fun showHideLoader(visibility: Boolean) {
        if (visibility) {
            if (!binding.pullToRefresh.isRefreshing) {
                if(viewModel.list.value.isEmpty()) {
                    binding.loaderLayout.viewVisible()
                }else{
                    binding.paginationLoaderLayout.viewVisible()
                }
            }
        } else {
            binding.loaderLayout.viewGone()
            binding.paginationLoaderLayout.viewGone()
            binding.pullToRefresh.isRefreshing = false
        }
        showHideNoData(viewModel.list.value.isEmpty())
    }

    private fun showHideNoData(visibility: Boolean) {
        if (visibility) {
            binding.noDataLayout.viewVisible()
        } else {
            binding.noDataLayout.viewGone()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(groupId: Int ) =
            ShareTrollingFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, groupId)
                }
            }
    }

}