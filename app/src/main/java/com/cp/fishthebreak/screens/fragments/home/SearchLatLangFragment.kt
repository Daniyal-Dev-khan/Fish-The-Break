package com.cp.fishthebreak.screens.fragments.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.FragmentSearchLatLangBinding
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.cp.fishthebreak.utils.convertCoordinatesToLatLng
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.transformIntoLatPicker

class SearchLatLangFragment : Fragment() {
    private lateinit var binding: FragmentSearchLatLangBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSearchLatLangBinding.inflate(layoutInflater,container,false)
        initListener()
        return binding.root
    }

    private fun initListener(){
        binding.etLat.transformIntoLatPicker(childFragmentManager, isLongitude = false)
        binding.etLang.transformIntoLatPicker(childFragmentManager, isLongitude = true)
        binding.backButton.setOnClickListener {
            if(requireActivity() is NavGraphActivity){
                (requireActivity() as NavGraphActivity).onBack()
            }
        }
        binding.buttonSave.setOnClickListener {
            if(validate()) {
                val latLng = convertCoordinatesToLatLng(binding.etLat.text.toString().trim()+","+binding.etLang.text.toString().trim())
                if (latLng != null) {
                    val (latitude, longitude) = latLng
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        "latLang",
                        //Pair(latitude, longitude)
                        Pair(binding.etLat.text.toString().trim(), binding.etLang.text.toString().trim())
                    )
                    findNavController().popBackStack()
                }else{
                    requireActivity().showToast(resources.getString(R.string.something_went_wrong),false)
                }
            }
        }
    }

    private fun validate(): Boolean{
        if (binding.etLat.text.toString().trim().isEmpty()){
            requireActivity().showToast(resources.getString(R.string.error_location_select_lat),false)
            return false
        }
        if (binding.etLang.text.toString().trim().isEmpty()){
            requireActivity().showToast(resources.getString(R.string.error_location_select_lang),false)
            return false
        }
        return true
    }

}