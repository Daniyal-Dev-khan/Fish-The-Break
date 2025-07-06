package com.cp.fishthebreak.screens.fragments.group

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.group.ChatListAdapter
import com.cp.fishthebreak.adapters.routes.RouteFishLogAdapter
import com.cp.fishthebreak.databinding.FragmentGroupBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.screens.activities.BaseActivityResult
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.cp.fishthebreak.screens.bottom_sheets.group.CreateGroupBottomSheet
import com.cp.fishthebreak.utils.Constants
import com.cp.fishthebreak.utils.NavigationDirections
import com.cp.fishthebreak.utils.StartDestination
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.group.ChatListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GroupFragment : Fragment() {
    private var isPagination = false
    private lateinit var binding: FragmentGroupBinding
    val viewModel: ChatListViewModel by activityViewModels()
    private val activityLauncher: BaseActivityResult<Intent, ActivityResult> =
        BaseActivityResult.registerActivityForResult(this)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentGroupBinding.inflate(layoutInflater,container,false)
        initDataBinding()
        initViewModelResponse()
        initListeners()
        return binding.root
    }

    private fun initListeners(){
        binding.ivAdd.setOnSingleClickListener {
            val dialog = CreateGroupBottomSheet(object: CreateGroupBottomSheet.OnGroupCreateListener{
                override fun groupCreated() {
                    viewModel.getAllGroups(1)
                }
            })
            dialog.show(childFragmentManager,"CreateGroupBottomSheet")
        }
    }

    private fun initDataBinding() {
        binding.lifecycleOwner = this@GroupFragment
        binding.viewModel = viewModel
        val mLayoutManager = LinearLayoutManager(requireContext())
        binding.rv.layoutManager = mLayoutManager
        binding.chatAdapter = ChatListAdapter(listOf(), viewModel)
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
            viewModel.chatListResponse
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
            viewModel.acceptResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader1(false)
                            if (response.data?.status == true) {
                                requireActivity().showToast(
                                    response.data.message , true
                                )
                            } else {
                                requireActivity().showToast(
                                    response.data?.message ?: resources.getString(
                                        R.string.something_went_wrong
                                    ), false
                                )
                            }
                            viewModel.resetResponse()
                        }

                        is NetworkResult.Error -> {
                            showHideLoader1(false)
                            requireActivity().showToast(
                                response.message.getOnErrorMessage(),
                                false
                            )
                            viewModel.resetResponse()
                        }

                        is NetworkResult.Loading -> {
                            showHideLoader1(true)
                        }

                        is NetworkResult.NoInternet -> {
                            showHideLoader1(false)
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
            viewModel.rejectResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader1(false)
                            if (response.data?.status == true) {
                                requireActivity().showToast(
                                    response.data.message , true
                                )
                            } else {
                                requireActivity().showToast(
                                    response.data?.message ?: resources.getString(
                                        R.string.something_went_wrong
                                    ), false
                                )
                            }
                            viewModel.resetResponse()
                        }

                        is NetworkResult.Error -> {
                            showHideLoader1(false)
                            requireActivity().showToast(
                                response.message.getOnErrorMessage(),
                                false
                            )
                            viewModel.resetResponse()
                        }

                        is NetworkResult.Loading -> {
                            showHideLoader1(true)
                        }

                        is NetworkResult.NoInternet -> {
                            showHideLoader1(false)
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
                            is NavigationDirections.OpenGroup ->{
//                                if (response.data.status == 1) {
                                val sIntent =
                                    Intent(requireContext(), NavGraphActivity::class.java)
                                sIntent.putExtra(
                                    Constants.START_DESTINATION,
                                    StartDestination.OpenGroup(response.data)
                                )
                                activityLauncher.launch(
                                    sIntent,
                                    object :
                                        BaseActivityResult.OnActivityResult<ActivityResult> {
                                        @SuppressLint("NotifyDataSetChanged")
                                        override fun onActivityResult(result: ActivityResult) {
                                            viewModel.getAllGroups(1)
                                        }
                                    })
//                                }
                            }
                            is NavigationDirections.RejectReason ->{
                                val sIntent = Intent(requireContext(), NavGraphActivity::class.java)
                                sIntent.putExtra(Constants.START_DESTINATION, StartDestination.RejectGroupReason())
                                activityLauncher.launch(
                                    sIntent,
                                    object :
                                        BaseActivityResult.OnActivityResult<ActivityResult> {
                                        @SuppressLint("NotifyDataSetChanged")
                                        override fun onActivityResult(result: ActivityResult) {
                                            if (result.resultCode == Activity.RESULT_OK) {
                                                if (result.data != null) {
                                                    if (result.data?.hasExtra("selectedReason") == true) {
                                                        val reason = result.data?.getStringExtra("selectedReason")?:return
                                                        viewModel.rejectRequest(response.data,reason)
                                                    }
                                                }
                                            }
                                        }
                                    })
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
    }
    private fun showHideLoader1(visibility: Boolean) {
        if (visibility) {
            binding.loaderLayout.viewVisible()
        } else {
            binding.loaderLayout.viewGone()
        }
    }


}