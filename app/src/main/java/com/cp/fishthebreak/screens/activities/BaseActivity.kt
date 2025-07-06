package com.cp.fishthebreak.screens.activities

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.*
import com.cp.fishthebreak.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
    }

    fun setStatusBarTransparent(view: View) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(view) { root, windowInset ->
            val inset = windowInset.getInsets(WindowInsetsCompat.Type.systemBars())
            root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = inset.left
                bottomMargin = inset.bottom
                rightMargin = inset.right
                topMargin = 0
            }
            WindowInsetsCompat.CONSUMED
        }
    }


    fun setStatusBarShown(view: View) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(view) { root, windowInset ->
            val inset = windowInset.getInsets(WindowInsetsCompat.Type.systemBars())
            val inset1 = windowInset.getInsets(WindowInsetsCompat.Type.statusBars())
            root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = inset.left
                bottomMargin = inset.bottom
                topMargin = inset1.top
                rightMargin = inset.right
            }
            WindowInsetsCompat.CONSUMED
        }
    }
    fun setStatusBarBackgroundWhite(view: View) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.white)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(view) { root, windowInset ->
            val inset = windowInset.getInsets(WindowInsetsCompat.Type.systemBars())
            root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = inset.bottom
            }
            WindowInsetsCompat.CONSUMED
        }
    }
    fun setStatusBarBackgroundTransparent(view: View) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(view) { root, windowInset ->
            val inset = windowInset.getInsets(WindowInsetsCompat.Type.systemBars())
            root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = inset.bottom
            }
            WindowInsetsCompat.CONSUMED
        }
    }

    fun handleSystemBar(container: View) {

        container.setOnApplyWindowInsetsListener { view, insets ->
            val navigationBarHeight = WindowInsetsCompat.toWindowInsetsCompat(insets)
                .getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            view.updatePadding(bottom = navigationBarHeight)
            WindowInsetsCompat.CONSUMED.toWindowInsets()!!
        }
    }

    fun fullScreenWithStatusBarWhiteIcon() {
        //icon color -> white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController?.setSystemBarsAppearance(
                0,
                APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            setStatusBarLightText(window, true)
        }
        //setStatusBarLightText(window, true)
    }

    private fun setStatusBarLightText(window: Window, isLight: Boolean) {
        setStatusBarLightTextOldApi(window, isLight)
        setStatusBarLightTextNewApi(window, isLight)
    }

    private fun setStatusBarLightTextOldApi(window: Window, isLight: Boolean) {
        val decorView = window.decorView
        decorView.systemUiVisibility =
            if (isLight) {
                decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() or View.SYSTEM_UI_FLAG_IMMERSIVE
            } else {
                decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_IMMERSIVE
            }
        getWindow().statusBarColor = Color.TRANSPARENT

    }

    private fun setStatusBarLightTextNewApi(window: Window, isLightText: Boolean) {
        ViewCompat.getWindowInsetsController(window.decorView)?.apply {
            // Light text == dark status bar
            isAppearanceLightStatusBars = !isLightText
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.navigationBars())
        }
        getWindow().statusBarColor = Color.TRANSPARENT
    }

    fun fullScreenWithStatusBarBlackIcon() {
        //icon color -> black
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController?.setSystemBarsAppearance(
                APPEARANCE_LIGHT_STATUS_BARS,
                APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            setStatusBarLightText(window, false)
        }
        //setStatusBarLightText(window, false)
    }
}