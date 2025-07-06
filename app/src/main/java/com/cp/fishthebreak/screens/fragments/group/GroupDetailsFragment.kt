package com.cp.fishthebreak.screens.fragments.group

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.group.MessageAdapter
import com.cp.fishthebreak.databinding.FragmentGroupDetailsBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.models.group.Message
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.cp.fishthebreak.screens.bottom_sheets.ConfirmBottomSheet
import com.cp.fishthebreak.screens.bottom_sheets.group.GroupOptionBottomSheet
import com.cp.fishthebreak.screens.bottom_sheets.group.SharePointsBottomSheet
import com.cp.fishthebreak.screens.bottom_sheets.routes.SelectLibraryBottomSheet
import com.cp.fishthebreak.utils.Constants
import com.cp.fishthebreak.utils.Constants.Companion.MESSAGE_DISPLAY_TIME
import com.cp.fishthebreak.utils.Constants.Companion.MESSAGE_PAGINATION_LOAD
import com.cp.fishthebreak.utils.MapUiData
import com.cp.fishthebreak.utils.NavigationDirections
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.cp.fishthebreak.utils.StartDestination
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.group.ChatMessagesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GroupDetailsFragment : Fragment() {
    private lateinit var binding: FragmentGroupDetailsBinding
    private val navArgs: GroupDetailsFragmentArgs by navArgs()
    val viewModel: ChatMessagesViewModel by viewModels()
    private var isPagination = false

    @Inject
    lateinit var sharePreferenceHelper: SharePreferenceHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentGroupDetailsBinding.inflate(layoutInflater, container, false)
        if(requireActivity() is NavGraphActivity){
            (requireActivity() as NavGraphActivity).setStatusBarBackgroundTransparent()
        }
        binding.lifecycleOwner = this@GroupDetailsFragment
        binding.viewModel = viewModel
        binding.chatData = navArgs.chatData
        initAdapter()
        initViewModelResponse()
        initListeners()
        return binding.root
    }

    private fun initListeners() {
        binding.buttonShare.setOnClickListener {
            val dialog = SharePointsBottomSheet(navArgs.chatData.id, object: SharePointsBottomSheet.OnClickListeners{
                override fun onCancel() {

                }

                override fun onShareClick() {
                    viewModel.getAllChats(1)
                }

            })
            dialog.show(childFragmentManager,"SharePointsBottomSheet")
        }
        binding.tvName.setOnSingleClickListener {
            findNavController().navigate(
                GroupDetailsFragmentDirections.actionGroupDetailsFragmentToGroupInfoFragment(
                    navArgs.chatData
                )
            )
        }
        binding.tvGroupMembersCount.setOnSingleClickListener {
            findNavController().navigate(
                GroupDetailsFragmentDirections.actionGroupDetailsFragmentToGroupInfoFragment(
                    navArgs.chatData
                )
            )
        }
        binding.ivMore.setOnSingleClickListener {
            val dialog = GroupOptionBottomSheet(
                viewModel.chatListResponse.value.data?.owner ?: false,
                object : GroupOptionBottomSheet.OnClickListeners {
                    override fun onEditClick() {
                        findNavController().navigate(
                            GroupDetailsFragmentDirections.actionGroupDetailsFragmentToGroupInfoFragment(
                                navArgs.chatData
                            )
                        )
                    }

                    override fun onDeleteClick() {
                        val dialog = ConfirmBottomSheet(
                            resources.getString(R.string.alert_message),
                            if (viewModel.chatListResponse.value.data?.owner == true) {
                                resources.getString(R.string.delete_group_alert)

                            } else {
                                resources.getString(R.string.leave_group_alert)
                            },
                            object : ConfirmBottomSheet.OnItemClickListener {
                                override fun onYesClick() {
                                    viewModel.leaveGroup()
                                }

                                override fun onCancelClick() {

                                }

                            })
                        dialog.show(childFragmentManager, "ConfirmBottomSheet")
                    }

                    override fun onCancelClick() {

                    }

                })
            dialog.show(childFragmentManager, "GroupOptionBottomSheet")
        }
        binding.backButton.setOnSingleClickListener {
            if (requireActivity() is NavGraphActivity) {
                (requireActivity() as NavGraphActivity).onBack()
            }
        }
    }

    private fun initAdapter() {
        val mLayoutManager = LinearLayoutManager(requireContext())
        binding.rv.layoutManager = mLayoutManager
        binding.rv.adapter = MessageAdapter(
            viewModel.list.value,
            sharePreferenceHelper.getUser()?.id ?: -1,
            object : MessageAdapter.OnItemClickListener {
                override fun onBookMark(data: Message) {
                    viewModel.bookMarkChat(data)
                }

                override fun onChatClick(data: Message) {
                    findNavController().navigate(
                        GroupDetailsFragmentDirections.actionGroupDetailsFragmentToCommonMapFragment(
                            if(data.getLatFromString() == null || data.getLangFromString() == null){
                                MapUiData.FromMessage(
                                    data.shareable_id, data.shareable_type,
                                    lat = null, lang = null
                                )
                            }else{
                                MapUiData.FromMessage(
                                    data.shareable_id, data.shareable_type,
                                    lat = data.getLatFromString(), lang = data.getLangFromString()
                                )
                            }
                        )
                    )
                }

            })
        binding.rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
                if (dy < 0) {
                    if (!binding.rv.canScrollVertically(-1)) {
                        if (!isPagination && viewModel.nextPage.value != null) {
                            isPagination = true
                            viewModel.getAllChats()
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
            viewModel.bookMarkResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    isPagination = false
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true && response.data.data?.saved == true) {
                                requireActivity().showToast(response.data.message, true)
                                binding.rv.adapter?.notifyDataSetChanged()
                            } else {
                                requireActivity().showToast(
                                    response.data?.message ?: resources.getString(
                                        R.string.something_went_wrong
                                    ), false
                                )
                            }
                            viewModel.resetBookMarkResponse()
                        }

                        is NetworkResult.Error -> {
                            requireActivity().showToast(
                                response.message.getOnErrorMessage(),
                                false
                            )
                            viewModel.resetBookMarkResponse()
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
                            viewModel.resetBookMarkResponse()
                        }

                        is NetworkResult.NoCallYet -> {
                        }
                    }
                }
        }
        lifecycleScope.launch {
            viewModel.leaveGroupResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    isPagination = false
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true && response.data.data?.leaved == true) {
                                requireActivity().showToast(response.data.message, true)
                                delay(MESSAGE_DISPLAY_TIME)
                                requireActivity().finish()
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
                    if (response) {
                        if (viewModel.list.value.size <= MESSAGE_PAGINATION_LOAD && viewModel.list.value.isNotEmpty()) {
                            binding.rv.adapter?.notifyDataSetChanged()
                            binding.rv.scrollToPosition(viewModel.list.value.size - 1)
                        } else if (viewModel.list.value.size > MESSAGE_PAGINATION_LOAD) {
                            val newItemCount = viewModel.list.value.size % MESSAGE_PAGINATION_LOAD
                            if (newItemCount == 0) {
                                binding.rv.adapter?.notifyItemRangeInserted(
                                    0,
                                    MESSAGE_PAGINATION_LOAD
                                )
                                binding.rv.adapter?.notifyItemChanged(MESSAGE_PAGINATION_LOAD)
                            } else {
                                binding.rv.adapter?.notifyItemRangeInserted(0, newItemCount)
                                binding.rv.adapter?.notifyItemChanged(newItemCount)
                            }
                        }
                        viewModel.resetNotifyResponse()
                    }
                }
        }
        lifecycleScope.launch {
            viewModel.navigationResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    if (response != null) {
                        when (response) {
                            is NavigationDirections.OpenGroup -> {
                                val sIntent = Intent(requireContext(), NavGraphActivity::class.java)
                                sIntent.putExtra(
                                    Constants.START_DESTINATION,
                                    StartDestination.OpenGroup(response.data)
                                )
                                startActivity(sIntent)
                            }

                            else -> {}
                        }
                        viewModel.resetNavigationResponse()
                    }
                }
        }
    }

    private fun showHideLoader(visibility: Boolean) {
        if (visibility) {
            if (!binding.pullToRefresh.isRefreshing) {
                binding.loaderLayout.viewVisible()
            }
        } else {
            binding.loaderLayout.viewGone()
            binding.pullToRefresh.isRefreshing = false
        }
        if (viewModel.list.value.isEmpty()) {
            binding.noDataLayout.viewVisible()
        } else {
            binding.noDataLayout.viewGone()
        }
    }

}