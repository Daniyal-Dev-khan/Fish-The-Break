package com.cp.fishthebreak.screens.fragments.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.FragmentOtpPasswordBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.screens.activities.MainActivity
import com.cp.fishthebreak.utils.Constants
import com.cp.fishthebreak.utils.Constants.Companion.MESSAGE_DISPLAY_TIME
import com.cp.fishthebreak.utils.GenericKeyEvent
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.auth.otp.OtpViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OtpPasswordFragment : Fragment() {

    private lateinit var binding: FragmentOtpPasswordBinding
    private val navArgs: OtpPasswordFragmentArgs by navArgs()
    val viewModel: OtpViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOtpPasswordBinding.inflate(layoutInflater, container, false)
        binding.model = viewModel
        initViewModelResponse()
        initListeners()
        return binding.root
    }


    private fun initListeners() {
        viewModel.onEmailChangeEvent(navArgs.email)
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
        textChangeListener(binding.etCode1)
        textChangeListener(binding.etCode2)
        textChangeListener(binding.etCode3)
        textChangeListener(binding.etCode4)
        //GenericKeyEvent here works for deleting the element and to switch back to previous EditText
        //first parameter is the current EditText and second parameter is previous EditText
        binding.etCode1.setOnKeyListener(GenericKeyEvent(binding.etCode1, null))
        binding.etCode2.setOnKeyListener(
            GenericKeyEvent(
                binding.etCode2,
                binding.etCode1
            )
        )
        binding.etCode3.setOnKeyListener(
            GenericKeyEvent(
                binding.etCode3,
                binding.etCode2
            )
        )
        binding.etCode4.setOnKeyListener(
            GenericKeyEvent(
                binding.etCode4,
                binding.etCode3
            )
        )
        binding.buttonContinue.setOnClickListener {
            if (navArgs.fromScreen == "signup" || navArgs.fromScreen == "login") {
                viewModel.onNextEvent(binding.buttonContinue)
            } else if (navArgs.fromScreen == "forget") {
                viewModel.onNextPasswordResetEvent(binding.buttonContinue)
            }
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
                                if (navArgs.fromScreen == "signup") {
                                    requireActivity().showToast(response.data.message, true)
                                    //delay(MESSAGE_DISPLAY_TIME)
                                    findNavController().navigate(OtpPasswordFragmentDirections.actionOtpPasswordFragmentToSubscriptionFragment())
//                                    delay(MESSAGE_DISPLAY_TIME)
//                                    startActivity(Intent(requireContext(),MainActivity::class.java))
//                                    requireActivity().finish()
                                    //viewModel.resetResponse()
                                } else if (navArgs.fromScreen == "login") {
                                    requireActivity().showToast(response.data.message, true)
                                    delay(MESSAGE_DISPLAY_TIME)
                                    startActivity(
                                        Intent(
                                            requireContext(),
                                            MainActivity::class.java
                                        )
                                    )
                                    requireActivity().finish()
                                    //viewModel.resetResponse()
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
            viewModel.resendResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true && response.data.data) {
                                requireActivity().showToast(response.data.message, true)
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
            viewModel.resetResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true && response.data.data?.verify == true) {
                                requireActivity().showToast(response.data.message, true)
                                //delay(MESSAGE_DISPLAY_TIME)
                                findNavController().navigate(
                                    OtpPasswordFragmentDirections.actionOtpPasswordFragmentToSetPasswordFragment(
                                        navArgs.email,
                                        binding.etCode1.text.toString() + binding.etCode2.text.toString() + binding.etCode3.text.toString() + binding.etCode4.text.toString()
                                    )
                                )
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
            viewModel.validationError
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    if (response.isNotEmpty()) {
                        requireActivity().showToast(response, false)
                        viewModel.resetError()
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

    private fun textChangeListener(edittext: EditText) {
        edittext.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (edittext.text.toString().trim().length == 1) {
                    edittext.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.bg_code_active)
                } else {
                    edittext.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.bg_code)
                }
                when (edittext.id) {
                    binding.etCode1.id -> {
                        viewModel.onCode1ChangeEvent(p0 ?: "")
                        binding.etCode2.requestFocus()
                    }

                    binding.etCode2.id -> {
                        viewModel.onCode2ChangeEvent(p0 ?: "")
                        binding.etCode3.requestFocus()
                    }

                    binding.etCode3.id -> {
                        viewModel.onCode3ChangeEvent(p0 ?: "")
                        binding.etCode4.requestFocus()
                    }

                    binding.etCode4.id -> {
                        viewModel.onCode4ChangeEvent(p0 ?: "")
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
    }
}