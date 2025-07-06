package com.cp.fishthebreak.screens.activities

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.ActivityMySplashBinding
import com.cp.fishthebreak.di.MyApplication
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.di.local.AppDatabase
import com.cp.fishthebreak.models.DeepLinkData
import com.cp.fishthebreak.models.group.ChatListData
import com.cp.fishthebreak.models.map.LayersStatusModel
import com.cp.fishthebreak.notifications.NotificationModel
import com.cp.fishthebreak.utils.Constants
import com.cp.fishthebreak.utils.Constants.Companion.DEFAULT_LAYER_OPACITY
import com.cp.fishthebreak.utils.NavigationDirections
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.cp.fishthebreak.utils.StartDestination
import com.cp.fishthebreak.utils.decodeFromBase64
import com.cp.fishthebreak.utils.deleteAppCache
import com.cp.fishthebreak.utils.deleteAppDownloadCache
import com.cp.fishthebreak.utils.isNetworkAvailable
import com.cp.fishthebreak.utils.isSubscriptionExpired
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.viewModels.auth.SplashViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject

@AndroidEntryPoint
class MySplashActivity : BaseActivity() {
    private lateinit var binding: ActivityMySplashBinding
    private var notificationsModel: NotificationModel? = null
    private var deepLinkModel: DeepLinkData? = null
    val viewModel: SplashViewModel by viewModels()

    @Inject
    lateinit var sharePreferenceHelper: SharePreferenceHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullScreenWithStatusBarWhiteIcon()
//        setStatusBarTransparent(binding.root)
        //printHashKey(applicationContext)
        val deepLinkData = intent?.data
        if (deepLinkData != null) {
            try {
                val allFields = deepLinkData.toString().split("/")
                val id = allFields[allFields.lastIndex].decodeFromBase64()
                val type = allFields[allFields.lastIndex - 1]
                deepLinkModel = DeepLinkData(id.toInt(), type)
                Log.e("deepLinkData data", "$id, $type")
            } catch (ex: Exception) {

            }
        }
        val extras = intent?.extras
        if (intent != null && intent.hasExtra("notificationData")) {
            try {
                notificationsModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra("notificationData", NotificationModel::class.java)
                } else {
                    intent.getParcelableExtra("notificationData")
                }
            } catch (ex: Exception) {

            }
        } else {
            if (extras != null) {
                try {
                    Log.i("hasExtras_", "true")
                    notificationsModel = NotificationModel()
                    val json = extras.get("my_data").toString()
                    val notificationType = extras.get("notification_type").toString()
                    Log.i("hasExtras_ json", json)
                    val body = Gson().fromJson(
                        json,
                        ChatListData::class.java
                    )
                    if(notificationType == "chat") {
                        notificationsModel?.navigation = NavigationDirections.OpenGroup(body)
                    }else if (notificationType == "invitation"){
                        notificationsModel?.navigation = NavigationDirections.OpenGroupList()
                    }
                } catch (ex: Exception) {
                    Log.e("noti_model_exp", ex.message.toString())
                }
            } else {
                Log.i("hasExtras_", "false")
            }
        }
//        val selectedLayers = sharePreferenceHelper.getSavedLayers()
//        selectedLayers?.data?.forEach { layers->
//            layers.opacity = DEFAULT_LAYER_OPACITY.toInt()
//        }
//        lifecycleScope.launch {
//            selectedLayers?.data?.let {data->
//                sharePreferenceHelper.saveLayers(LayersStatusModel(data))
//            }
//        }
        lifecycleScope.launch {
            if (sharePreferenceHelper.isUserLoggedIn()) {
                delay(500)
                initViewModelResponse()
                /*if(sharePreferenceHelper.getUser()?.is_subscribed == 0 && sharePreferenceHelper.getUser()?.trial_start_date.isNullOrEmpty()){
                    sharePreferenceHelper.isFirstLaunch(false)
                    val sIntent = Intent(applicationContext, AuthActivity::class.java)
                    sIntent.putExtra(Constants.START_DESTINATION, StartDestination.Subscription())
                    startActivity(sIntent)
                    finish()
                }else {
                    sharePreferenceHelper.isFirstLaunch(false)
                    val sIntent = Intent(applicationContext, MainActivity::class.java)
                    if (notificationsModel != null) {
                        sIntent.putExtra("notificationData", notificationsModel)
                    }
                    if (deepLinkModel != null) {
                        sIntent.putExtra("deepLinkData", deepLinkModel)
                    }
                    startActivity(sIntent)
                    finish()
                }*/
            } else if (!sharePreferenceHelper.getIsFirstLaunch()) {
                delay(2000)
                sharePreferenceHelper.isFirstLaunch(false)
                startActivity(Intent(applicationContext, AuthActivity::class.java))
                finish()
            } else {
                delay(2000)
                startActivity(Intent(applicationContext, OnBoardingActivity::class.java))
                finish()
            }
        }
    }
    private fun getProfile(){
        lifecycleScope.launch {
            if (applicationContext.isNetworkAvailable()) {
                viewModel.getProfiles().collect { response ->
                    if (response.data?.status == true && response.data.statusCode == 200 && response.data.data != null) {
                        if (sharePreferenceHelper.isUserLoggedIn()) {
//                            if (sharePreferenceHelper.getUser()?.is_subscribed == 0 && sharePreferenceHelper.getUser()?.trial_start_date.isNullOrEmpty()) {
                            if (sharePreferenceHelper.getUser()?.purchase_status == 0 || sharePreferenceHelper.getUser()?.purchase_status == 2 || sharePreferenceHelper.getUser()?.purchase_status == 4) {
                                Log.i("Splash_screen", "1")
                                sharePreferenceHelper.isFirstLaunch(false)
                                val sIntent = Intent(applicationContext, AuthActivity::class.java)
                                sIntent.putExtra(
                                    Constants.START_DESTINATION,
                                    StartDestination.Subscription()
                                )
                                //sIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(sIntent)
                                finish()
                            } else {
                                Log.i("Splash_screen", "2")
                                sharePreferenceHelper.isFirstLaunch(false)
                                val sIntent = Intent(applicationContext, MainActivity::class.java)
                                if (notificationsModel != null) {
                                    sIntent.putExtra("notificationData", notificationsModel)
                                }
                                if (deepLinkModel != null) {
                                    sIntent.putExtra("deepLinkData", deepLinkModel)
                                }
                                //sIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(sIntent)
                                finish()
                            }
                        }
                    }
                }
            } else {
                this@MySplashActivity.showToast(
                    applicationContext.resources.getString(
                        R.string.no_internet
                    ), false
                )
            }
        }
    }
    private fun initViewModelResponse() {//TODO handle expire date
        val isSubscriptionExpired = when (sharePreferenceHelper.getUser()?.purchase_status) {
            1 -> {
                sharePreferenceHelper.getUser()?.trial_end_date.isSubscriptionExpired()
            }
            3 -> {
                sharePreferenceHelper.getUser()?.expiry_date.isSubscriptionExpired()
            }
            else -> {
                true
            }
        }
        if (applicationContext.isNetworkAvailable()) {
            getProfile()
        }else if (sharePreferenceHelper.isUserLoggedIn() && sharePreferenceHelper.getUser()?.purchase_status != 0 && sharePreferenceHelper.getUser()?.purchase_status != 2 && sharePreferenceHelper.getUser()?.purchase_status != 4 && !isSubscriptionExpired){
            lifecycleScope.launch {
                sharePreferenceHelper.isFirstLaunch(false)
                val sIntent = Intent(applicationContext, MainActivity::class.java)
                if (notificationsModel != null) {
                    sIntent.putExtra("notificationData", notificationsModel)
                }
                if (deepLinkModel != null) {
                    sIntent.putExtra("deepLinkData", deepLinkModel)
                }
                //sIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(sIntent)
                finish()
            }
        } else {
            this@MySplashActivity.showToast(
                applicationContext.resources.getString(
                    R.string.splash_error_message
                ), false
            )
//            lifecycleScope.launch {
//                sharePreferenceHelper.clearAllPreferenceData()
//                sharePreferenceHelper.isFirstLaunch(false)
//                MyApplication.appContext.deleteAppCache()
//                MyApplication.appContext.deleteAppDownloadCache()
//                CoroutineScope(Dispatchers.IO).launch {
//                    Room.databaseBuilder(
//                        MyApplication.appContext,
//                        AppDatabase::class.java,
//                        "TrollingPoints"
//                    ).build().clearAllTables()
//                }
//                val sIntent = Intent(MyApplication.appContext, AuthActivity::class.java)
//                sIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                startActivity(sIntent)
//            }
        }
        /*lifecycleScope.launch {
            viewModel.profileResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            if (response.data?.status == true) {
                                viewModel.resetResponse()
                                if (sharePreferenceHelper.isUserLoggedIn()) {
                                    if(sharePreferenceHelper.getUser()?.is_subscribed == 0 && sharePreferenceHelper.getUser()?.trial_start_date.isNullOrEmpty()){
                                        Log.i("Splash_screen","1")
                                        sharePreferenceHelper.isFirstLaunch(false)
                                        val sIntent = Intent(applicationContext, AuthActivity::class.java)
                                        sIntent.putExtra(Constants.START_DESTINATION, StartDestination.Subscription())
                                        sIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(sIntent)
                                        finish()
                                    }else {
                                        Log.i("Splash_screen","2")
                                        sharePreferenceHelper.isFirstLaunch(false)
                                        val sIntent = Intent(applicationContext, MainActivity::class.java)
                                        if (notificationsModel != null) {
                                            sIntent.putExtra("notificationData", notificationsModel)
                                        }
                                        if (deepLinkModel != null) {
                                            sIntent.putExtra("deepLinkData", deepLinkModel)
                                        }
                                        sIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(sIntent)
                                        finish()
                                    }
                                }
                            }
                        }

                        is NetworkResult.Error -> {
                            viewModel.resetResponse()
                        }

                        is NetworkResult.Loading -> {
                        }

                        is NetworkResult.NoInternet -> {
                            this@MySplashActivity.showToast(
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
*/
    }

    private fun printHashKey(pContext: Context) {
        try {
            val info: PackageInfo = pContext.packageManager
                .getPackageInfo(pContext.packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA1")
                md.update(signature.toByteArray())
                val hashKey: String = String(Base64.encode(md.digest(), 0))
                Log.e("TAG", "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("TAG", "printHashKey()", e)
        } catch (e: Exception) {
            Log.e("TAG", "printHashKey()", e)
        }
    }
}