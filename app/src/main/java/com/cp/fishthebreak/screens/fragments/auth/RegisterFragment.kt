package com.cp.fishthebreak.screens.fragments.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.cp.fishthebreak.databinding.FragmentRegisterBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.utils.Constants.Companion.PrivacyPolicyUrl
import com.cp.fishthebreak.utils.Constants.Companion.TermsAndConditionsUrl
import com.cp.fishthebreak.utils.applyAsteriskPasswordTransformation
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.hideKeyboardFrom
import com.cp.fishthebreak.utils.hideShowPassword
import com.cp.fishthebreak.utils.makeLinks
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.auth.signup.SignupViewModel
import com.hbb20.CountryCodePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    val viewModel: SignupViewModel by viewModels()
    private lateinit var ccp: CountryCodePicker
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentRegisterBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.model = viewModel
        ccp = CountryCodePicker(requireContext())
        ccp.registerCarrierNumberEditText(binding.etMobileHide)
        ccp.setAutoDetectedCountry(true)
        ccp.setNumberAutoFormattingEnabled(true)
        initViewModelResponse()
        initListeners()
        return binding.root
    }

    private fun initListeners() {
        binding.etPassword.applyAsteriskPasswordTransformation()
        binding.etConfirmPassword.applyAsteriskPasswordTransformation()
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.checkBox.makeLinks(
            Pair(resources.getString(R.string.privacy_policy), View.OnClickListener {
                findNavController().navigate(
                    RegisterFragmentDirections.actionRegisterFragmentToWebPageFragment(
                        PrivacyPolicyUrl
                    )
                )
            }),
            Pair(resources.getString(R.string.terms_amp_conditions), View.OnClickListener {
                findNavController().navigate(
                    RegisterFragmentDirections.actionRegisterFragmentToWebPageFragment(
                        TermsAndConditionsUrl
                    )
                )
            })
        )
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
        binding.buttonRegister.setOnClickListener {
            binding.etMobileHide.setText(binding.etMobile.text.toString())
            ccp.fullNumber = binding.etMobileHide.text.toString()
            if (binding.etMobile.text.toString().trim().isNotEmpty() && ccp.isValidFullNumber) {
                viewModel.onPhoneEvent(binding.etMobile.text.toString().trim())
                viewModel.onSignupEvent(it)
            } else {
                viewModel.onPhoneEvent("")
                viewModel.onSignupEvent(it)
            }
        }
        binding.btnLogin.setOnClickListener {
            requireContext().hideKeyboardFrom(binding.btnLogin)
            findNavController().navigateUp()
        }
        binding.etMobile.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.etMobileHide.setText(binding.etMobile.text.toString())
                ccp.fullNumber = binding.etMobileHide.text.toString()
                if (binding.etMobile.text.toString().trim().isNotEmpty() && ccp.isValidFullNumber) {
                    viewModel.onPhoneEvent(binding.etMobile.text.toString().trim())
                } else {
                    viewModel.onPhoneEvent("")
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
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
                                //delay(MESSAGE_DISPLAY_TIME)
                                findNavController().navigate(
                                    RegisterFragmentDirections.actionRegisterFragmentToOtpPasswordFragment(
                                        "signup",
                                        response.data.data?.email ?: ""
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

    }
}