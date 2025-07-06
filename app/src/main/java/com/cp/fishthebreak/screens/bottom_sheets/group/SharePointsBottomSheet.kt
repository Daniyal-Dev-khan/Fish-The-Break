package com.cp.fishthebreak.screens.bottom_sheets.group

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
import com.cp.fishthebreak.databinding.FragmentSharePointsBottomSheetBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.screens.fragments.group.ShareFishLogFragment
import com.cp.fishthebreak.screens.fragments.group.ShareLocationFragment
import com.cp.fishthebreak.screens.fragments.group.ShareRouteFragment
import com.cp.fishthebreak.screens.fragments.group.ShareTrollingFragment
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.group.SharePointInGroupViewModel
import com.cp.fishthebreak.viewModels.saved.SavedFishLogViewModel
import com.cp.fishthebreak.viewModels.saved.SavedLocationViewModel
import com.cp.fishthebreak.viewModels.saved.SavedRouteViewModel
import com.cp.fishthebreak.viewModels.saved.SavedTrollingViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
@AndroidEntryPoint
class SharePointsBottomSheet(private val groupId:Int, private val listener: OnClickListeners) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentSharePointsBottomSheetBinding
    private var selectedTab = 1
    private lateinit var adapter: MyViewPagerAdapter
    val viewModelLocation: SavedLocationViewModel by activityViewModels()
    val viewModelFishLog: SavedFishLogViewModel by activityViewModels()
    val viewModelRoute: SavedRouteViewModel by activityViewModels()
    val viewModelTrolling: SavedTrollingViewModel by activityViewModels()
    val viewModelSharePoint: SharePointInGroupViewModel by activityViewModels()
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
        binding = FragmentSharePointsBottomSheetBinding.inflate(layoutInflater,container,false)
        binding.tvFishLogs.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.tvFishLogs.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.selected_tab)
        initListeners()
        initViewpager()
        initViewModelResponse()
        return binding.root
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
            viewModelRoute.routeResponse
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
                            viewModelRoute.resetResponse()
                        }

                        is NetworkResult.Loading -> {

                        }

                        is NetworkResult.NoInternet -> {
                            dialog?.showToast(
                                requireContext(),
                                response.message.getOnErrorMessage(),
                                false
                            )
                            viewModelRoute.resetResponse()
                        }

                        is NetworkResult.NoCallYet -> {
                        }
                    }
                }
        }
        lifecycleScope.launch {
            viewModelTrolling.trollingResponse
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
                            viewModelTrolling.resetResponse()
                        }

                        is NetworkResult.Loading -> {

                        }

                        is NetworkResult.NoInternet -> {
                            dialog?.showToast(
                                requireContext(),
                                response.message.getOnErrorMessage(),
                                false
                            )
                            viewModelTrolling.resetResponse()
                        }

                        is NetworkResult.NoCallYet -> {
                        }
                    }
                }
        }
        lifecycleScope.launch {
            viewModelSharePoint.shareResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            if (response.data?.status == true && response.data.statusCode == 200 && response.data.data?.sent == true) {
                                showHideLoader(false)
                                requireActivity().showToast(
                                    response.data.message, true
                                )
                                listener.onShareClick()
                                viewModelSharePoint.resetResponse()
                                dismiss()
                            } else {
                                showHideLoader(false)
                                dialog?.showToast(
                                    requireContext(),
                                    response.data?.message ?: resources.getString(
                                        R.string.something_went_wrong
                                    ), false
                                )
                                viewModelSharePoint.resetResponse()
                            }
                        }

                        is NetworkResult.Error -> {
                            showHideLoader(false)
                            dialog?.showToast(
                                requireContext(),
                                response.message.getOnErrorMessage(),
                                false
                            )
                            viewModelSharePoint.resetResponse()
                        }

                        is NetworkResult.Loading -> {
                            showHideLoader(true)
                        }

                        is NetworkResult.NoInternet -> {
                            showHideLoader(false)
                            dialog?.showToast(
                                requireContext(),
                                resources.getString(R.string.no_internet),
                                false
                            )
                            viewModelSharePoint.resetResponse()
                        }

                        is NetworkResult.NoCallYet -> {
                        }
                    }
                }
        }
    }

    private fun showHideLoader(visibility: Boolean) {
        if (visibility) {
            binding.loaderLayout.viewVisible()
        } else {
            binding.loaderLayout.viewGone()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    private fun initViewpager() {
        lifecycleScope.launch {
            delay(500)
            binding.viewPager.offscreenPageLimit = 2
            adapter = MyViewPagerAdapter(childFragmentManager, lifecycle)
            adapter.addFragment(ShareFishLogFragment.newInstance(groupId))
            adapter.addFragment(ShareLocationFragment.newInstance(groupId))
            adapter.addFragment(ShareTrollingFragment.newInstance(groupId))
            adapter.addFragment(ShareRouteFragment.newInstance(groupId))
            binding.viewPager.isUserInputEnabled = false
            binding.viewPager.adapter = adapter
        }
    }

    private fun initListeners() {
        binding.btnCross.setOnClickListener {
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
        fun onShareClick()
    }

}