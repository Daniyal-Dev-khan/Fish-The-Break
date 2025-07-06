package com.cp.fishthebreak.screens.bottom_sheets.group

import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.group.ChatListAdapter
import com.cp.fishthebreak.adapters.group.FindUserAdapter
import com.cp.fishthebreak.adapters.group.SelectedUserAdapter
import com.cp.fishthebreak.databinding.FragmentFindUsersBottomSheetBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.models.auth.User
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.hideKeyboardFrom
import com.cp.fishthebreak.utils.onSearch
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.group.create.FindUserViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FindUsersBottomSheet(private val resetData:Boolean, private val roomId: Int? = null, private val listener: OnClickListeners) : BottomSheetDialogFragment() {
    private var isPagination = false
    private lateinit var binding: FragmentFindUsersBottomSheetBinding
    val viewModel: FindUserViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        try {
            (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
            (dialog as? BottomSheetDialog)?.behavior?.isDraggable = false
            isCancelable = false
        } catch (ex: Exception) {
        }
        binding = FragmentFindUsersBottomSheetBinding.inflate(layoutInflater,container,false)
        binding.extraSpace.minimumHeight = (Resources.getSystem().displayMetrics.heightPixels) / 2
        if(resetData){
            viewModel.resetList()
            viewModel.onRoomIdEvent(roomId)
            viewModel.getAllUser(1)
        }
        initDataBinding()
        initViewModelResponse()
        initListeners()
        return binding.root
    }

    private fun initListeners(){
        binding.etSearch.onSearch{
            requireContext().hideKeyboardFrom(binding.etSearch)
            viewModel.getAllUser(1)
        }
        binding.backButton.setOnClickListener {
            dismiss()
        }
        binding.saveButton.setOnClickListener {
            listener.onSaveClick(viewModel.selectedList.value)
            dismiss()
        }
    }

    private fun initDataBinding() {
        binding.lifecycleOwner = this@FindUsersBottomSheet
        binding.viewModel = viewModel

        binding.selectedListAdapter = SelectedUserAdapter(listOf(), viewModel)

        val mLayoutManager = LinearLayoutManager(requireContext())
        binding.rvUserList.layoutManager = mLayoutManager
        binding.userListAdapter = FindUserAdapter(listOf(), viewModel)
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
                            viewModel.getAllUser()
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

    interface OnClickListeners{
        fun onSaveClick(users: List<User>)
    }

}