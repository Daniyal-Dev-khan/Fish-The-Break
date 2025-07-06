package com.cp.fishthebreak.screens.fragments.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.cp.fishthebreak.databinding.FragmentPasswordSuccessBinding


class PasswordSuccessFragment : Fragment() {

    private lateinit var binding: FragmentPasswordSuccessBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPasswordSuccessBinding.inflate(layoutInflater,container,false)
        initListeners()
        return binding.root
    }

    private fun initListeners(){
        binding.buttonLogin.setOnClickListener {
            findNavController().navigate(PasswordSuccessFragmentDirections.actionPasswordSuccessFragmentToLoginFragment())
        }
    }

}