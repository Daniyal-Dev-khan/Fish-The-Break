package com.cp.fishthebreak.screens.bottom_sheets.group

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.FragmentCreateGroupBottomSheetBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.models.auth.User
import com.cp.fishthebreak.screens.activities.BaseActivityResult
import com.cp.fishthebreak.screens.fragments.profile.ChangeEmailFragmentDirections
import com.cp.fishthebreak.utils.SelectImageListener
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.loadImage
import com.cp.fishthebreak.utils.selectImage
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.group.create.CreateGroupViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateGroupBottomSheet(private val listener: OnGroupCreateListener) : BottomSheetDialogFragment() {
    val viewModel: CreateGroupViewModel by viewModels()
    private lateinit var binding: FragmentCreateGroupBottomSheetBinding
    private val activityLauncher: BaseActivityResult<Intent, ActivityResult> =
        BaseActivityResult.registerActivityForResult(this)
    private var resetData = true
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
        binding = FragmentCreateGroupBottomSheetBinding.inflate(layoutInflater,container,false)
        binding.lifecycleOwner = this
        binding.model = viewModel
        initViewModelResponse()
        initListeners()
        return binding.root
    }

    private fun initListeners(){
        binding.backButton.setOnClickListener {
            dismiss()
        }
        binding.addFriendsButton.setOnClickListener {
            val dialog = FindUsersBottomSheet(resetData, null, object: FindUsersBottomSheet.OnClickListeners{
                override fun onSaveClick(users: List<User>) {
                    resetData = false
                    viewModel.onUserListChangeEvent(users)
                    createUsersUi()
                }

            })
            dialog.show(childFragmentManager,"FindUsersBottomSheet")
        }
        binding.ivAdd.setOnClickListener {
            imagePickerAvatar()
        }
    }

    private fun createUsersUi(){
        if(viewModel.createGroupUIStates.value.users.isNotEmpty()) {
            viewModel.createGroupUIStates.value.users.forEachIndexed { index, user ->
                when (index) {
                    0 -> {
                        binding.ivUser1.loadImage(
                            user.base_url + user.profile_pic,
                            R.drawable.place_holder_square,
                            R.drawable.place_holder_square
                        )
                        binding.ivUser2.viewGone()
                        binding.tvUserCount.text = "1 Friend selected"
                    }

                    1 -> {
                        binding.ivUser2.loadImage(
                            user.base_url + user.profile_pic,
                            R.drawable.place_holder_square,
                            R.drawable.place_holder_square
                        )
                        binding.ivUser2.viewVisible()
                        binding.ivUser3.viewGone()
                        binding.tvUserCount.text = "2 Friends selected"
                    }

                    2 -> {
                        binding.ivUser3.loadImage(
                            user.base_url + user.profile_pic,
                            R.drawable.place_holder_square,
                            R.drawable.place_holder_square
                        )
                        binding.ivUser3.viewVisible()
                        binding.tvUserCount.text = "3 Friends selected"
                    }

                    else -> {
                        binding.tvUserCount.text = "+${viewModel.createGroupUIStates.value.users.size-3} Friends selected"
                    }
                }
            }
        }else{
            binding.ivUser2.viewGone()
            binding.ivUser3.viewGone()
            binding.ivUser1.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.place_holder_square))
            binding.tvUserCount.text = resources.getString(R.string.no_friends_selected_yet)
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

    private fun initViewModelResponse() {
        lifecycleScope.launch {
            viewModel.createGroupResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true && response.data.data?.created_group == true) {
                                requireActivity().showToast(response.data.message, true)
                                listener.groupCreated()
                                dismiss()
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

    interface OnGroupCreateListener{
        fun groupCreated()
    }

}