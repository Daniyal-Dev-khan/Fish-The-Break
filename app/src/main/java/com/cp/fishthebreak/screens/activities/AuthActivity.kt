package com.cp.fishthebreak.screens.activities

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.ActivityAuthBinding
import com.cp.fishthebreak.utils.Constants
import com.cp.fishthebreak.utils.StartDestination
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AuthActivity : BaseActivity() {
    private lateinit var binding: ActivityAuthBinding
    private var doubleBackToExitPressedOnce = false
    private var navHostFragment: NavHostFragment? = null
    private var navController: NavController? = null
    private var startDestination: StartDestination? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        fullScreenWithStatusBarBlackIcon()
        handleSystemBar(binding.root)
        setStatusBarBackgroundWhite()
        setContentView(binding.root)
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment?.navController
        if (intent != null && intent.hasExtra(Constants.START_DESTINATION)) {
            startDestination = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Constants.START_DESTINATION, StartDestination::class.java)
            } else {
                intent.getParcelableExtra(Constants.START_DESTINATION) as StartDestination?
            }
            startDestination?.let { setStartDestination(it) }
        }
        registerOnBackPressedCallBack()
    }

    private fun setStartDestination(destination: StartDestination) {
        when(destination){
            is StartDestination.Subscription -> {
                val bundle = Bundle()
                launchDestination(R.id.subscriptionFragment, bundle)
            }
            else->{

            }
        }
    }

    private fun launchDestination(destination: Int, bundle: Bundle? = null) {
        val inflater = navHostFragment?.navController?.navInflater
        val graph = inflater?.inflate(R.navigation.auth_nav_graph)
        graph?.setStartDestination(destination)
        if (graph != null) {
            if (bundle == null) {
                navHostFragment?.navController?.graph = graph
            } else {
                navHostFragment?.navController?.setGraph(graph, bundle)
            }
        }
    }

    private fun registerOnBackPressedCallBack(){
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                try {
                    if (navHostFragment != null && (navHostFragment?.childFragmentManager?.backStackEntryCount
                            ?: 0) == 0
                    ) {
                        if (doubleBackToExitPressedOnce) {
                            finish()
                            return
                        }

                        doubleBackToExitPressedOnce = true
                        Toast.makeText(applicationContext, resources.getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show()

                        // Reset the flag after 2 seconds
                        lifecycleScope.launch {
                            delay(2000)
                            doubleBackToExitPressedOnce = false
                        }
                    } else {
                        navController?.navigateUp()
                        //onBackPressedDispatcher.onBackPressed()
                    }
                } catch (ex: Exception) {

                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)
    }
    fun setStatusBarBackgroundWhite() {
        setStatusBarBackgroundWhite(binding.root)
    }
    fun setStatusBarBackgroundTransparent() {
        setStatusBarBackgroundTransparent(binding.root)
    }
}