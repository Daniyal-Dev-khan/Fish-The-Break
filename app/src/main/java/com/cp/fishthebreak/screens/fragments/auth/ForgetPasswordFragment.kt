package com.cp.fishthebreak.screens.fragments.auth

import android.os.Bundle
import android.util.Log
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
import com.cp.fishthebreak.databinding.FragmentForgetPasswordBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.utils.Constants
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.auth.forget.ForgetPasswordViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ForgetPasswordFragment : Fragment() {

    private lateinit var binding: FragmentForgetPasswordBinding
    val viewModel: ForgetPasswordViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentForgetPasswordBinding.inflate(layoutInflater,container,false)
        binding.lifecycleOwner = this
        binding.model = viewModel
        initViewModelResponse()
        initListeners()
        return binding.root
    }

    private fun initListeners(){
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
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
            viewModel.forgetResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true && response.data.data) {
                                requireActivity().showToast(response.data.message, true)
                                //delay(Constants.MESSAGE_DISPLAY_TIME)
                                findNavController().navigate(ForgetPasswordFragmentDirections.actionForgetPasswordFragmentToOtpPasswordFragment("forget",binding.etEmail.text.toString()))
                                viewModel.resetServerResponse()
                            } else {
                                requireActivity().showToast(
                                    response.data?.message ?: resources.getString(
                                        R.string.something_went_wrong
                                    ), false
                                )
                                viewModel.resetServerResponse()
                            }
                        }

                        is NetworkResult.Error -> {
                            showHideLoader(false)
                            requireActivity().showToast(
                                response.message.getOnErrorMessage(),
                                false
                            )
                            viewModel.resetServerResponse()
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
                            viewModel.resetServerResponse()
                        }

                        is NetworkResult.NoCallYet -> {
                            Log.i("ForgetFragment","NetworkResult.NoCallYet")
                        }
                    }
                }
        }

    }

}