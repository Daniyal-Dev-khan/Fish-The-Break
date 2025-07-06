package com.cp.fishthebreak.screens.fragments.auth

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.navArgs
import com.cp.fishthebreak.databinding.FragmentAccountCreatedBinding
import com.cp.fishthebreak.screens.activities.AuthActivity
import com.cp.fishthebreak.screens.activities.MainActivity
import com.cp.fishthebreak.utils.Constants


class AccountCreatedFragment : Fragment() {
    private lateinit var binding: FragmentAccountCreatedBinding
    private val navArgs: AccountCreatedFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAccountCreatedBinding.inflate(layoutInflater, container, false)
        initView()
        initListeners()
        return binding.root
    }

    private fun initView() {
        if(requireActivity() is AuthActivity){
            (requireActivity() as AuthActivity).setStatusBarBackgroundTransparent()
            (requireActivity() as AuthActivity).fullScreenWithStatusBarWhiteIcon()
        }
        if (navArgs.subscriptionType == Constants.SUBSCRIPTION) {
            binding.tvTitle.text =
                "You have successfully subscribed premium membership ${navArgs.period} plan. Enjoy Fish the Break\n"
        } else {
            binding.tvTitle.text =
                "Your ${navArgs.period} days free trial has started. Enjoy Fish the Break\n"
        }
    }

    private fun initListeners() {
        requireActivity().onBackPressedDispatcher.addCallback(object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActivity(Intent(requireContext(), MainActivity::class.java))
                requireActivity().finish()
            }
        })
        binding.buttonLogin.setOnClickListener {
            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish()
        }
    }

}