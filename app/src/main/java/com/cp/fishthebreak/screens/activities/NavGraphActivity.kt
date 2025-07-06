package com.cp.fishthebreak.screens.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.ActivityNavGraphBinding
import com.cp.fishthebreak.utils.Constants.Companion.START_DESTINATION
import com.cp.fishthebreak.utils.NavigationDirections
import com.cp.fishthebreak.utils.StartDestination
import com.cp.fishthebreak.utils.back
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NavGraphActivity : BaseActivity() {
    private lateinit var binding: ActivityNavGraphBinding
    private var navHostFragment: NavHostFragment? = null
    private var navController: NavController? = null
    private var startDestination: StartDestination? = null
    private var sendResultBack = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavGraphBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullScreenWithStatusBarBlackIcon()
        setStatusBarBackgroundWhite()
        handleSystemBar(binding.root)
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment?.navController
        if (intent != null && intent.hasExtra(START_DESTINATION)) {
            startDestination = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(START_DESTINATION, StartDestination::class.java)
            } else {
                intent.getParcelableExtra(START_DESTINATION) as StartDestination?
            }
            startDestination?.let { setStartDestination(it) }
        }
    }

    private fun setStartDestination(destination: StartDestination) {
        when(destination){
            is StartDestination.ApplyLayers -> {
                val bundle = Bundle()
                launchDestination(R.id.applyLayersFragment, bundle)
            }

            is StartDestination.SearchLocations -> {
                val bundle = Bundle()
                bundle.putBoolean("isFromRoute", destination.isFromRoute)
                launchDestination(R.id.searchLocationFragment, bundle)
            }

            is StartDestination.EditPrifile -> {
                val bundle = Bundle()
                launchDestination(R.id.editProfileFragment, bundle)
            }
            is StartDestination.UpdatePassword -> {
                val bundle = Bundle()
                launchDestination(R.id.updatePasswordFragment, bundle)
            }
            is StartDestination.UpdateEmail -> {
                val bundle = Bundle()
                launchDestination(R.id.changeEmailFragment, bundle)
            }
            is StartDestination.UpdatetvPreferences -> {
                val bundle = Bundle()
                launchDestination(R.id.preferencesFragment, bundle)
            }
            is StartDestination.SaveOfflineMap -> {
                val bundle = Bundle()
                launchDestination(R.id.offlineMapsFragment, bundle)
            }
            is StartDestination.ResoursesData -> {
                val bundle = Bundle()
                launchDestination(R.id.articlesFragment, bundle)
            }
            is StartDestination.VesselsData -> {
                val bundle = Bundle()
                launchDestination(R.id.myVesselFragment, bundle)
            }
            is StartDestination.Subscription -> {
                val bundle = Bundle()
                launchDestination(R.id.subscriptionFragment, bundle)
            }
            is StartDestination.ActiveSubscription -> {
                val bundle = Bundle()
                launchDestination(R.id.viewSubscriptionsFragment, bundle)
            }

            is StartDestination.ReadMoreAboutLayer -> {
                val bundle = Bundle()
                bundle.putString("webUrl", destination.link)
                launchDestination(R.id.singleResourceFragment, bundle)
            }
            is StartDestination.OpenGroup -> {
                sendResultBack = true
                val bundle = Bundle()
                bundle.putSerializable("chatData", destination.data)
                launchDestination(R.id.groupDetailsFragment, bundle)
            }
            is StartDestination.RejectGroupReason -> {
                val bundle = Bundle()
                launchDestination(R.id.rejectionReasonFragment, bundle)
            }
            is StartDestination.CommonMap -> {
                sendResultBack = true
                val bundle = Bundle()
                bundle.putParcelable("data", destination.data)
                launchDestination(R.id.commonMapFragment, bundle)
            }
        }
    }

    private fun launchDestination(destination: Int, bundle: Bundle? = null) {
        val inflater = navHostFragment?.navController?.navInflater
        val graph = inflater?.inflate(R.navigation.home_nav_graph)
        graph?.setStartDestination(destination)
        if (graph != null) {
            if (bundle == null) {
                navHostFragment?.navController?.graph = graph
            } else {
                navHostFragment?.navController?.setGraph(graph, bundle)
            }
        }
    }

    override fun onDestroy() {
        if(sendResultBack) {
            val sIntent = Intent()
            setResult(RESULT_OK, sIntent)
        }
        super.onDestroy()
    }

    fun onBack() {
        this.back(navHostFragment)
    }

    fun setStatusBarBackgroundWhite() {
        setStatusBarBackgroundWhite(binding.root)
    }
    fun setStatusBarBackgroundTransparent() {
        setStatusBarBackgroundTransparent(binding.root)
    }
}