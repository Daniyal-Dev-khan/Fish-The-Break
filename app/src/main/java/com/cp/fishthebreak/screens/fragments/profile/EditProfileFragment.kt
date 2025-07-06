package com.cp.fishthebreak.screens.fragments.profile

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.FragmentEditProfileBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.screens.activities.BaseActivityResult
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.cp.fishthebreak.utils.Constants.Companion.MESSAGE_DISPLAY_TIME
import com.cp.fishthebreak.utils.SelectImageListener
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.getPathFromUri
import com.cp.fishthebreak.utils.isPhotoPickerAvailable
import com.cp.fishthebreak.utils.loadImage
import com.cp.fishthebreak.utils.selectFromImagePicker
import com.cp.fishthebreak.utils.selectImage
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.profile.edit.EditProfileViewModel
import com.hbb20.CountryCodePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditProfileFragment : Fragment() {
    private lateinit var binding: FragmentEditProfileBinding
    val viewModel: EditProfileViewModel by viewModels()
    private lateinit var ccp: CountryCodePicker
    private val activityLauncher: BaseActivityResult<Intent, ActivityResult> =
        BaseActivityResult.registerActivityForResult(this)

    var pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects media items or closes the
            // photo picker.
            if (uri != null) {
                val image = requireActivity().getPathFromUri(uri)
                if (image.isEmpty()) {
                    return@registerForActivityResult
                }
                binding.ivProfile.loadImage(
                    image,
                    R.drawable.place_holder_square,
                    R.drawable.place_holder_square
                )
                viewModel.onUserImageEvent(image)
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentEditProfileBinding.inflate(layoutInflater, container, false)
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
        binding.uploadImageButton.setOnClickListener {
            if (requireContext().isPhotoPickerAvailable() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                requireActivity().selectFromImagePicker(
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
                            viewModel.onUserImageEvent(path)
                        }

                        override fun onImageCancel() {

                        }

                    },
                    pickMultipleMedia
                )
            } else {
                imagePickerAvatar()
            }
        }


        binding.backButton.setOnClickListener {
            if (requireActivity() is NavGraphActivity) {
                (requireActivity() as NavGraphActivity).onBack()
            }
        }
        binding.saveButton.setOnClickListener {
            binding.etMobileHide.setText(binding.etMobile.text.toString())
            ccp.fullNumber = binding.etMobileHide.text.toString()
            if (binding.etMobile.text.toString().trim().isNotEmpty() && ccp.isValidFullNumber) {
                viewModel.onPhoneError(true)
                viewModel.onSaveClickEvent(it)
            } else {
                viewModel.onPhoneError(false)
                viewModel.onSaveClickEvent(it)
            }
        }
        binding.etMobile.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.etMobileHide.setText(binding.etMobile.text.toString())
                ccp.fullNumber = binding.etMobileHide.text.toString()
                viewModel.onPhoneEvent(binding.etMobile.text.toString().trim())
                if (binding.etMobile.text.toString().trim().isNotEmpty() && ccp.isValidFullNumber) {
                    viewModel.onPhoneError(true)
                } else {
                    viewModel.onPhoneError(false)
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
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
                    viewModel.onUserImageEvent(path)
                }

                override fun onImageCancel() {
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
            viewModel.editProfileResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true) {
                                requireActivity().showToast(response.data.message, true)
                                delay(MESSAGE_DISPLAY_TIME)
                                if (requireActivity() is NavGraphActivity) {
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

    }
}