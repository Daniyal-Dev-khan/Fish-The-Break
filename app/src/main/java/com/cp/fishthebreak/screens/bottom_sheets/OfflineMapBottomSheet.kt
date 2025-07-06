package com.cp.fishthebreak.screens.bottom_sheets

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
import com.cp.fishthebreak.adapters.profile.OnItemClickListener
import com.cp.fishthebreak.adapters.profile.SelectOfflineMapAdapter
import com.cp.fishthebreak.databinding.FragmentOfflineMapBottomSheetBinding
import com.cp.fishthebreak.models.map.OfflineMap
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.cp.fishthebreak.screens.fragments.profile.OfflineMapsFragmentDirections
import com.cp.fishthebreak.utils.Constants
import com.cp.fishthebreak.utils.MapUiData
import com.cp.fishthebreak.utils.StartDestination
import com.cp.fishthebreak.utils.getMapPathToLoadOffline
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.map.OfflineMapViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

@AndroidEntryPoint
class OfflineMapBottomSheet(private val map: OfflineMap?, private val isLiveMapLoaded: Boolean, private val listener: OnItemClickListener) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentOfflineMapBottomSheetBinding
    val viewModel: OfflineMapViewModel by viewModels()
    private var selectedMap: OfflineMap? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        try {
            (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
            (dialog as? BottomSheetDialog)?.behavior?.isDraggable = false
        } catch (ex: Exception) {
        }
        binding = FragmentOfflineMapBottomSheetBinding.inflate(layoutInflater,container,false)
        initDataBinding()
        initListeners()
        return binding.root
    }

    private fun initListeners() {
        binding.backButton.setOnClickListener {
            val onlineMap = viewModel.offlineMapList.value.filter { item-> item.id == -1 }
            if(onlineMap.isNotEmpty()) {
                listener.onMapSelect((onlineMap).first())
                dismiss()
            }
        }
        binding.loadMapButton.setOnClickListener {
            selectedMap?.mapPath?.let {mapPath->
                if(selectedMap?.id == -1){
                    listener.onMapSelect(selectedMap)
                    dismiss()
                }
                else if (requireContext().getMapPathToLoadOffline("${mapPath}/p13")
                        .isNotEmpty()
                ) {
                    listener.onMapSelect(selectedMap)
                    dismiss()
                }else{
                    dialog?.showToast(requireContext(),"Map path not found", false)
                }
            }
        }
    }

    private fun initDataBinding() {
        binding.lifecycleOwner = this@OfflineMapBottomSheet
        binding.viewModel = viewModel
        binding.mapAdapter = SelectOfflineMapAdapter(listOf(), object : com.cp.fishthebreak.adapters.profile.OnItemClickListener{
            override fun onMapClick(data: OfflineMap) {
                viewModel.offlineMapList.value.forEach { item->
                    item.isSelected = false
                }
                data.isSelected = true
                binding.rv.adapter?.notifyDataSetChanged()
                selectedMap = data
            }
        })
        if(map == null){
            selectedMap = OfflineMap(
                id = -1,
                name = "Live Map",
                description = "Display live map",
                image = "",
                mapPath = "",
                Date().time
            )
        }else {
            selectedMap = map
        }
        viewModel.getOfflineMap(addLiveMap = true, map, isLiveMapLoaded = isLiveMapLoaded)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    interface OnItemClickListener{
        fun onMapSelect(selectedMap: OfflineMap?)
    }

}