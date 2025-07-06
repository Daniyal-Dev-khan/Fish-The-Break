package com.cp.fishthebreak.screens.fragments.profile

import android.content.Intent
import android.os.Build
import android.os.Bundle
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
import com.cp.fishthebreak.databinding.FragmentProfileBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.screens.activities.AuthActivity
import com.cp.fishthebreak.screens.activities.BaseActivityResult
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.cp.fishthebreak.screens.bottom_sheets.ConfirmBottomSheet
import com.cp.fishthebreak.screens.bottom_sheets.LogoutBottomSheet
import com.cp.fishthebreak.utils.Constants
import com.cp.fishthebreak.utils.SelectImageListener
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.cp.fishthebreak.utils.StartDestination
import com.cp.fishthebreak.utils.createUrlANdInvite
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.getPathFromUri
import com.cp.fishthebreak.utils.isPhotoPickerAvailable
import com.cp.fishthebreak.utils.loadImage
import com.cp.fishthebreak.utils.selectFromImagePicker
import com.cp.fishthebreak.utils.selectImage
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.profile.ProfileViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    val viewModel: ProfileViewModel by viewModels()

    @Inject
    lateinit var sharePreferenceHelper: SharePreferenceHelper
    private val activityLauncher: BaseActivityResult<Intent, ActivityResult> =
        BaseActivityResult.registerActivityForResult(this)

    private lateinit var mGoogleSignInClient: GoogleSignInClient

//    val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted->
//        if(isGranted){
//
//        }
//    }

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
                viewModel.updateProfilePic(image)
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.model = viewModel
        initListeners()
        initViewModelResponse()
        if (sharePreferenceHelper.getUser()?.social_login_id != null) {
            initializeGoogleLogin()
        }
        return binding.root
    }

    private fun initializeGoogleLogin() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(resources.getString(R.string.google_sign_in_key)).requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun signOutGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity()) {
            if (it.isSuccessful) {
                val sIntent = Intent(requireContext(), AuthActivity::class.java)
                sIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                requireContext().startActivity(sIntent)
            }
        }
    }

    private fun initListeners() {
        binding.ivAdd.setOnSingleClickListener {
//            requestCameraPermission.launch(CAMERA)
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
                            viewModel.updateProfilePic(path)
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
        binding.ivEdit.setOnSingleClickListener {
            val sIntent = Intent(requireContext(), NavGraphActivity::class.java)
            sIntent.putExtra(Constants.START_DESTINATION, StartDestination.EditPrifile())
            startActivity(sIntent)
        }
        binding.tvChangePassword.setOnSingleClickListener {
            val sIntent = Intent(requireContext(), NavGraphActivity::class.java)
            sIntent.putExtra(Constants.START_DESTINATION, StartDestination.UpdatePassword())
            startActivity(sIntent)
        }
        binding.tvChangeEmail.setOnSingleClickListener {
            val sIntent = Intent(requireContext(), NavGraphActivity::class.java)
            sIntent.putExtra(Constants.START_DESTINATION, StartDestination.UpdateEmail())
            startActivity(sIntent)
        }
        binding.tvPreferences.setOnSingleClickListener {
            val sIntent = Intent(requireContext(), NavGraphActivity::class.java)
            sIntent.putExtra(Constants.START_DESTINATION, StartDestination.UpdatetvPreferences())
            startActivity(sIntent)
        }
        binding.tvSaveMapOffline.setOnSingleClickListener {
            val sIntent = Intent(requireContext(), NavGraphActivity::class.java)
            sIntent.putExtra(Constants.START_DESTINATION, StartDestination.SaveOfflineMap())
            startActivity(sIntent)
        }
        binding.tvResources.setOnSingleClickListener {
            val sIntent = Intent(requireContext(), NavGraphActivity::class.java)
            sIntent.putExtra(Constants.START_DESTINATION, StartDestination.ResoursesData())
            startActivity(sIntent)
        }
        binding.tvVessels.setOnSingleClickListener {
            val sIntent = Intent(requireContext(), NavGraphActivity::class.java)
            sIntent.putExtra(Constants.START_DESTINATION, StartDestination.VesselsData())
            startActivity(sIntent)
        }
        binding.tvFaqsFeedback.setOnSingleClickListener {
            val sIntent = Intent(requireContext(), NavGraphActivity::class.java)
            sIntent.putExtra(Constants.START_DESTINATION, StartDestination.Subscription())
            startActivity(sIntent)
        }
        binding.tvManageSubscriptions.setOnSingleClickListener {
            val sIntent = Intent(requireContext(), NavGraphActivity::class.java)
            sIntent.putExtra(Constants.START_DESTINATION, StartDestination.ActiveSubscription())
            startActivity(sIntent)
        }

        binding.layoutLogout.setOnSingleClickListener {
            val dialog = LogoutBottomSheet(object : LogoutBottomSheet.LogoutClickListener {
                override fun onLogoutClick() {
                    viewModel.logout()
                }

                override fun onCancelClick() {

                }

            })
            dialog.show(childFragmentManager, "LogoutBottomSheet")
        }
        binding.tvDeleteAccount.setOnSingleClickListener {
            val dialog = ConfirmBottomSheet(
                resources.getString(R.string.delete_account),
                resources.getString(R.string.delete_account_alert),
                object : ConfirmBottomSheet.OnItemClickListener {
                    override fun onYesClick() {
                        viewModel.deleteAccount()
                    }

                    override fun onCancelClick() {

                    }
                })
            dialog.show(childFragmentManager, "LogoutBottomSheet")
        }
        binding.tvInviteFriends.setOnSingleClickListener {
            requireContext().createUrlANdInvite()
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
                    viewModel.updateProfilePic(path)
                }

                override fun onImageCancel() {
                }

            })
    }

    private fun initViewModelResponse() {
        lifecycleScope.launch {
            viewModel.profileResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
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
                            viewModel.loadUserFromLocalStorage()
                        }

                        is NetworkResult.NoCallYet -> {
                        }
                    }
                }
        }
        lifecycleScope.launch {
            viewModel.logoutResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            if (response.data?.status == true) {
                                requireActivity().showToast(
                                    response.data.message, true
                                )

                                delay(1500)
                                if (::mGoogleSignInClient.isInitialized) {
                                    signOutGoogle()
                                } else {
                                    val sIntent = Intent(requireContext(), AuthActivity::class.java)
                                    sIntent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    requireContext().startActivity(sIntent)
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
                            requireActivity().showToast(
                                response.message.getOnErrorMessage(),
                                false
                            )
                            viewModel.resetResponse()
                        }

                        is NetworkResult.Loading -> {
                        }

                        is NetworkResult.NoInternet -> {
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
            viewModel.deleteResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            if (response.data?.status == true) {
                                requireActivity().showToast(
                                    response.data.message, true
                                )

                                delay(1500)
                                if (::mGoogleSignInClient.isInitialized) {
                                    signOutGoogle()
                                } else {
                                    val sIntent = Intent(requireContext(), AuthActivity::class.java)
                                    sIntent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    requireContext().startActivity(sIntent)
                                }
                            } else {
                                showHideLoader(false)
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
            viewModel.loadingResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    showHideLoader(response)
                }
        }

    }

    override fun onResume() {
        viewModel.loadUserFromLocalStorage()
        super.onResume()
    }

    private fun showHideLoader(visibility: Boolean) {
        if (visibility) {
            if (!binding.pullToRefresh.isRefreshing) {
                binding.loaderLayout.viewVisible()
            }
        } else {
            binding.loaderLayout.viewGone()
            binding.pullToRefresh.isRefreshing = false
        }
    }

}