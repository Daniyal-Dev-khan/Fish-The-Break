package com.cp.fishthebreak.screens.fragments.auth

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
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.FragmentSetPasswordBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.utils.Constants
import com.cp.fishthebreak.utils.applyAsteriskPasswordTransformation
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.hideShowPassword
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.auth.forget.ResetPasswordViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SetPasswordFragment : Fragment() {
    private lateinit var binding: FragmentSetPasswordBinding
    val viewModel: ResetPasswordViewModel by viewModels()
    val navArgs: SetPasswordFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSetPasswordBinding.inflate(layoutInflater,container,false)
        binding.lifecycleOwner = this
        binding.model = viewModel
        viewModel.onEmailChangeEvent(navArgs.email)
        viewModel.onOtpChangeEvent(navArgs.otp)
        initViewModelResponse()
        initListeners()
        return binding.root
    }

    private fun initListeners(){
        binding.etPassword.applyAsteriskPasswordTransformation()
        binding.etConfirmPassword.applyAsteriskPasswordTransformation()
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.ivPasswordHideShow.setOnClickListener {
            binding.etPassword.hideShowPassword(
                binding.ivPasswordHideShow
            )
        }
        binding.ivConfirmPasswordHideShow.setOnClickListener {
            binding.etConfirmPassword.hideShowPassword(
                binding.ivConfirmPasswordHideShow
            )
        }

    }

    private fun showHideLoader(visibility: Boolean) {
        if (visibility) {
            binding.loaderLayout.viewVisible()
        } else {
            binding.loaderLayout.viewGone()
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
                                //delay(Constants.MESSAGE_DISPLAY_TIME)
                                findNavController().navigate(SetPasswordFragmentDirections.actionSetPasswordFragmentToPasswordSuccessFragment())
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

}