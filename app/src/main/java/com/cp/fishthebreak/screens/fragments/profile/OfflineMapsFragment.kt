package com.cp.fishthebreak.screens.fragments.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.profile.OfflineMapAdapter
import com.cp.fishthebreak.adapters.profile.OfflineMapListener
import com.cp.fishthebreak.databinding.FragmentOfflineMapsBinding
import com.cp.fishthebreak.models.map.OfflineMap
import com.cp.fishthebreak.screens.activities.AuthActivity
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.cp.fishthebreak.screens.bottom_sheets.ConfirmBottomSheet
import com.cp.fishthebreak.utils.Constants
import com.cp.fishthebreak.utils.MapUiData
import com.cp.fishthebreak.utils.StartDestination
import com.cp.fishthebreak.utils.getMapPathToLoadOffline
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.viewModels.map.OfflineMapViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OfflineMapsFragment : Fragment() {
    private lateinit var binding: FragmentOfflineMapsBinding
    val viewModel: OfflineMapViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOfflineMapsBinding.inflate(layoutInflater, container, false)
        if(requireActivity() is NavGraphActivity){
            (requireActivity() as NavGraphActivity).setStatusBarBackgroundWhite()
        }
        initDataBinding()
        initListeners()
        return binding.root
    }

    private fun initListeners() {
        binding.pullToRefresh.setOnRefreshListener {
            viewModel.getOfflineMap()
            binding.pullToRefresh.isRefreshing = false
        }
        binding.backButton.setOnClickListener {
            if (requireActivity() is NavGraphActivity) {
                (requireActivity() as NavGraphActivity).onBack()
            }
        }
        binding.addButton.setOnSingleClickListener {
            val sIntent = Intent(requireContext(), NavGraphActivity::class.java)
            sIntent.putExtra(
                Constants.START_DESTINATION,
                StartDestination.CommonMap(MapUiData.OfflineMap(null))
            )
            startActivity(sIntent)
//            findNavController().navigate(
//                OfflineMapsFragmentDirections.actionOfflineMapsFragmentToCommonMapFragment(
//                    MapUiData.OfflineMap(
//                        null
//                    )
//                )
//            )
            //findNavController().navigate(OfflineMapsFragmentDirections.actionOfflineMapsFragmentToGenerateOfflineMapFragment(null))
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getOfflineMap()
    }

    private fun initDataBinding() {
        binding.lifecycleOwner = this@OfflineMapsFragment
        binding.viewModel = viewModel
        binding.mapAdapter = OfflineMapAdapter(listOf(), object : OfflineMapListener {
            override fun onMapDeleteClick(data: OfflineMap) {
                val dialog =
                    ConfirmBottomSheet(resources.getString(R.string.confirmation),
                        resources.getString(R.string.delete_point_alert),
                        object : ConfirmBottomSheet.OnItemClickListener {
                            override fun onYesClick() {
                                viewModel.deleteMap(data)
                            }

                            override fun onCancelClick() {

                            }

                        })
                dialog.show(childFragmentManager, "ConfirmBottomSheet")
            }

            override fun onMapClick(data: OfflineMap) {
                if (requireContext().getMapPathToLoadOffline("${data.mapPath ?: ""}/p13")
                        .isNotEmpty()
                ) {
                    findNavController().navigate(
                        OfflineMapsFragmentDirections.actionOfflineMapsFragmentToGenerateOfflineMapFragment(
                            data.mapPath
                        )
                    )
                } else {
                    requireActivity().showToast("Map path not found", false)
                }
            }

        })
        viewModel.getOfflineMap()
    }
}