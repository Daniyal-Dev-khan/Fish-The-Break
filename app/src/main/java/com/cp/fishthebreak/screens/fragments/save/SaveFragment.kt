package com.cp.fishthebreak.screens.fragments.save

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.MyViewPagerAdapter
import com.cp.fishthebreak.databinding.FragmentSaveBinding
import com.cp.fishthebreak.screens.bottom_sheets.PointFilterBottomSheet
import com.cp.fishthebreak.utils.getTrollingTime
import com.cp.fishthebreak.utils.hideKeyboardFrom
import com.cp.fishthebreak.utils.onSearch
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.cp.fishthebreak.utils.showKeyboard
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.SyncViewModel
import com.cp.fishthebreak.viewModels.saved.SavedLocationViewModel
import com.cp.fishthebreak.viewModels.saved.SavedRouteViewModel
import com.cp.fishthebreak.viewModels.saved.SavedTrollingViewModel
import com.cp.fishthebreak.worker.WorkerStarter.Companion.WORK_MANAGER_TROLLING_TAG
import com.cp.fishthebreak.worker.WorkerStarter.Companion.workRequestId
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.BasemapStyle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SaveFragment : Fragment() {

    private lateinit var binding: FragmentSaveBinding
    private var selectedTab = 1
    private val delayTime = 700L
    private lateinit var adapter: MyViewPagerAdapter
    val viewModelTrolling: SavedTrollingViewModel by activityViewModels()
    val viewModelRoute: SavedRouteViewModel by activityViewModels()
    val viewModelLocation: SavedLocationViewModel by activityViewModels()
    val syncSharedViewModel: SyncViewModel by activityViewModels()
    private val handler: Handler = Handler(Looper.getMainLooper())

    val runableTimer = object : Runnable {
        override fun run() {
            binding.tvSync.text = when(binding.tvSync.text.toString().length){
                9->{"Syncing .."}
                10->{"Syncing ..."}
                11->{"Syncing ."}
                else->{"Syncing "}
            }
            handler.postDelayed(this, delayTime)// 1 seconds
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSaveBinding.inflate(layoutInflater, container, false)
        initViewModelResponse()
        initListeners()
        binding.tvLocations.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.tvLocations.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.selected_tab)
        initViewpager()
        return binding.root
    }

    private fun initViewModelResponse(){
        lifecycleScope.launch {
            syncSharedViewModel.syncResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    if (response) {
                        Log.i("SaveFragmentListener", "True")
                        binding.tvSync.viewVisible()
                        binding.tvSync.text= "Syncing ."
                        handler.postDelayed(runableTimer, delayTime)
                    }else{
                        Log.i("SaveFragmentListener", "False")
                        handler.removeCallbacks(runableTimer)
                        binding.tvSync.viewGone()
                        viewModelLocation.getAllSavedLocations(1)
                        viewModelTrolling.getAllSavedTrolling(1)
                        viewModelRoute.getAllSavedRoute(1)
                    }
                }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runableTimer)
    }

    private fun initViewpager() {
        lifecycleScope.launch {
            delay(500)
            binding.viewPager.offscreenPageLimit = 3
            adapter = MyViewPagerAdapter(childFragmentManager, lifecycle)
            adapter.addFragment(SavedPointsFragment())
            adapter.addFragment(SavedTrollingFragment())
            adapter.addFragment(SavedRoutesFragment())
            binding.viewPager.isUserInputEnabled = false
            binding.viewPager.adapter = adapter
            showHideLoader(false)
        }
    }

    private fun showHideLoader(visibility: Boolean) {
        if (visibility) {
            binding.loaderLayout.viewVisible()
        } else {
            binding.loaderLayout.viewGone()
        }
    }

    private fun updateSearchResults(){
        when(selectedTab){
            1->{
                viewModelLocation.getAllSavedLocations(1)
            }
            2->{
                viewModelTrolling.getAllSavedTrolling(1)
            }
            3->{
                viewModelRoute.getAllSavedRoute(1)
            }
        }
    }
    private fun initListeners() {
//        (WorkManager.getInstance(requireContext())).getWorkInfosByTagLiveData(WORK_MANAGER_TROLLING_TAG).observe(viewLifecycleOwner,
//            Observer {
//                Log.i("SaveFragmentListener","WORK_MANAGER_TROLLING_TAG")
//            })
        binding.etSearch.onSearch {
            requireContext().hideKeyboardFrom(binding.ivClearSearch)
            updateSearchResults()
        }
        binding.etSearch.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                when(selectedTab){
                    1->{
                        viewModelLocation.onSearchChangeEvent(binding.etSearch.text.toString().trim())
                    }
                    2->{
                        viewModelTrolling.onSearchChangeEvent(binding.etSearch.text.toString().trim())
                    }
                    3->{
                        viewModelRoute.onSearchChangeEvent(binding.etSearch.text.toString().trim())
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
        binding.ivSearch.setOnSingleClickListener {
            binding.ivSearch.viewGone()
            binding.layoutSearch.viewVisible()
            binding.etSearch.requestFocus()
            binding.etSearch.showKeyboard()
        }
        binding.ivClearSearch.setOnSingleClickListener {
            binding.etSearch.setText("")
            binding.ivSearch.viewVisible()
            binding.layoutSearch.viewGone()
            requireContext().hideKeyboardFrom(binding.ivClearSearch)
            updateSearchResults()
        }
        binding.ivFilter.setOnSingleClickListener {
            val type = when (selectedTab) { // null for all, 0 for my items, 1 for shared items
                1 -> {
                    viewModelLocation.filterType.value
                }

                2 -> {
                    viewModelTrolling.filterType.value
                }

                3 -> {
                    viewModelRoute.filterType.value
                }

                else -> {
                    null
                }
            }
            val dialog =
                PointFilterBottomSheet(type, object : PointFilterBottomSheet.OnApplyFilterListener {
                    override fun onApplyClick(filterType: Int?) {
                        when (selectedTab) {
                            1 -> {
                                //locationFilter = filterType
                                viewModelLocation.onFilterChangeEvent(filterType)
                            }

                            2 -> {
                                //trollingFilter = filterType
                                viewModelTrolling.onFilterChangeEvent(filterType)
                            }

                            3 -> {
                                //routeFilter = filterType
                                viewModelRoute.onFilterChangeEvent(filterType)
                            }
                        }
                        updateSearchResults()
                    }

                })
            dialog.show(childFragmentManager, "PointFilterBottomSheet")
        }
        binding.tvLocations.setOnClickListener {
            if (selectedTab != 1) {
                setSelection(1, binding.tvLocations)
                binding.viewPager.currentItem = selectedTab - 1
            }
        }
        binding.tvTrolling.setOnClickListener {
            if (selectedTab != 2) {
                setSelection(2, binding.tvTrolling)
                binding.viewPager.currentItem = selectedTab - 1
            }
        }
        binding.tvRoutes.setOnClickListener {
            if (selectedTab != 3) {
                setSelection(3, binding.tvRoutes)
                binding.viewPager.currentItem = selectedTab - 1
            }
        }
    }

    private fun applySearch(oldTab: Int, newTab: Int){
        // same search and filter on each tab
        //remove this function if you need to maintain each tab search and filter results separate
        when(oldTab){
            1->{
                when(newTab){
                    2->{
                        if(viewModelLocation.searchText.value != viewModelTrolling.searchText.value || viewModelLocation.filterType.value != viewModelTrolling.filterType.value){
                            viewModelTrolling.onSearchChangeEvent(viewModelLocation.searchText.value)
                            viewModelTrolling.onFilterChangeEvent(viewModelLocation.filterType.value)
                            updateSearchResults()
                        }
                    }
                    3->{
                        if(viewModelLocation.searchText.value != viewModelRoute.searchText.value || viewModelLocation.filterType.value != viewModelRoute.filterType.value){
                            viewModelRoute.onSearchChangeEvent(viewModelLocation.searchText.value)
                            viewModelRoute.onFilterChangeEvent(viewModelLocation.filterType.value)
                            updateSearchResults()
                        }
                    }

                }
            }
            2->{
                when(newTab){
                    1->{
                        if(viewModelLocation.searchText.value != viewModelTrolling.searchText.value || viewModelLocation.filterType.value != viewModelTrolling.filterType.value){
                            viewModelLocation.onSearchChangeEvent(viewModelTrolling.searchText.value)
                            viewModelLocation.onFilterChangeEvent(viewModelTrolling.filterType.value)
                            updateSearchResults()
                        }
                    }
                    3->{
                        if(viewModelTrolling.searchText.value != viewModelRoute.searchText.value || viewModelTrolling.filterType.value != viewModelRoute.filterType.value){
                            viewModelRoute.onSearchChangeEvent(viewModelTrolling.searchText.value)
                            viewModelRoute.onFilterChangeEvent(viewModelTrolling.filterType.value)
                            updateSearchResults()
                        }
                    }

                }
            }
            3->{
                when(newTab){
                    1->{
                        if(viewModelLocation.searchText.value != viewModelRoute.searchText.value || viewModelLocation.filterType.value != viewModelRoute.filterType.value){
                            viewModelLocation.onSearchChangeEvent(viewModelRoute.searchText.value)
                            viewModelLocation.onFilterChangeEvent(viewModelRoute.filterType.value)
                            updateSearchResults()
                        }
                    }
                    2->{
                        if(viewModelRoute.searchText.value != viewModelTrolling.searchText.value || viewModelRoute.filterType.value != viewModelTrolling.filterType.value){
                            viewModelTrolling.onSearchChangeEvent(viewModelRoute.searchText.value)
                            viewModelTrolling.onFilterChangeEvent(viewModelRoute.filterType.value)
                            updateSearchResults()
                        }
                    }
                }
            }

        }
    }

    private fun setSelection(position: Int, tab: TextView) {
        val slideTo = (position - selectedTab) * tab.width
        val translateAnimation = TranslateAnimation(0F, slideTo.toFloat(), 0F, 0F)
        translateAnimation.duration = 100
        when (selectedTab) {
            1 -> {
                binding.tvLocations.startAnimation(translateAnimation)
            }

            2 -> {
                binding.tvTrolling.startAnimation(translateAnimation)
            }

            3 -> {
                binding.tvRoutes.startAnimation(translateAnimation)
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
                binding.tvTrolling.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.secondary400
                    )
                )
                binding.tvRoutes.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.secondary400
                    )
                )

                binding.tvLocations.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.unselected_tab)
                binding.tvTrolling.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.unselected_tab)
                binding.tvRoutes.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.unselected_tab)

                tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                tab.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.selected_tab)
            }

            override fun onAnimationRepeat(p0: Animation?) {

            }

        })
        val oldTab = selectedTab
        selectedTab = position
        applySearch(oldTab,position)
    }

}