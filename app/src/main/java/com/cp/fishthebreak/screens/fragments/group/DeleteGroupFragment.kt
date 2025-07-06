package com.cp.fishthebreak.screens.fragments.group

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.FragmentDeleteGroupBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.cp.fishthebreak.utils.Constants
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.loadImage
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.group.update.DeleteGroupViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DeleteGroupFragment : Fragment() {
    private lateinit var binding: FragmentDeleteGroupBinding
    val viewModel: DeleteGroupViewModel by viewModels()
    private val navArgs: DeleteGroupFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDeleteGroupBinding.inflate(layoutInflater,container,false)
        if(requireActivity() is NavGraphActivity){
            (requireActivity() as NavGraphActivity).setStatusBarBackgroundTransparent()
        }
        initDataBinding()
        initListeners()
        initViewModelResponse()
        return binding.root
    }

    private fun initDataBinding() {
        binding.lifecycleOwner = this@DeleteGroupFragment
        binding.model = viewModel
        binding.groupData = navArgs.groupData
        binding.ivProfile.loadImage(navArgs.groupData.base_url+navArgs.groupData.profile_pic,R.drawable.place_holder_square,R.drawable.place_holder_square)
    }

    private fun initListeners(){
        binding.buttonCancel.setOnClickListener {
            if(requireActivity() is NavGraphActivity){
                (requireActivity() as NavGraphActivity).onBack()
            }
        }
    }

    private fun initViewModelResponse() {
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
                                delay(Constants.MESSAGE_DISPLAY_TIME)
                                requireActivity().finish()
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
}