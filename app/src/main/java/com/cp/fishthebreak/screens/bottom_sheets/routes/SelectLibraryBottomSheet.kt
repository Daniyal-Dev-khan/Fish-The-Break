package com.cp.fishthebreak.screens.bottom_sheets.routes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.MyViewPagerAdapter
import com.cp.fishthebreak.adapters.routes.SelectedLibraryAdapter
import com.cp.fishthebreak.databinding.FragmentSelectLibraryBottomSheetBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.models.points.SaveFishLogData
import com.cp.fishthebreak.models.points.SavePointsData
import com.cp.fishthebreak.screens.fragments.routes.RouteFishLogFragment
import com.cp.fishthebreak.screens.fragments.routes.RoutesLocationFragment
import com.cp.fishthebreak.screens.fragments.save.SavedRoutesFragment
import com.cp.fishthebreak.screens.fragments.save.SavedTrollingFragment
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.viewModels.routes.RoutesFishLogViewModel
import com.cp.fishthebreak.viewModels.routes.RoutesLocationViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SelectLibraryBottomSheet(private val listener: OnClickListeners) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentSelectLibraryBottomSheetBinding
    private var selectedTab = 1
    private lateinit var adapter: MyViewPagerAdapter
    val viewModelLocation: RoutesLocationViewModel by activityViewModels()
    val viewModelFishLog: RoutesFishLogViewModel by activityViewModels()
    private val selectedList = ArrayList<Any>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        try {
            (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
            (dialog as? BottomSheetDialog)?.behavior?.isDraggable = false
//            (dialog as? BottomSheetDialog)?.behavior?.peekHeight = (screenValue() * 0.95).toInt()
            isCancelable = false
        } catch (ex: Exception) {
        }
        binding = FragmentSelectLibraryBottomSheetBinding.inflate(layoutInflater,container,false)
        binding.tvFishLogs.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.tvFishLogs.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.selected_tab)
        initListeners()
        initViewpager()
        initAdapter()
        initViewModelResponse()
        return binding.root
    }

    private fun initAdapter(){
        binding.rvSelected.adapter = SelectedLibraryAdapter(selectedList,object: SelectedLibraryAdapter.OnItemClickListener{
            override fun onCrossClick(position: Int) {
                when(selectedList[position]){
                    is SavePointsData->{
                        viewModelLocation.removeSelection(selectedList[position] as SavePointsData)
                    }
                    is SaveFishLogData ->{
                       viewModelFishLog.removeSelection(selectedList[position] as SaveFishLogData)
                    }
                }
                selectedList.remove(selectedList[position])
                binding.rvSelected.adapter?.notifyDataSetChanged()
            }
        })
    }

    private fun initViewModelResponse() {
        lifecycleScope.launch {
            viewModelFishLog.fishLogResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {

                        }

                        is NetworkResult.Error -> {
                            dialog?.showToast(
                                requireContext(),
                                response.message.getOnErrorMessage(),
                                false
                            )
                            viewModelFishLog.resetResponse()
                        }

                        is NetworkResult.Loading -> {

                        }

                        is NetworkResult.NoInternet -> {
                            dialog?.showToast(
                                requireContext(),
                                response.message.getOnErrorMessage(),
                                false
                            )
                            viewModelFishLog.resetResponse()
                        }

                        is NetworkResult.NoCallYet -> {
                        }
                    }
                }
        }
        lifecycleScope.launch {
            viewModelLocation.locationsResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {

                        }

                        is NetworkResult.Error -> {
                            dialog?.showToast(
                                requireContext(),
                                response.message.getOnErrorMessage(),
                                false
                            )
                            viewModelLocation.resetResponse()
                        }

                        is NetworkResult.Loading -> {

                        }

                        is NetworkResult.NoInternet -> {
                            dialog?.showToast(
                                requireContext(),
                                response.message.getOnErrorMessage(),
                                false
                            )
                            viewModelLocation.resetResponse()
                        }

                        is NetworkResult.NoCallYet -> {
                        }
                    }
                }
        }
        lifecycleScope.launch {
            viewModelLocation.selectUnselectResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    if(response != null){
                        if(response.isSelected) {
                            selectedList.add(response)
                        }else{
                            selectedList.remove(response)
                        }
                        binding.rvSelected.adapter?.notifyDataSetChanged()
                        viewModelLocation.resetSelectResponse()
                    }
                }
        }
        lifecycleScope.launch {
            viewModelFishLog.selectUnselectResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    if(response != null){
                        if(response.isSelected) {
                            selectedList.add(response)
                        }else{
                            selectedList.remove(response)
                        }
                        binding.rvSelected.adapter?.notifyDataSetChanged()
                        viewModelLocation.resetSelectResponse()
                    }
                }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    private fun initViewpager() {
        lifecycleScope.launch {
            delay(500)
            viewModelLocation.resetAllSelection()
            viewModelFishLog.resetAllSelection()
            binding.viewPager.offscreenPageLimit = 2
            adapter = MyViewPagerAdapter(childFragmentManager, lifecycle)
            adapter.addFragment(RouteFishLogFragment())
            adapter.addFragment(RoutesLocationFragment())
            adapter.addFragment(SavedTrollingFragment())
            adapter.addFragment(SavedRoutesFragment())
            binding.viewPager.isUserInputEnabled = false
            binding.viewPager.adapter = adapter
        }
    }

    private fun initListeners() {
        binding.saveButton.setOnClickListener {
            if(selectedList.isNotEmpty() && selectedList.size >= 2){
                listener.onNextClick(selectedList)
                dismiss()
            }
            else if(selectedList.isEmpty()){
                dialog?.showToast(requireContext(),resources.getString(R.string.please_select_points_to_measure_distance),false)
            }
            else{
                dialog?.showToast(requireContext(),resources.getString(R.string.error_points_to_measure_distance),false)
            }
        }
        binding.backButton.setOnClickListener {
            listener.onCancel()
            dismiss()
        }
        binding.tvLocations.setOnClickListener {
            if (selectedTab != 2) {
                setSelection(2, binding.tvLocations)
                binding.viewPager.currentItem = selectedTab - 1
            }
        }
        binding.tvFishLogs.setOnClickListener {
            if (selectedTab != 1) {
                setSelection(1, binding.tvFishLogs)
                binding.viewPager.currentItem = selectedTab - 1
            }
        }
        binding.tvTrolling.setOnClickListener {
            if (selectedTab != 3) {
                setSelection(3, binding.tvTrolling)
                binding.viewPager.currentItem = selectedTab - 1
            }
        }
        binding.tvRoute.setOnClickListener {
            if (selectedTab != 4) {
                setSelection(4, binding.tvRoute)
                binding.viewPager.currentItem = selectedTab - 1
            }
        }
    }

    private fun setSelection(position: Int, tab: TextView) {
        val slideTo = (position - selectedTab) * tab.width
        val translateAnimation = TranslateAnimation(0F, slideTo.toFloat(), 0F, 0F)
        translateAnimation.duration = 100
        when (selectedTab) {
            1 -> {
                binding.tvFishLogs.startAnimation(translateAnimation)
            }
            2 -> {
                binding.tvLocations.startAnimation(translateAnimation)
            }
            3 -> {
                binding.tvTrolling.startAnimation(translateAnimation)
            }
            4 -> {
                binding.tvRoute.startAnimation(translateAnimation)
            }
        }
        translateAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                binding.tvLocations.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.secondary400
                    )
                )
                binding.tvFishLogs.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.secondary400
                    )
                )
                binding.tvTrolling.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.secondary400
                    )
                )
                binding.tvRoute.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.secondary400
                    )
                )

                binding.tvLocations.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.unselected_tab)
                binding.tvFishLogs.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.unselected_tab)
                binding.tvTrolling.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.unselected_tab)
                binding.tvRoute.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.unselected_tab)

                tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                tab.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.selected_tab)
            }

            override fun onAnimationRepeat(p0: Animation?) {

            }

        })
        selectedTab = position
    }

    interface OnClickListeners{
        fun onCancel()
        fun onNextClick(list: List<Any>)
    }
}