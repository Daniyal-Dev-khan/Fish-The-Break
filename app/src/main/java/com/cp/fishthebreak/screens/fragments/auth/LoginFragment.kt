package com.cp.fishthebreak.screens.fragments.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.FragmentLoginBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.screens.activities.BaseActivityResult
import com.cp.fishthebreak.screens.activities.MainActivity
import com.cp.fishthebreak.utils.Constants.Companion.MESSAGE_DISPLAY_TIME
import com.cp.fishthebreak.utils.applyAsteriskPasswordTransformation
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.hideKeyboardFrom
import com.cp.fishthebreak.utils.hideShowPassword
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.auth.login.LoginViewModel
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.Date


@AndroidEntryPoint
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    val viewModel: LoginViewModel by viewModels()
    private var gso: GoogleSignInOptions? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private val activityLauncher: BaseActivityResult<Intent, ActivityResult> =
        BaseActivityResult.registerActivityForResult(this)

    //Fb login
    private var callbackManager: CallbackManager? = null
    private val EMAIL = "email"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.model = viewModel
        getFcmToken()
        initViewModelResponse()
        initGoogleLogin()
        initFbLogin()
        initListeners()
        return binding.root
    }

    private fun initFbLogin() {
        callbackManager = CallbackManager.Factory.create()
//        binding.fbLoginButton.setPermissions(EMAIL)
        // If you are using in a fragment, call loginButton.setFragment(this);
//        binding.fbLoginButton.setFragment(this)
        // Callback registration
//        callbackManager?.let {
//            binding.fbLoginButton.registerCallback(it, object : FacebookCallback<LoginResult> {
//                override fun onSuccess(result: LoginResult) {
//                    // App code
//                }
//
//                override fun onCancel() {
//                    // App code
//                }
//
//                override fun onError(error: FacebookException) {
//                    // App code
//                }
//            })
//        }
    }

    private fun getFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("SignUp FCM", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val fcmToken = task.result
            viewModel.onFcmTokenChangeEvent(fcmToken ?: "")
            // Log and toast
            Log.d("SignUp FCM", fcmToken ?: "failed")
        })
    }

    private fun initGoogleLogin() {
//        // Configure sign-in to request the user's ID, email address, and basic
//        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
//        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(resources.getString(R.string.google_sign_in_key))
//            .requestEmail()
//            .build()
//        // Build a GoogleSignInClient with the options specified by gso.
//        gso?.let { mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), it) }
//        // Check for existing Google Sign In account, if the user is already signed in
//        // the GoogleSignInAccount will be non-null.
//        // Check for existing Google Sign In account, if the user is already signed in
//        // the GoogleSignInAccount will be non-null.
//        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
//        if (account != null) {
//            showHideLoader(true)
//            mGoogleSignInClient?.signOut()?.addOnCompleteListener(requireActivity()) {
//                if (it.isSuccessful) {
//                    showHideLoader(false)
//                } else {
//                    showHideLoader(false)
//                }
//            }
//        }
    }

    private fun googleSignIn() {
//        val signInIntent = mGoogleSignInClient!!.signInIntent
//        activityLauncher.launch(
//            signInIntent,
//            object :
//                BaseActivityResult.OnActivityResult<ActivityResult> {
//                override fun onActivityResult(result: ActivityResult) {
//                    val task: Task<GoogleSignInAccount> =
//                        GoogleSignIn.getSignedInAccountFromIntent(result.data)
//                    handleGoogleSignInResult(task)
//                }
//            })
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
//        try {
//            val account = completedTask.getResult(ApiException::class.java)
//            Log.i("Google_Id", account?.id ?: "")
//            Log.i("Google_Id", account?.displayName ?: "")
//            Log.i("Google_Id", account?.familyName ?: "")
//            viewModel.socialLogin(
//                account?.id ?: "",
//                (account?.displayName ?: "").replace(account?.familyName ?: "", "").trim(),
//                account?.familyName ?: "",
//                (account?.displayName ?: "").replace(" ", "") + "_" + Date().time,
//                "google",
//                account?.email ?: "",
//                binding.layoutGoogleLogin
//            )
//            // Signed in successfully, show authenticated UI.
//
//        } catch (e: ApiException) {
//            // The ApiException status code indicates the detailed failure reason.
//            // Please refer to the GoogleSignInStatusCodes class reference for more information.
//            requireActivity().showToast(e.message?:"signInResult:failed code=${e.statusCode}",false)
//            Log.i("handleGoogleSignInResult", e.message ?: "handleGoogleSignInResult Error")
//            Log.w("Google_SignIn", "signInResult:failed code=" + e.statusCode)
//        }
    }

    private fun initListeners() {
        binding.apply {
            etPassword.applyAsteriskPasswordTransformation()
            ivPasswordHideShow.setOnClickListener {
                etPassword.hideShowPassword(
                    ivPasswordHideShow
                )
            }
            btnRegister.setOnClickListener {
                requireContext().hideKeyboardFrom(btnRegister)
                findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
            }
            forgetButton.setOnClickListener {
                requireContext().hideKeyboardFrom(forgetButton)
                findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToForgetPasswordFragment())
            }
            layoutGoogleLogin.setOnSingleClickListener {
                if (mGoogleSignInClient != null) {
                    googleSignIn()
                }
            }
            layoutFacebookLogin.setOnSingleClickListener {
                binding.fbLoginButton.performClick()
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
                                if (response.data.data?.token.isNullOrEmpty()) {
                                    findNavController().navigate(
                                        LoginFragmentDirections.actionLoginFragmentToOtpPasswordFragment(
                                            "login",
                                            binding.etEmail.text.toString()
                                        )
                                    )
                                    viewModel.resetResponse()
                                } else if (response.data.data?.purchase_status == 0 || response.data.data?.purchase_status == 2 || response.data.data?.purchase_status == 4) {
                                    findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToSubscriptionFragment())
                                    viewModel.resetResponse()
                                } else {
                                    delay(MESSAGE_DISPLAY_TIME)
                                    startActivity(
                                        Intent(
                                            requireContext(),
                                            MainActivity::class.java
                                        )
                                    )
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

    }
}