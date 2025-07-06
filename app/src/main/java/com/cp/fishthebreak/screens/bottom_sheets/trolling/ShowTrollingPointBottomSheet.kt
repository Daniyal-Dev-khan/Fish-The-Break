package com.cp.fishthebreak.screens.bottom_sheets.trolling

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.MyViewPagerAdapter
import com.cp.fishthebreak.adapters.routes.SelectedLibraryAdapter
import com.cp.fishthebreak.databinding.FragmentShowTrollingPointBottomSheetBinding
import com.cp.fishthebreak.models.points.SaveFishLogData
import com.cp.fishthebreak.models.points.SavePointsData
import com.cp.fishthebreak.screens.bottom_sheets.routes.SelectLibraryBottomSheet
import com.cp.fishthebreak.screens.fragments.routes.RouteFishLogFragment
import com.cp.fishthebreak.screens.fragments.routes.RoutesLocationFragment
import com.cp.fishthebreak.screens.fragments.trolling.TrollingFishLogFragment
import com.cp.fishthebreak.screens.fragments.trolling.TrollingPointsFragment
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
class ShowTrollingPointBottomSheet(private val trollingId: Int, private val listener: SelectLibraryBottomSheet.OnClickListeners) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentShowTrollingPointBottomSheetBinding
    private var selectedTab = 1
    private lateinit var adapter: MyViewPagerAdapter
    val viewModelLocation: RoutesLocationViewModel by viewModels()
    val viewModelFishLog: RoutesFishLogViewModel by viewModels()
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
        binding = FragmentShowTrollingPointBottomSheetBinding.inflate(layoutInflater,container,false)
        binding.tvLocations.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.tvLocations.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.selected_tab)
        initListeners()
        initViewpager()
        return binding.root
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
            adapter.addFragment(TrollingPointsFragment.newInstance(trollingId))
            adapter.addFragment(TrollingFishLogFragment.newInstance(trollingId))
            binding.viewPager.isUserInputEnabled = false
            binding.viewPager.adapter = adapter
        }
    }

    private fun initListeners() {
        binding.ivCross.setOnClickListener {
            listener.onCancel()
            dismiss()
        }
        binding.tvLocations.setOnClickListener {
            if (selectedTab != 1) {
                setSelection(1, binding.tvLocations)
                binding.viewPager.currentItem = selectedTab - 1
            }
        }
        binding.tvFishLogs.setOnClickListener {
            if (selectedTab != 2) {
                setSelection(2, binding.tvFishLogs)
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
                binding.tvLocations.startAnimation(translateAnimation)
            }

            2 -> {
                binding.tvFishLogs.startAnimation(translateAnimation)
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

                binding.tvLocations.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.unselected_tab)
                binding.tvFishLogs.background =
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
}