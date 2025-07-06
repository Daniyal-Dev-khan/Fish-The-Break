package com.cp.fishthebreak.screens.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.ActivityOnBoardingBinding
import com.cp.fishthebreak.utils.OnSwipeTouchListener
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.cp.fishthebreak.utils.setTextAnimation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OnBoardingActivity : BaseActivity() {
    @Inject
    lateinit var sharePreferenceHelper: SharePreferenceHelper
    private lateinit var binding: ActivityOnBoardingBinding
    private var doubleBackToExitPressedOnce = false
    private var currentScreen = 0
    private val fadeDuration = 1000L
    private lateinit var animCrossFadeIn: Animation
    private lateinit var animCrossFadeOut: Animation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoardingBinding.inflate(layoutInflater)
        fullScreenWithStatusBarWhiteIcon()
        setContentView(binding.root)
        handleSystemBar(binding.root)
        lifecycleScope.launch {
            sharePreferenceHelper.isFirstLaunch(false)
        }
        initAnimation()
        initListeners()
        showOnBoardingScreensNext()
        registerOnBackPressedCallBack()
    }

    private fun initAnimation() {
        animCrossFadeIn = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.fade_in
        )
        animCrossFadeOut = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.fade_out
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListeners() {
        binding.onBoardLayout.setOnTouchListener(
            OnSwipeTouchListener(applicationContext,
                object : OnSwipeTouchListener.OnSwipeListener {
                    override fun onSwipeRight() {
                        if (currentScreen >= 2) {
                            currentScreen--
                            showOnBoardingScreensBack()
                        }
                    }

                    override fun onSwipeLeft() {
                        showOnBoardingScreensNext()
                    }

                    override fun onSwipeTop() {

                    }

                    override fun onSwipeBottom() {

                    }

                })
        )
        binding.buttonNext.setOnClickListener {
            showOnBoardingScreensNext()
        }
        binding.skipButton.setOnClickListener {
            startActivity(Intent(applicationContext, AuthActivity::class.java))
            finish()
        }
    }

    private fun fade(v1: ImageView, v2: ImageView, duration: Long = fadeDuration) {
        v1.animate().alpha(0f).duration = duration
        v2.animate().alpha(1f).duration = duration
    }

    private fun fade(v1: TextView, v2: TextView, duration: Long = fadeDuration) {
        v1.animate().alpha(0f).duration = duration
        v2.animate().alpha(1f).duration = duration
    }

    private fun showOnBoardingScreensNext() {
        currentScreen++
        when (currentScreen) {
            1 -> {
                binding.ivCover.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.on_board_1
                    )
                )
                fade(binding.ivCover1, binding.ivCover)
                setTextWithAnimation(
                    binding.tvTitle,
                    resources.getString(R.string.on_board_1_title)
                )
                setTextWithAnimation(
                    binding.tvDescription,
                    resources.getString(R.string.on_board_1_description)
                )
                binding.ivDot1.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.active_onboard
                    )
                )
            }

            2 -> {
                binding.ivCover1.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.on_board_2
                    )
                )
                fade(binding.ivCover, binding.ivCover1)
                setTextWithAnimation(
                    binding.tvTitle,
                    resources.getString(R.string.on_board_2_title)
                )
                setTextWithAnimation(
                    binding.tvDescription,
                    resources.getString(R.string.on_board_2_description)
                )
                binding.ivDot1.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.un_active_onboard
                    )
                )
                binding.ivDot2.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.active_onboard
                    )
                )

            }

            3 -> {
                binding.ivCover.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.on_board_3
                    )
                )
                fade(binding.ivCover1, binding.ivCover)
                setTextWithAnimation(
                    binding.tvTitle,
                    resources.getString(R.string.on_board_3_title)
                )
                setTextWithAnimation(
                    binding.tvDescription,
                    resources.getString(R.string.on_board_3_description)
                )
                binding.ivDot2.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.un_active_onboard
                    )
                )
                binding.ivDot3.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.active_onboard
                    )
                )
            }

            else -> {
                startActivity(Intent(applicationContext, AuthActivity::class.java))
                finish()
            }
        }
    }

    private fun showOnBoardingScreensBack() {
        when (currentScreen) {
            1 -> {
                binding.ivCover.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.on_board_1
                    )
                )
                fade(binding.ivCover1, binding.ivCover)
                setTextWithAnimation(
                    binding.tvTitle,
                    resources.getString(R.string.on_board_1_title)
                )
                setTextWithAnimation(
                    binding.tvDescription,
                    resources.getString(R.string.on_board_1_description)
                )
                binding.ivDot2.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.un_active_onboard
                    )
                )
                binding.ivDot1.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.active_onboard
                    )
                )
            }

            2 -> {
                binding.ivCover1.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.on_board_2
                    )
                )
                fade(binding.ivCover, binding.ivCover1)
                setTextWithAnimation(
                    binding.tvTitle,
                    resources.getString(R.string.on_board_2_title)
                )
                setTextWithAnimation(
                    binding.tvDescription,
                    resources.getString(R.string.on_board_2_description)
                )
                binding.ivDot3.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.un_active_onboard
                    )
                )
                binding.ivDot2.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.active_onboard
                    )
                )

            }

            3 -> {
                binding.ivCover.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.on_board_3
                    )
                )
                fade(binding.ivCover1, binding.ivCover)
                setTextWithAnimation(
                    binding.tvTitle,
                    resources.getString(R.string.on_board_3_title)
                )
                setTextWithAnimation(
                    binding.tvDescription,
                    resources.getString(R.string.on_board_3_description)
                )
                binding.ivDot2.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.un_active_onboard
                    )
                )
                binding.ivDot3.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.active_onboard
                    )
                )
            }

            else -> {
                startActivity(Intent(applicationContext, AuthActivity::class.java))
                finish()
            }
        }
    }

    private fun setTextWithAnimation(textView: TextView, text: String) {
        textView.setTextAnimation(text, fadeDuration / 2)
    }

    private fun crossFadeTextAnimation(
        title1: TextView,
        description1: TextView,
        title2: TextView,
        description2: TextView
    ) {
        title1.startAnimation(animCrossFadeOut)
        description1.startAnimation(animCrossFadeOut)
        title2.startAnimation(animCrossFadeIn)
        description2.startAnimation(animCrossFadeIn)
    }

    private fun registerOnBackPressedCallBack() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
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
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)
    }
}