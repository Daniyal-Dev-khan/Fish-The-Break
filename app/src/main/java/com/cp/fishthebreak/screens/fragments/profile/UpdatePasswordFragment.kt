package com.cp.fishthebreak.screens.fragments.profile

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
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.FragmentUpdatePasswordBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.cp.fishthebreak.screens.fragments.auth.SetPasswordFragmentDirections
import com.cp.fishthebreak.utils.Constants.Companion.MESSAGE_DISPLAY_TIME
import com.cp.fishthebreak.utils.applyAsteriskPasswordTransformation
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.hideShowPassword
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.profile.password.UpdatePasswordViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UpdatePasswordFragment : Fragment() {
    private lateinit var binding: FragmentUpdatePasswordBinding
    val viewModel: UpdatePasswordViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentUpdatePasswordBinding.inflate(layoutInflater,container,false)
        binding.lifecycleOwner = this
        binding.model = viewModel
        initListeners()
        initViewModelResponse()
        return binding.root
    }

    private fun initListeners(){
        binding.etPassword.applyAsteriskPasswordTransformation()
        binding.etConfirmPassword.applyAsteriskPasswordTransformation()
        binding.etCurrentPassword.applyAsteriskPasswordTransformation()
        binding.backButton.setOnClickListener {
            if(requireActivity() is NavGraphActivity){
                (requireActivity() as NavGraphActivity).onBack()
            }
        }
        binding.ivCurrentPasswordHideShow.setOnClickListener {
            binding.etCurrentPassword.hideShowPassword(
                binding.ivCurrentPasswordHideShow
            )
        }
        binding.ivNewPasswordHideShow.setOnClickListener {
            binding.etPassword.hideShowPassword(
                binding.ivNewPasswordHideShow
            )
        }
        binding.ivConfirmPasswordHideShow.setOnClickListener {
            binding.etConfirmPassword.hideShowPassword(
                binding.ivConfirmPasswordHideShow
            )
        }

    }

    private fun initViewModelResponse() {
        lifecycleScope.launch {
            viewModel.response
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true) {
                                requireActivity().showToast(response.data.message, true)
                                delay(MESSAGE_DISPLAY_TIME)
                                if(requireActivity() is NavGraphActivity){
                                    (requireActivity() as NavGraphActivity).onBack()
                                }
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

    }

    private fun showHideLoader(visibility: Boolean) {
        if (visibility) {
            binding.loaderLayout.viewVisible()
        } else {
            binding.loaderLayout.viewGone()
        }
    }

}