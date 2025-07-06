package com.cp.fishthebreak.screens.fragments.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.FragmentMyVesselBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.screens.activities.AuthActivity
import com.cp.fishthebreak.screens.activities.BaseActivityResult
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.cp.fishthebreak.utils.Constants
import com.cp.fishthebreak.utils.MapUiData
import com.cp.fishthebreak.utils.StartDestination
import com.cp.fishthebreak.utils.convertCoordinatesToLatLng
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.transformIntoYearPicker
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.profile.vessel.VesselViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.Date

@AndroidEntryPoint
class MyVesselFragment : Fragment() {

    private lateinit var binding: FragmentMyVesselBinding
    val viewModel: VesselViewModel by viewModels()
    private val activityLauncher: BaseActivityResult<Intent, ActivityResult> =
        BaseActivityResult.registerActivityForResult(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMyVesselBinding.inflate(layoutInflater, container, false)
        if(requireActivity() is NavGraphActivity){
            (requireActivity() as NavGraphActivity).setStatusBarBackgroundWhite()
        }
        binding.lifecycleOwner = this
        binding.model = viewModel
        initListeners()
        initViewModelResponse()
        return binding.root
    }

    private fun initListeners() {
        binding.etYear.transformIntoYearPicker(requireActivity())
        binding.backButton.setOnClickListener {
            if (requireActivity() is NavGraphActivity) {
                (requireActivity() as NavGraphActivity).onBack()
            }
        }
        binding.boatRangeLayout.setOnSingleClickListener {
            var lat: Double? = null
            var lang:Double? = null
            val vasel = viewModel.getVesselResponse.value.data?.data
            if(!vasel.isNullOrEmpty()){
                lat = vasel.first().getLatFromString()
                lang = vasel.first().getLangFromString()
            }
            val sIntent = Intent(requireContext(), NavGraphActivity::class.java)
            sIntent.putExtra(
                Constants.START_DESTINATION,
                StartDestination.CommonMap(
                    MapUiData.BoatRange(
                        showBoatRangeControls = true,
                        saveBoatRangeToServer = false,
                        latitude = lat,
                        longitude = lang
                    )
                )
            )
            activityLauncher.launch(
                sIntent,
                object :
                    BaseActivityResult.OnActivityResult<ActivityResult> {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onActivityResult(result: ActivityResult) {
                        if(result.resultCode == Activity.RESULT_OK){
                            if (result.data != null) {
                                if (result.data?.hasExtra("boatRange") == true) {
                                    //binding.etBoatRange.text = result.data?.getStringExtra("boatRange")?:""
                                    viewModel.onBoatRangeEvent(result.data?.getStringExtra("boatRange")?:"")
                                    viewModel.onSaveClickEvent(null)
                                }
                            }
                        }
                    }
                })
            //findNavController().navigate(MyVesselFragmentDirections.actionMyVesselFragmentToBoatRangeFragment(true, false))
//            findNavController().navigate(MyVesselFragmentDirections.actionMyVesselFragmentToCommonMapFragment(MapUiData.BoatRange(
//                showBoatRangeControls = true,
//                saveBoatRangeToServer = false,
//                latitude = null,
//                longitude = null
//            )))
        }
        binding.dockLocationLayout.setOnSingleClickListener {
            //findNavController().navigate(MyVesselFragmentDirections.actionMyVesselFragmentToBoatRangeFragment(false, false))
            var lat: Double? = null
            var lang: Double? = null
            if (viewModel.vesselUIStates.value.dockLocation.isNotEmpty()) {
                val latLng = convertCoordinatesToLatLng(viewModel.vesselUIStates.value.dockLocation)
                if (latLng != null) {
                    val (latitude, longitude) = latLng
                    Log.i("convertCoordinates","Latitude: $latitude, Longitude: $longitude")
                    lat = latitude
                    lang = longitude
                } else {
                    Log.i("convertCoordinates","Invalid coordinate string format.")
                }

//                val latLang = viewModel.vesselUIStates.value.dockLocation.split(",")
//                if (latLang.size == 2) {
//                    lat = latLang.first().toDouble()
//                    lang = latLang[1].toDouble()
//                }

            }
            val sIntent = Intent(requireContext(), NavGraphActivity::class.java)
            sIntent.putExtra(
                Constants.START_DESTINATION,
                StartDestination.CommonMap(
                    MapUiData.BoatRange(
                        showBoatRangeControls = false,
                        saveBoatRangeToServer = false,
                        latitude = lat,
                        longitude = lang
                    )
                )
            )
            activityLauncher.launch(
                sIntent,
                object :
                    BaseActivityResult.OnActivityResult<ActivityResult> {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onActivityResult(result: ActivityResult) {
                        if(result.resultCode == Activity.RESULT_OK){
                            if (result.data != null) {
                                if (result.data?.hasExtra("dockLocation") == true) {
                                    binding.etDockLocation.text = result.data?.getStringExtra("dockLocation")?:""
                                    viewModel.onDockLocationEvent(result.data?.getStringExtra("dockLocation")?:"")
                                    viewModel.onSaveClickEvent(null)
                                }
                            }
                        }
                    }
                })
//            findNavController().navigate(MyVesselFragmentDirections.actionMyVesselFragmentToCommonMapFragment(MapUiData.BoatRange(
//                showBoatRangeControls = false,
//                saveBoatRangeToServer = false,
//                latitude = lat,
//                longitude = lang
//            )))
        }
    }

    private fun showHideLoader(visibility: Boolean) {
        if (visibility) {
            binding.loaderLayout.viewVisible()
        } else {
            binding.loaderLayout.viewGone()
        }
    }

    private fun initViewModelResponse() {
        /*findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("dockLocation")
            ?.observe(
                viewLifecycleOwner
            ) { result ->
                // Do something with the result.
                if (!result.isNullOrEmpty()) {
                    binding.etDockLocation.text = result ?: ""
                    viewModel.onDockLocationEvent(result ?: "")
                }
            }
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("boatRange")
            ?.observe(
                viewLifecycleOwner
            ) { result ->
                // Do something with the result.
                if (!result.isNullOrEmpty()) {
                    binding.etBoatRange.text = result ?: ""
                    viewModel.onBoatRangeEvent(result ?: "")
                }
            }*/
        lifecycleScope.launch {
            viewModel.getVesselResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true) {

                            } else {
                                requireActivity().showToast(
                                    response.data?.message ?: resources.getString(
                                        R.string.something_went_wrong
                                    ), false
                                )
                                viewModel.resetResponse()
                            }
                        }

                        is NetworkResult.Error -> {
                            showHideLoader(false)
                            requireActivity().showToast(
                                response.message.getOnErrorMessage(),
                                false
                            )
                            viewModel.resetResponse()
                        }

                        is NetworkResult.Loading -> {
                            showHideLoader(true)
                        }

                        is NetworkResult.NoInternet -> {
                            showHideLoader(false)
                            requireActivity().showToast(
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
        lifecycleScope.launch {
            viewModel.saveVesselResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true) {
                                requireActivity().showToast(
                                    response.data.message, true
                                )
                                viewModel.resetResponse()
                            } else {
                                requireActivity().showToast(
                                    response.data?.message ?: resources.getString(
                                        R.string.something_went_wrong
                                    ), false
                                )
                                viewModel.resetResponse()
                            }
                        }

                        is NetworkResult.Error -> {
                            showHideLoader(false)
                            requireActivity().showToast(
                                response.message.getOnErrorMessage(),
                                false
                            )
                            viewModel.resetResponse()
                        }

                        is NetworkResult.Loading -> {
                            showHideLoader(true)
                        }

                        is NetworkResult.NoInternet -> {
                            showHideLoader(false)
                            requireActivity().showToast(
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

    }
}