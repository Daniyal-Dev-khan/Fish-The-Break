package com.cp.fishthebreak.screens.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.ActivityMainBinding
import com.cp.fishthebreak.models.DeepLinkData
import com.cp.fishthebreak.models.group.ChatListData
import com.cp.fishthebreak.notifications.NotificationModel
import com.cp.fishthebreak.screens.bottom_sheets.PermissionBottomSheet
import com.cp.fishthebreak.screens.fragments.group.GroupFragment
import com.cp.fishthebreak.screens.fragments.home.HomeFragment
import com.cp.fishthebreak.screens.fragments.log.FishLogFragment
import com.cp.fishthebreak.screens.fragments.profile.ProfileFragment
import com.cp.fishthebreak.screens.fragments.save.SaveFragment
import com.cp.fishthebreak.utils.Constants
import com.cp.fishthebreak.utils.MapUiData
import com.cp.fishthebreak.utils.NavigationDirections
import com.cp.fishthebreak.utils.StartDestination
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.cp.fishthebreak.viewModels.group.ChatListViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private var doubleBackToExitPressedOnce = false
    private val fm: FragmentManager = supportFragmentManager
    private var homeFragment: HomeFragment? = HomeFragment()
    private var groupFragment: GroupFragment? = null
    private var fishLogFragment: FishLogFragment? = null
    private var saveFragment: SaveFragment? = null
    private var profileFragment: ProfileFragment? = null
    private var activeFragment: Fragment? = homeFragment
    private var selectedTab = 1
    private var notificationsModel: NotificationModel? = null
    private var deepLinkModel: DeepLinkData? = null
    private var permissionBottomSheet: PermissionBottomSheet? = null
    val viewModelChat: ChatListViewModel by viewModels()
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        mutableListOf(
            Manifest.permission.POST_NOTIFICATIONS,
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        )
    } else {
        mutableListOf(
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fullScreenWithStatusBarBlackIcon()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setStatusBarBackgroundTransparent()
        setContentView(binding.root)
        handleSystemBar(binding.root)
        registerOnBackPressedCallBack()
        initListeners()
        homeFragment?.let {
            fm.beginTransaction().add(R.id.nav_host_fragment_activity_main, it, "1").commit()
        }
        selectHomeTab()
        handlePermission()
        val extras = intent?.extras
        if (intent != null && intent.hasExtra("deepLinkData")) {
            try {
                deepLinkModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra("deepLinkData", DeepLinkData::class.java)
                } else {
                    intent.getParcelableExtra("deepLinkData")
                }
            } catch (ex: Exception) {

            }
        } else if (intent != null && intent.hasExtra("notificationData")) {
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
                        json, ChatListData::class.java
                    )
                    if (notificationType == "chat") {
                        notificationsModel?.navigation = NavigationDirections.OpenGroup(body)
                    } else if (notificationType == "invitation") {
                        notificationsModel?.navigation = NavigationDirections.OpenGroupList()
                    }
                } catch (ex: Exception) {
                    Log.e("noti_model_exp", ex.message.toString())
                }
            } else {
                Log.i("hasExtras_", "false")
            }
        }
        if (notificationsModel != null) {
            when (notificationsModel?.navigation) {
                is NavigationDirections.OpenGroup -> {
                    viewModelChat.navigateToGroupChat((notificationsModel?.navigation as NavigationDirections.OpenGroup).data)
                    selectGroupTab()
//                    val sIntent = Intent(applicationContext, NavGraphActivity::class.java)
//                    sIntent.putExtra(Constants.START_DESTINATION, StartDestination.OpenGroup((notificationsModel?.navigation as NavigationDirections.OpenGroup).data))
//                    startActivity(sIntent)
                }

                is NavigationDirections.OpenGroupList -> {
                    //selectGroupTab()
                }

                else -> {}
            }
        } else if (deepLinkModel != null) {
            val sIntent = Intent(applicationContext, NavGraphActivity::class.java)
            sIntent.putExtra(
                Constants.START_DESTINATION, StartDestination.CommonMap(
                    MapUiData.FromMessage(
                        deepLinkModel?.id ?: -1, deepLinkModel?.type ?: "", lat = null, lang = null
                    )
                )
            )
            startActivity(sIntent)
        }
    }

    private fun initListeners() {
        binding.bottomNav.btnHome.setOnSingleClickListener {
            selectHomeTab()
        }
        binding.bottomNav.btnGroup.setOnSingleClickListener {
            selectGroupTab()
        }
        binding.bottomNav.btnFishLog.setOnSingleClickListener {
            selectFishLogTab()
        }
        binding.bottomNav.floatingButtonFishLog.setOnClickListener {
            selectFishLogTab()
        }
        binding.bottomNav.btnSave.setOnSingleClickListener {
            selectSaveTab()
        }
        binding.bottomNav.btnProfile.setOnSingleClickListener {
            selectProfileTab()
        }
    }

    private fun selectHomeTab() {
        selectedTab = 1
        selectionChange(
            binding.bottomNav.tvHome, binding.bottomNav.ivHome, binding.bottomNav.btnHome
        )
        loadSelectedFragment(homeFragment)
    }

    private fun selectGroupTab() {
        selectedTab = 2
        selectionChange(
            binding.bottomNav.tvGroup, binding.bottomNav.ivGroup, binding.bottomNav.btnGroup
        )
        loadSelectedFragment(groupFragment)
    }

    private fun selectFishLogTab() {
        selectedTab = 3
        selectionChange(
            binding.bottomNav.tvFishLog,
            binding.bottomNav.floatingButtonFishLog,
            binding.bottomNav.btnFishLog
        )
        loadSelectedFragment(fishLogFragment)
    }

    private fun selectSaveTab() {
        selectedTab = 4
        selectionChange(
            binding.bottomNav.tvSave, binding.bottomNav.ivSave, binding.bottomNav.btnSave
        )
        loadSelectedFragment(saveFragment)
    }

    private fun selectProfileTab() {
        selectedTab = 5
        selectionChange(
            binding.bottomNav.tvProfile, binding.bottomNav.ivProfile, binding.bottomNav.btnProfile
        )
        loadSelectedFragment(profileFragment)
    }

    private fun selectionChange(tv: TextView, iv: ImageView, layout: ConstraintLayout) {

        binding.bottomNav.tvHome.setTextColor(
            ContextCompat.getColor(
                applicationContext, R.color.secondary400
            )
        )
        binding.bottomNav.tvGroup.setTextColor(
            ContextCompat.getColor(
                applicationContext, R.color.secondary400
            )
        )
        binding.bottomNav.tvFishLog.setTextColor(
            ContextCompat.getColor(
                applicationContext, R.color.secondary400
            )
        )
        binding.bottomNav.tvSave.setTextColor(
            ContextCompat.getColor(
                applicationContext, R.color.secondary400
            )
        )
        binding.bottomNav.tvProfile.setTextColor(
            ContextCompat.getColor(
                applicationContext, R.color.secondary400
            )
        )

        binding.bottomNav.ivHome.setColorFilter(
            ContextCompat.getColor(
                applicationContext, R.color.secondary400
            )
        )
        binding.bottomNav.ivGroup.setColorFilter(
            ContextCompat.getColor(
                applicationContext, R.color.secondary400
            )
        )
        binding.bottomNav.ivSave.setColorFilter(
            ContextCompat.getColor(
                applicationContext, R.color.secondary400
            )
        )
        binding.bottomNav.ivProfile.setColorFilter(
            ContextCompat.getColor(
                applicationContext, R.color.secondary400
            )
        )

        tv.setTextColor(
            ContextCompat.getColor(
                applicationContext, R.color.primary_color_celestial_blue
            )
        )
        if (iv.id != binding.bottomNav.floatingButtonFishLog.id) {
            iv.setColorFilter(
                ContextCompat.getColor(
                    applicationContext, R.color.primary_color_celestial_blue
                )
            )
        }
    }

    private fun loadSelectedFragment(selectedFragment: Fragment?) {
        if (selectedFragment == activeFragment) {
            return
        }
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        }

        when (selectedTab) {
            1 -> {
                if (homeFragment == null) {
                    homeFragment = HomeFragment()
                    fm.beginTransaction()
                        .add(R.id.nav_host_fragment_activity_main, homeFragment!!, "1")
                        .hide(homeFragment!!).commit()
                }
                activeFragment?.let { fm.beginTransaction().hide(it).show(homeFragment!!).commit() }
                activeFragment = homeFragment
            }

            2 -> {
                if (groupFragment == null) {
                    groupFragment = GroupFragment()
                    fm.beginTransaction()
                        .add(R.id.nav_host_fragment_activity_main, groupFragment!!, "2")
                        .hide(groupFragment!!).commit()
                }
                activeFragment?.let {
                    fm.beginTransaction().hide(it).show(groupFragment!!).commit()
                }
                activeFragment = groupFragment
            }

            3 -> {
                if (fishLogFragment == null) {
                    fishLogFragment = FishLogFragment()
                    fm.beginTransaction()
                        .add(R.id.nav_host_fragment_activity_main, fishLogFragment!!, "3")
                        .hide(fishLogFragment!!).commit()
                }
                activeFragment?.let {
                    fm.beginTransaction().hide(it).show(fishLogFragment!!).commit()
                }
                activeFragment = fishLogFragment
            }

            4 -> {
                if (saveFragment == null) {
                    saveFragment = SaveFragment()
                    fm.beginTransaction()
                        .add(R.id.nav_host_fragment_activity_main, saveFragment!!, "4")
                        .hide(saveFragment!!).commit()
                }
                activeFragment?.let { fm.beginTransaction().hide(it).show(saveFragment!!).commit() }
                activeFragment = saveFragment
            }

            5 -> {
                if (profileFragment == null) {
                    profileFragment = ProfileFragment()
                    fm.beginTransaction()
                        .add(R.id.nav_host_fragment_activity_main, profileFragment!!, "5")
                        .hide(profileFragment!!).commit()
                }
                activeFragment?.let {
                    fm.beginTransaction().hide(it).show(profileFragment!!).commit()
                }
                activeFragment = profileFragment
            }
        }
    }


    /**
     * Check for the permissions
     */
    private fun allPermissionsGranted() = permissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Permission request
     */
    private var permissionRequest: ActivityResultLauncher<Array<String>>? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            askNotificationPermission()
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.

                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun handlePermission() {
        if (!allPermissionsGranted()) {
            permissionRequest =
                registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                    if (permissions.all { it.value }) {

                    } else {
                        val deniedList: List<String> = permissions.filter {
                            !it.value
                        }.map {
                            it.key
                        }

                        if (deniedList.contains(Manifest.permission.ACCESS_FINE_LOCATION) || deniedList.contains(
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        ) {
                            openPermissionSheet(resources.getString(R.string.request_for_location))
                        } else if (deniedList.contains(Manifest.permission.POST_NOTIFICATIONS)) {
                            openPermissionSheet(resources.getString(R.string.request_for_notification))
                        }
                    }
                }
            permissionRequest?.launch(permissions.toTypedArray())
        } else {

        }
    }

    private fun openPermissionSheet(description: String) {
        permissionBottomSheet =
            PermissionBottomSheet(description, object : PermissionBottomSheet.OnItemClickListeners {
                override fun onSettingsClick() {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", packageName, null)
                    })
                }

                override fun onCancelClick() {
                }

            })
        permissionBottomSheet?.show(supportFragmentManager, "permissionBottomSheet")
    }

    private fun registerOnBackPressedCallBack() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                try {
                    if (supportFragmentManager.backStackEntryCount > 0) {
                        supportFragmentManager.popBackStack()
                    } else {
                        if (selectedTab == 1) {
                            if (doubleBackToExitPressedOnce) {
                                finish()
                                return
                            }

                            doubleBackToExitPressedOnce = true
                            Toast.makeText(
                                applicationContext,
                                resources.getString(R.string.press_again_to_exit),
                                Toast.LENGTH_SHORT
                            ).show()

                            // Reset the flag after 2 seconds
                            lifecycleScope.launch {
                                delay(2000)
                                doubleBackToExitPressedOnce = false
                            }
                        } else {
                            selectHomeTab()
                        }
                    }
                } catch (ex: Exception) {

                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onResume() {
        super.onResume()
        setStatusBarBackgroundTransparent()
    }

    private fun setStatusBarBackgroundWhite() {
        setStatusBarBackgroundWhite(binding.root)
    }

    private fun setStatusBarBackgroundTransparent() {
        setStatusBarBackgroundTransparent(binding.root)
    }
}