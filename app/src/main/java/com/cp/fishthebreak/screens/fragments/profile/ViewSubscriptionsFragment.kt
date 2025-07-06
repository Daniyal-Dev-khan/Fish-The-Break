package com.cp.fishthebreak.screens.fragments.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryPurchasesAsync
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.profile.ActiveSubscriptionAdapter
import com.cp.fishthebreak.databinding.FragmentViewSubscriptionsBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.models.profile.SubscribedModel
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.profile.subscription.SubscriptionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class ViewSubscriptionsFragment : Fragment() {
    private val TAG = ViewSubscriptionsFragment::class.simpleName
    private lateinit var binding: FragmentViewSubscriptionsBinding
    private val purchaseList = ArrayList<SubscribedModel>()
    val viewModel: SubscriptionViewModel by viewModels()
    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            // To be implemented in a later section.
//            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
//                for (purchase in purchases) {
//                    lifecycleScope.launch {
//                        handlePurchase(purchase)
//                    }
//                }
//            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
//                // Handle an error caused by a user cancelling the purchase flow.
//                Log.i("PurchasesUpdatedListener", "cancelled by user.")
//            } else {
//                // Handle any other error codes.
//                Log.i("PurchasesUpdatedListener", billingResult.debugMessage)
//            }
        }
    private var billingClient: BillingClient? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentViewSubscriptionsBinding.inflate(layoutInflater, container, false)
        initViewModelResponse()
        initAdapter()
        startInAppConnection()
        initListeners()
        return binding.root
    }

    private fun initAdapter(){
        binding.rv.adapter = ActiveSubscriptionAdapter(purchaseList,object: ActiveSubscriptionAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {

            }

        })
    }

    private fun initListeners() {
        binding.backButton.setOnClickListener {
            if (requireActivity() is NavGraphActivity) {
                (requireActivity() as NavGraphActivity).onBack()
            }
        }
        binding.buttonManageSubscription.setOnClickListener {
            findNavController().navigate(ViewSubscriptionsFragmentDirections.actionViewSubscriptionsFragmentToSubscriptionFragment())
        }
        binding.buttonCancelSubscription.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/account/subscriptions")
            )
            startActivity(browserIntent)
        }
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

    private suspend fun getQueryPurchasesAsync() {

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)

        // uses queryPurchasesAsync Kotlin extension function
        val purchasesResult = withContext(Dispatchers.IO) {
            billingClient?.queryPurchasesAsync(params.build())
        }

        // check purchasesResult.billingResult
        // process returned purchasesResult.purchasesList, e.g. display the plans user owns
        when(purchasesResult?.billingResult?.responseCode){
            BillingClient.BillingResponseCode.OK ->{
                if(purchasesResult.purchasesList.isNotEmpty()){
                    purchaseList.clear()
                    purchasesResult.purchasesList.forEach { item->
                        purchaseList.add(SubscribedModel(serverData = null, purchaseData = item))
                    }
//                    if(purchaseList.isNotEmpty()) {
//                        viewModel.getSubscription()
//                    }
                    viewModel.getSubscription()
                }else{
                    viewModel.getSubscription()
                }
            }
            else -> {
                Log.e(TAG, purchasesResult?.billingResult?.debugMessage?:"")
            }
        }

    }

    private fun initViewModelResponse() {
        lifecycleScope.launch {
            viewModel.getSubscriptionResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true && response.data.statusCode == 200) {
                                if(response.data.data == null){
                                    binding.buttonManageSubscription.viewGone()
                                    binding.buttonCancelSubscription.viewGone()
                                    binding.noDataLayout.viewVisible()
                                }else{
                                    if(purchaseList.isNotEmpty()) {
                                        binding.buttonManageSubscription.viewVisible()
                                        if (response.data.data?.product_id == "free_trial"){
                                            binding.buttonCancelSubscription.viewGone()
                                        }else{
                                            binding.buttonCancelSubscription.viewVisible()
                                        }
                                        binding.noDataLayout.viewGone()
                                        purchaseList.first().serverData = response.data.data
                                        binding.rv.adapter?.notifyDataSetChanged()
                                    }else{
                                        binding.buttonManageSubscription.viewVisible()
                                        binding.noDataLayout.viewGone()
                                        purchaseList.add(SubscribedModel(response.data.data,null))
                                        binding.rv.adapter?.notifyDataSetChanged()
                                        if (response.data.data?.product_id == "free_trial"){
                                            binding.buttonCancelSubscription.viewGone()
                                        }else{
                                            binding.buttonCancelSubscription.viewVisible()
                                        }
//                                        binding.buttonManageSubscription.viewGone()
//                                        binding.buttonCancelSubscription.viewGone()
//                                        binding.noDataLayout.viewVisible()
                                    }
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