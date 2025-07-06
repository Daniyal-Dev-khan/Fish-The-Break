package com.cp.fishthebreak.viewModels.profile.subscription

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.Repository
import com.cp.fishthebreak.models.auth.RegisterModel
import com.cp.fishthebreak.models.profile.AddSubscriptionModel
import com.cp.fishthebreak.models.profile.ChangeEmailModel
import com.cp.fishthebreak.models.profile.GetSubscribedPackageModel
import com.cp.fishthebreak.models.profile.StartTrialModel
import com.cp.fishthebreak.models.profile.SubscribedData
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.cp.fishthebreak.utils.getMyDeviceId
import com.cp.fishthebreak.utils.getProductTimePeriod
import com.cp.fishthebreak.utils.hideKeyboardFrom
import com.cp.fishthebreak.utils.isNetworkAvailable
import com.cp.fishthebreak.utils.rules.Validator
import com.cp.fishthebreak.utils.toDate
import com.cp.fishthebreak.utils.toFormat
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor
    (
    private val repository: Repository,
    private val preference: SharePreferenceHelper,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext) {


    private val _addSubscriptionResponse: MutableStateFlow<NetworkResult<AddSubscriptionModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val addSubscriptionResponse: StateFlow<NetworkResult<AddSubscriptionModel>> =
        _addSubscriptionResponse.asStateFlow()

    private val _trialResponse: MutableStateFlow<NetworkResult<StartTrialModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val trialResponse: StateFlow<NetworkResult<StartTrialModel>> =
        _trialResponse.asStateFlow()

    private val _getSubscriptionResponse: MutableStateFlow<NetworkResult<GetSubscribedPackageModel>> =
        MutableStateFlow(NetworkResult.NoCallYet())
    val getSubscriptionResponse: StateFlow<NetworkResult<GetSubscribedPackageModel>> =
        _getSubscriptionResponse.asStateFlow()

    fun addSubscription(
        purchaseData: String,
        purchaseDate: Long,
        productId: String,
        productName: String,
        productPrice: String
    ) = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _addSubscriptionResponse.value = NetworkResult.Loading()
            val jsonObject = JsonObject()
            jsonObject.addProperty("data", purchaseData)
            jsonObject.addProperty("platform", "0")
            jsonObject.addProperty(
                "expiry_date",
                purchaseDate.toDate(productId).toFormat("yyyy-MM-dd")
            )
            jsonObject.addProperty("product_name", productName)
            jsonObject.addProperty("product_price", productPrice)
            jsonObject.addProperty("package_month", productId.getProductTimePeriod())
            repository.subscribePackage(jsonObject).collect { values ->
                if (values.data?.status == true && values.data.statusCode == 200 && values.data.data != null) {
                    preference.saveUser(values.data.data)
                }
                _addSubscriptionResponse.value = values
            }
        } else {
            _addSubscriptionResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )
        }
    }

    fun startFreeTrial() = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _trialResponse.value = NetworkResult.Loading()
            repository.startFreeTrial().collect { values ->
                if (values.data?.status == true && values.data.statusCode == 200 && values.data.data != null) {
                    preference.saveUser(values.data.data)
                }
                _trialResponse.value = values
            }
        } else {
            _trialResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )
        }
    }

    fun getSubscription() = viewModelScope.launch {
        if (applicationContext.isNetworkAvailable()) {
            _getSubscriptionResponse.value = NetworkResult.Loading()
            repository.getSubscribedPackage().collect { values ->
                if (values.data?.data == null) {
                    val user = preference.getUser()
                    val trialExpireDate = user?.trial_end_date?.toDate("yyyy-MM-dd")
                    val currentDate = Date().toFormat("yyyy-MM-dd").toDate("yyyy-MM-dd")
                    if (trialExpireDate != null && trialExpireDate >= currentDate) {
                        values.data?.data = SubscribedData(
                            "",
                            user.trial_end_date,
                            -1,
                            "",
                            "",
                            0,
                            "free_trial",
                            "Free Trial",
                            "",
                            "",
                            "",
                            "",
                            preference.getUser()?.id?:-1
                        )
                    }
                }
                _getSubscriptionResponse.value = values
            }
        } else {
            _getSubscriptionResponse.value = NetworkResult.NoInternet(
                applicationContext.resources.getString(
                    R.string.no_internet
                )
            )
        }
    }


    fun resetResponse() {
        _addSubscriptionResponse.value = NetworkResult.NoCallYet()
        _trialResponse.value = NetworkResult.NoCallYet()
    }
}