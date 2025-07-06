package com.cp.fishthebreak.screens.fragments.group

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.group.FindGroupMemberAdapter
import com.cp.fishthebreak.databinding.FragmentGroupInfoBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.models.auth.User
import com.cp.fishthebreak.models.group.GroupMember
import com.cp.fishthebreak.screens.activities.BaseActivityResult
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.cp.fishthebreak.screens.bottom_sheets.group.FindUsersBottomSheet
import com.cp.fishthebreak.utils.Constants.Companion.MESSAGE_DISPLAY_TIME
import com.cp.fishthebreak.utils.SelectImageListener
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.loadImage
import com.cp.fishthebreak.utils.recycler.RecyclerTouchListener
import com.cp.fishthebreak.utils.selectImage
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.group.update.GroupInfoViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GroupInfoFragment : Fragment() {
    @Inject
    lateinit var sharePreferenceHelper: SharePreferenceHelper
    private lateinit var binding: FragmentGroupInfoBinding
    private var isPagination = false
    val viewModel: GroupInfoViewModel by viewModels()
    private val navArgs: GroupInfoFragmentArgs by navArgs()
    private val activityLauncher: BaseActivityResult<Intent, ActivityResult> =
        BaseActivityResult.registerActivityForResult(this)
    private var resetData = true
    private var isGroupLeaveOrRemoved = false
    private var touchListener: RecyclerTouchListener? = null
    private var list = ArrayList<GroupMember>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentGroupInfoBinding.inflate(layoutInflater,container,false)
        initListeners()
        initDataBinding()
        initViewModelResponse()
        return binding.root
    }

    private fun initListeners(){
        binding.backButton.setOnSingleClickListener {
            if(requireActivity() is NavGraphActivity){
                (requireActivity() as NavGraphActivity).onBack()
            }
        }
        binding.ivAdd.setOnSingleClickListener {
            imagePickerAvatar()
        }
        binding.addMembersButton.setOnClickListener {
            val dialog = FindUsersBottomSheet(resetData, navArgs.data.id, object: FindUsersBottomSheet.OnClickListeners{
                override fun onSaveClick(users: List<User>) {
                    if(users.isNotEmpty()) {
                        viewModel.addMembers(users)
                    }
                }

            })
            dialog.show(childFragmentManager,"FindUsersBottomSheet")
        }
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
                    binding.ivProfile.loadImage(
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

    private fun initDataBinding() {
        binding.lifecycleOwner = this@GroupInfoFragment
        binding.model = viewModel
        binding.groupData = navArgs.data

        val mLayoutManager = LinearLayoutManager(requireContext())
        binding.rvUserList.layoutManager = mLayoutManager
        binding.rvUserList.adapter = FindGroupMemberAdapter(list)
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
        if(navArgs.data.owner){
            binding.tvTitle.text = resources.getString(R.string.edit_group)
            binding.addMembersButton.viewVisible()
            touchListener = RecyclerTouchListener(requireActivity(), binding.rvUserList)
            touchListener
                ?.setClickable(object : RecyclerTouchListener.OnRowClickListener {
                    override fun onRowClicked(position: Int) {
//                    Toast.makeText(
//                        applicationContext,
//                        taskList.get(position).getName(),
//                        Toast.LENGTH_SHORT
//                    ).show()
                    }

                    override fun onIndependentViewClicked(independentViewID: Int, position: Int) {}
                })
                ?.setSwipeOptionViews(R.id.layoutDelete)
                ?.setSwipeable(R.id.rowFG, R.id.rowBG, object :
                    RecyclerTouchListener.OnSwipeOptionsClickListener {
                    override fun onSwipeOptionClicked(viewID: Int, position: Int) {
                        when (viewID) {
                            R.id.layoutDelete -> {
                                if(!navArgs.data.owner){
                                    isGroupLeaveOrRemoved = true
                                    viewModel.removeUser(position)
                                }else if(navArgs.data.owner && viewModel.list.value[position].user_id == sharePreferenceHelper.getUser()?.id){
//                        isGroupLeaveOrRemoved = true
                                    findNavController().navigate(GroupInfoFragmentDirections.actionGroupInfoFragmentToDeleteGroupFragment(navArgs.data))
                                }else{
                                    viewModel.removeUser(position)
                                }
                                //binding.rvUserList.adapter?.notifyDataSetChanged()
                            }
                        }
                    }
                })
            touchListener?.let {  binding.rvUserList.addOnItemTouchListener(it)}
            /*var itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                chatListAdapter.deleteItem(viewHolder.adapterPosition)
                    if(!navArgs.data.owner){
                        isGroupLeaveOrRemoved = true
                        viewModel.removeUser(viewHolder.adapterPosition)
                    }else if(navArgs.data.owner && viewModel.list.value[viewHolder.adapterPosition].user_id == sharePreferenceHelper.getUser()?.id){
//                        isGroupLeaveOrRemoved = true
                        findNavController().navigate(GroupInfoFragmentDirections.actionGroupInfoFragmentToDeleteGroupFragment(navArgs.data))
                    }else{
                        viewModel.removeUser(viewHolder.adapterPosition)
                    }
                    binding.rvUserList.adapter?.notifyDataSetChanged()
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {

                    RecyclerViewSwipeDecorator.Builder(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                        .addBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.error500
                            )
                        )
                        .setSwipeLeftLabelColor(Color.WHITE)
                        .addSwipeLeftLabel("Remove")
                        .create()
                        .decorate()

                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }

            }).attachToRecyclerView(binding.rvUserList)*/
        }else{
            binding.addMembersButton.viewGone()
            binding.saveButton.viewGone()
            binding.ivAdd.viewGone()
            binding.etName.isEnabled = false
            binding.tvTitle.text = resources.getString(R.string.group_derails)
        }
    }

    private fun initViewModelResponse() {
        lifecycleScope.launch {
            viewModel.groupMemberResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    isPagination = false
                    when (response) {
                        is NetworkResult.Success -> {
                            if (response.data?.status == true) {
                                navArgs.data.members = viewModel.list.value.size
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
            viewModel.updateGroupResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true && response.data.data?.updated == true) {
                                requireActivity().showToast(
                                    response.data.message , true
                                )
                                navArgs.data.name = viewModel.groupInfoUIStates.value.name
                                navArgs.data.profile_pic = viewModel.groupInfoUIStates.value.image
                                if(requireActivity() is NavGraphActivity){
                                    (requireActivity() as NavGraphActivity).onBack()
                                }
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
            viewModel.leaveGroupResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true && response.data.data?.leaved == true) {
                                requireActivity().showToast(
                                    response.data.message , true
                                )
                                navArgs.data.members = viewModel.list.value.size
                                if(isGroupLeaveOrRemoved){
                                    delay(MESSAGE_DISPLAY_TIME)
                                    requireActivity().finish()
                                }
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
            viewModel.addMembersResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true && response.data.data?.added_group_member == true) {
                                requireActivity().showToast(
                                    response.data.message , true
                                )
                                resetData = true
                            } else {
                                requireActivity().showToast(
                                    response.data?.message ?: resources.getString(
                                        R.string.something_went_wrong
                                    ), false
                                )
                                resetData = false
                                viewModel.resetResponse()
                            }
                        }

                        is NetworkResult.Error -> {
                            showHideLoader(false)
                            requireActivity().showToast(
                                response.message.getOnErrorMessage(),
                                false
                            )
                            resetData = false
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
                            resetData = false
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
                        list.clear()
                        list.addAll(viewModel.list.value)
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
//        if (visibility && !binding.pullToRefresh.isRefreshing) {
//            binding.loaderLayout.viewVisible()
//        } else {
//            binding.loaderLayout.viewGone()
//            binding.pullToRefresh.isRefreshing = false
//        }
    }

}