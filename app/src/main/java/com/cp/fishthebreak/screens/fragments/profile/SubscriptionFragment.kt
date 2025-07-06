package com.cp.fishthebreak.screens.fragments.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.android.billingclient.api.acknowledgePurchase
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.profile.SubscriptionAdapter
import com.cp.fishthebreak.databinding.FragmentSubscriptionBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.models.profile.InAppSubscriptionModel
import com.cp.fishthebreak.screens.activities.AuthActivity
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.cp.fishthebreak.utils.Constants
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.getProductIdFromPurchase
import com.cp.fishthebreak.utils.getProductNameFromProductId
import com.cp.fishthebreak.utils.getRemainingTrialDays
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.toDate
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.auth.SplashViewModel
import com.cp.fishthebreak.viewModels.profile.subscription.SubscriptionViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class SubscriptionFragment : Fragment() {
    private lateinit var binding: FragmentSubscriptionBinding
    private val TAG = SubscriptionFragment::class.simpleName
    private val subscriptionProductList = ArrayList<InAppSubscriptionModel>()
    private val subscriptionPurchasedList = ArrayList<Purchase>()
    private var selectedProduct: ProductDetails? = null
    val viewModel: SubscriptionViewModel by viewModels()
    val logoutViewModel: SplashViewModel by viewModels()
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private var currentPurchase: Purchase? = null

    @Inject
    lateinit var sharePreferenceHelper: SharePreferenceHelper
    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            // To be implemented in a later section.
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                Log.i("PurchasesUpdatedListener", purchases.size.toString())
                for (purchase in purchases) {
                    lifecycleScope.launch {
                        showHideLoader(true)
                        handlePurchase(purchase)
                    }
                }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
                Log.i("PurchasesUpdatedListener", "cancelled by user.")
            } else {
                // Handle any other error codes.
                Log.i("PurchasesUpdatedListener", billingResult.debugMessage)
                requireActivity().showToast(billingResult.debugMessage, false)
            }
        }

    private var billingClient: BillingClient? = null

    private fun initializeGoogleLogin() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(resources.getString(R.string.google_sign_in_key)).requestEmail()
            .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSubscriptionBinding.inflate(layoutInflater, container, false)
        initViews()
        initViewModelResponse()
        initAdapter()
        startInAppConnection()
        initListeners()
        if (sharePreferenceHelper.getUser()?.social_login_id != null) {
            initializeGoogleLogin()
        }
        return binding.root
    }

    private fun initViews() {
        val user = sharePreferenceHelper.getUser()
        if (user != null && user.is_subscribed == 0 && user.trial_start_date == null && user.purchase_status == 0) {
            binding.tvStartFreeTrial.viewVisible()
            binding.btnStartFreeTrial.viewVisible()
        } else {
            binding.tvStartFreeTrial.viewGone()
            binding.btnStartFreeTrial.viewGone()
        }
        if (requireActivity() is AuthActivity) {
            binding.backButton.text = resources.getString(R.string.cancel)
            ( binding.backButton as MaterialButton).icon  = null
        }else{
            binding.backButton.text = ""
            ( binding.backButton as MaterialButton).icon  = ContextCompat.getDrawable(requireContext(),R.drawable.ic_back_primary)
        }
    }

//    override fun onResume() {
//        super.onResume()
//        lifecycleScope.launch {
//            getQueryPurchasesAsync()
//        }
//    }

    private fun initListeners() {
        binding.btnStartFreeTrial.setOnSingleClickListener {
            viewModel.startFreeTrial()
        }
        binding.buttonRestoreSubscribe.setOnSingleClickListener {
            lifecycleScope.launch {
                currentPurchase?.let {
                    showHideLoader(true)
                    acknowledgePurchaseSub(it)
                }
            }
        }
        binding.backButton.setOnClickListener {
            if (requireActivity() is NavGraphActivity) {
                (requireActivity() as NavGraphActivity).onBack()
            }
            else if (requireActivity() is AuthActivity) {
                logoutViewModel.logout()
            }
        }
        binding.buttonSubscribe.setOnSingleClickListener {
            val selected = subscriptionProductList.filter { item -> item.isSelected }
            if (selected.isNotEmpty()) {
                selectedProduct = selected.first().product
                if (subscriptionPurchasedList.isEmpty()) {
                    launchBillingFlow(selected.first().product)
                } else {
                    upgradePlan(selected.first().product)
                }
            }
        }
    }

    private fun initAdapter() {
        binding.rv.adapter = SubscriptionAdapter(
            subscriptionProductList,
            object : SubscriptionAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    subscriptionProductList.forEach { item ->
                        item.isSelected = false
                    }
                    subscriptionProductList[position].isSelected = true
                    binding.rv.adapter?.notifyDataSetChanged()
                }
            })
    }

    override fun onDestroy() {
        billingClient?.endConnection()
        super.onDestroy()
    }

    private fun startInAppConnection() {
        billingClient = BillingClient.newBuilder(requireContext())
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    lifecycleScope.launch {
                        getQueryPurchasesAsync()
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    private suspend fun processPurchases() {
        val productList = ArrayList<QueryProductDetailsParams.Product>()
        productList.add(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("one_month_sub")
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        productList.add(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("one_year_sub")
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)

        // leverage queryProductDetails Kotlin extension function
        val productDetailsResult = withContext(Dispatchers.IO) {
            billingClient?.queryProductDetails(params.build())
        }

        // Process the result.
        when (productDetailsResult?.billingResult?.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (productDetailsResult.productDetailsList.isNullOrEmpty()) {
                    showHideLoader(false)
                    Log.e(
                        TAG,
                        "onProductDetailsResponse: " +
                                "Found null or empty ProductDetails. " +
                                "Check to see if the Products you requested are correctly " +
                                "published in the Google Play Console."
                    )
                } else {
                    showHideLoader(false)
                    subscriptionProductList.clear()
                    productDetailsResult.productDetailsList?.forEach { item ->
                        subscriptionProductList.add(InAppSubscriptionModel(item, item.productId == subscriptionPurchasedList.getProductIdFromPurchase()))
                    }
                    if (subscriptionProductList.isNotEmpty()) {
                        
                        binding.buttonSubscribe.viewVisible()
                        binding.noDataLayout.viewGone()
                    } else {
                        binding.noDataLayout.viewVisible()
                        binding.buttonSubscribe.viewGone()
                    }
                    binding.rv.adapter?.notifyDataSetChanged()
                }
            }

            else -> {
                showHideLoader(false)
                Log.i(
                    TAG,
                    "onProductDetailsResponse: ${productDetailsResult?.billingResult?.responseCode} ${productDetailsResult?.billingResult?.debugMessage}"
                )
            }
        }
    }

    private fun launchBillingFlow(product: ProductDetails) {
        if (subscriptionProductList.isEmpty()) {
            return
        }
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                .setProductDetails(product)
                // For One-time product, "setOfferToken" method shouldn't be called.
                // For subscriptions, to get an offer token, call ProductDetails.subscriptionOfferDetails()
                // for a list of offers that are available to the user
                .setOfferToken(
                    product.subscriptionOfferDetails?.first()?.offerToken
                        ?: ""
                )
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        // Launch the billing flow
        val billingResult = billingClient?.launchBillingFlow(requireActivity(), billingFlowParams)
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        // Purchase retrieved from BillingClient#queryPurchasesAsync or your PurchasesUpdatedListener.

        // Verify the purchase.
        // Ensure entitlement was not already granted for this purchaseToken.
        // Grant entitlement to the user.

//        val consumeParams =
//            ConsumeParams.newBuilder()
//                .setPurchaseToken(purchase.getPurchaseToken())
//                .build()
//        val consumeResult = withContext(Dispatchers.IO) {
//            billingClient?.consumePurchase(consumeParams)
//        }
//        when(consumeResult?.billingResult?.responseCode){
//            BillingClient.BillingResponseCode.OK ->{
//                Log.i(TAG, "consumePurchase BillingClient.BillingResponseCode.OK")
//                aknowledgePurchaseSub(purchase)
//            }
//            else -> {
//                Log.e(TAG, consumeResult?.billingResult?.debugMessage?:"")
//            }
//        }
        currentPurchase = purchase
        acknowledgePurchaseSub(purchase)
    }

    private suspend fun acknowledgePurchaseSub(purchase: Purchase) {
        Log.i("PurchaseState", purchase.purchaseState.toString())
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                val ackPurchaseResult = withContext(Dispatchers.IO) {
                    billingClient?.acknowledgePurchase(acknowledgePurchaseParams.build())
                }
                when (ackPurchaseResult?.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        Log.i(TAG, "acknowledgePurchaseSub, BillingClient.BillingResponseCode.OK")
                        if (selectedProduct != null) {
                            viewModel.addSubscription(
                                purchaseData = purchase.originalJson,
                                purchaseDate = purchase.purchaseTime,
                                productId = selectedProduct?.productId ?: "",
                                productName = selectedProduct?.name ?: "",
                                productPrice = selectedProduct?.subscriptionOfferDetails?.first()?.pricingPhases?.pricingPhaseList?.first()?.formattedPrice
                                    ?: ""
                            )
                        }else{
                            currentPurchase?.let {cPurchase->
                                val selected = subscriptionProductList.filter { item ->
                                    item.product.productId == JSONObject(cPurchase.originalJson).get(
                                        "productId"
                                    ).toString()
                                }
                                if (selected.isNotEmpty()) {
                                    selectedProduct = selected.first().product
                                    viewModel.addSubscription(
                                        purchaseData = purchase.originalJson,
                                        purchaseDate = purchase.purchaseTime,
                                        productId = selectedProduct?.productId ?: "",
                                        productName = selectedProduct?.name ?: "",
                                        productPrice = selectedProduct?.subscriptionOfferDetails?.first()?.pricingPhases?.pricingPhaseList?.first()?.formattedPrice
                                            ?: ""
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        showHideLoader(false)
                        binding.buttonSubscribe.viewGone()
                        binding.tvStartFreeTrial.viewGone()
                        binding.btnStartFreeTrial.viewGone()
                        binding.buttonRestoreSubscribe.viewVisible()
                        Log.e(TAG, ackPurchaseResult?.debugMessage ?: "")
                        requireActivity().showToast(
                            ackPurchaseResult?.debugMessage ?: "Acknowledge Purchase failed", false
                        )
                    }
                }
            } else {
                if (selectedProduct != null) {
                    viewModel.addSubscription(
                        purchaseData = purchase.originalJson,
                        purchaseDate = purchase.purchaseTime,
                        productId = selectedProduct?.productId ?: "",
                        productName = selectedProduct?.name ?: "",
                        productPrice = selectedProduct?.subscriptionOfferDetails?.first()?.pricingPhases?.pricingPhaseList?.first()?.formattedPrice
                            ?: ""
                    )
                }else{
                    currentPurchase?.let {cPurchase->
                        val selected = subscriptionProductList.filter { item ->
                            item.product.productId == JSONObject(cPurchase.originalJson).get(
                                "productId"
                            ).toString()
                        }
                        if (selected.isNotEmpty()) {
                            selectedProduct = selected.first().product
                            viewModel.addSubscription(
                                purchaseData = purchase.originalJson,
                                purchaseDate = purchase.purchaseTime,
                                productId = selectedProduct?.productId ?: "",
                                productName = selectedProduct?.name ?: "",
                                productPrice = selectedProduct?.subscriptionOfferDetails?.first()?.pricingPhases?.pricingPhaseList?.first()?.formattedPrice
                                    ?: ""
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun getQueryPurchasesAsync() {
        showHideLoader(true)
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)

        // uses queryPurchasesAsync Kotlin extension function
        val purchasesResult = withContext(Dispatchers.IO) {
            billingClient?.queryPurchasesAsync(params.build())
        }

        // check purchasesResult.billingResult
        // process returned purchasesResult.purchasesList, e.g. display the plans user owns
        when (purchasesResult?.billingResult?.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchasesResult.purchasesList.isNotEmpty()) {
                    subscriptionPurchasedList.clear()
                    subscriptionPurchasedList.addAll(purchasesResult.purchasesList)
                    val user = sharePreferenceHelper.getUser()
                    if(user?.purchase_status == 0 || user?.purchase_status == 2 || user?.purchase_status == 4){//restore subscription
                        currentPurchase = purchasesResult.purchasesList.first()
                        binding.buttonSubscribe.viewGone()
                        binding.tvStartFreeTrial.viewGone()
                        binding.btnStartFreeTrial.viewGone()
                        binding.buttonRestoreSubscribe.viewVisible()
                    }else if(!purchasesResult.purchasesList.first().isAcknowledged){//restore subscription
                        currentPurchase = purchasesResult.purchasesList.first()
                        binding.buttonSubscribe.viewGone()
                        binding.tvStartFreeTrial.viewGone()
                        binding.btnStartFreeTrial.viewGone()
                        binding.buttonRestoreSubscribe.viewVisible()
                    }
                }
                processPurchases()
            }

            else -> {
                showHideLoader(false)
                Log.e(TAG, purchasesResult?.billingResult?.debugMessage ?: "")
            }
        }

    }

    private fun upgradePlan(product: ProductDetails) {
        if (subscriptionPurchasedList.isEmpty()) {
            return
        }
        val activePurchase = subscriptionPurchasedList.first()

        if (JSONObject(activePurchase.originalJson).get("productId")
                .toString() == product.productId
        ) {
            requireActivity().showToast(resources.getString(R.string.already_subscribed), false)
            return
        }

        val billingParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(product)
                    .setOfferToken(product.subscriptionOfferDetails?.first()?.offerToken ?: "")
                    .build()
            )
        ).setSubscriptionUpdateParams(
            BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                .setOldPurchaseToken(activePurchase.purchaseToken)
                .setSubscriptionReplacementMode(
                    BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.CHARGE_FULL_PRICE
                )
                .build()
        ).build()

        billingClient?.launchBillingFlow(
            requireActivity(),
            billingParams
        )
    }

    private fun signOutGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity()) {
            showHideLoader(false)
            if (it.isSuccessful) {
                if (requireActivity() is AuthActivity) {
                    findNavController().navigate(SubscriptionFragmentDirections.actionSubscriptionFragmentToLoginFragment())
                }
            }
        }
    }

    private fun initViewModelResponse() {
        lifecycleScope.launch {
            viewModel.addSubscriptionResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true && response.data.data != null) {
                                requireActivity().showToast(response.data.message, true)
                                binding.buttonSubscribe.viewVisible()
                                val user = sharePreferenceHelper.getUser()
                                if (user != null && user.is_subscribed == 0 && user.trial_start_date == null && user.purchase_status == 0) {
                                    binding.tvStartFreeTrial.viewVisible()
                                    binding.btnStartFreeTrial.viewVisible()
                                }
                                binding.buttonRestoreSubscribe.viewGone()
                                if(requireActivity() is AuthActivity) {
                                    findNavController().navigate(
                                        SubscriptionFragmentDirections.actionOtpPasswordFragmentToAccountCreatedFragment(
                                            selectedProduct?.productId?.getProductNameFromProductId()
                                                .toString(),
                                            Constants.SUBSCRIPTION
                                        )
                                    )
                                }else if(requireActivity() is NavGraphActivity){
                                    (requireActivity() as NavGraphActivity).onBack()
                                }
                                viewModel.resetResponse()
                            } else {
                                binding.buttonSubscribe.viewGone()
                                binding.tvStartFreeTrial.viewGone()
                                binding.btnStartFreeTrial.viewGone()
                                binding.buttonRestoreSubscribe.viewVisible()
                                requireActivity().showToast(
                                    response.data?.message ?: resources.getString(
                                        R.string.something_went_wrong
                                    ), false
                                )
                                viewModel.resetResponse()
                            }
                        }

                        is NetworkResult.Error -> {
                            binding.buttonSubscribe.viewGone()
                            binding.tvStartFreeTrial.viewGone()
                            binding.btnStartFreeTrial.viewGone()
                            binding.buttonRestoreSubscribe.viewVisible()
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
                            binding.buttonSubscribe.viewGone()
                            binding.tvStartFreeTrial.viewGone()
                            binding.btnStartFreeTrial.viewGone()
                            binding.buttonRestoreSubscribe.viewVisible()
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
            viewModel.trialResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true && response.data.data != null) {
                                requireActivity().showToast(response.data.message, true)
                                findNavController().navigate(
                                    SubscriptionFragmentDirections.actionOtpPasswordFragmentToAccountCreatedFragment(
                                        response.data.data.trial_end_date?.toDate("yyyy-MM-dd")
                                            ?.getRemainingTrialDays().toString(),
                                        Constants.FREE_TRAIL
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
            logoutViewModel.logoutResponse
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
                                    showHideLoader(false)
                                    if (requireActivity() is AuthActivity) {
                                        findNavController().navigate(SubscriptionFragmentDirections.actionSubscriptionFragmentToLoginFragment())
                                    }
                                }
                            } else {
                                showHideLoader(false)
                                requireActivity().showToast(
                                    response.data?.message ?: resources.getString(
                                        R.string.something_went_wrong
                                    ), false
                                )
                                logoutViewModel.resetResponse()
                            }
                        }

                        is NetworkResult.Error -> {
                            showHideLoader(false)
                            requireActivity().showToast(
                                response.message.getOnErrorMessage(),
                                false
                            )
                            logoutViewModel.resetResponse()
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
                            logoutViewModel.resetResponse()
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