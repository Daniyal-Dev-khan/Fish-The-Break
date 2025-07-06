package com.cp.fishthebreak.screens.fragments.log

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.saved.SavedFishLogAdapter
import com.cp.fishthebreak.databinding.FragmentFishLogBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.cp.fishthebreak.screens.bottom_sheets.ConfirmBottomSheet
import com.cp.fishthebreak.screens.bottom_sheets.PointFilterBottomSheet
import com.cp.fishthebreak.screens.bottom_sheets.group.ShareInGroupBottomSheet
import com.cp.fishthebreak.screens.bottom_sheets.locations.SaveLocationBottomSheet
import com.cp.fishthebreak.utils.Constants
import com.cp.fishthebreak.utils.MapUiData
import com.cp.fishthebreak.utils.NavigationDirections
import com.cp.fishthebreak.utils.PermissionListener
import com.cp.fishthebreak.utils.StartDestination
import com.cp.fishthebreak.utils.checkLocationPermission
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.SyncViewModel
import com.cp.fishthebreak.viewModels.saved.SavedFishLogViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FishLogFragment : Fragment() {
    private lateinit var binding: FragmentFishLogBinding
    val viewModel: SavedFishLogViewModel by viewModels()
    private var isPagination = false
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var typeFilter: Int? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    val syncSharedViewModel: SyncViewModel by activityViewModels()
    private val delayTime = 700L
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
        binding = FragmentFishLogBinding.inflate(layoutInflater, container, false)
        initDataBinding()
        initViewModelResponse()
        initListeners()
        return binding.root
    }

    private fun initListeners() {
        binding.ivAdd.setOnSingleClickListener {
            getLocation()
        }
        binding.ivFilter.setOnSingleClickListener {
            val dialog = PointFilterBottomSheet(typeFilter, object : PointFilterBottomSheet.OnApplyFilterListener {
                    override fun onApplyClick(filterType: Int?) {
                        typeFilter = filterType
                        viewModel.onFilterChangeEvent(filterType)
                        viewModel.getAllSavedFishLogs(1)
                    }
                })
            dialog.show(childFragmentManager, "PointFilterBottomSheet")
        }
    }

    private fun initDataBinding() {
        binding.lifecycleOwner = this@FishLogFragment
        binding.viewModel = viewModel
        val mLayoutManager = LinearLayoutManager(requireContext())
        binding.rv.layoutManager = mLayoutManager
        binding.locationAdapter = SavedFishLogAdapter(listOf(), viewModel)
        var pastVisiblesItems = 0
        var visibleItemCount: Int = 0
        var totalItemCount: Int = 0
        binding.rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    visibleItemCount = mLayoutManager.getChildCount()
                    totalItemCount = mLayoutManager.getItemCount()
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition()
                    if (!isPagination && viewModel.nextPage.value != null) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            isPagination = true
                            viewModel.getAllSavedFishLogs()
                        }
                    }
                }

            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runableTimer)
    }
    private fun initViewModelResponse() {
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
                        viewModel.getAllSavedFishLogs(1)
                    }
                }
        }
        lifecycleScope.launch {
            viewModel.fishLogResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    isPagination = false
                    when (response) {
                        is NetworkResult.Success -> {
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
                            requireActivity().showToast(
                                response.message.getOnErrorMessage(),
                                false
                            )
                            viewModel.resetResponse()
                        }

                        is NetworkResult.Loading -> {

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
            viewModel.deleteResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true) {
                                requireActivity().showToast(
                                    response.data.message , true
                                )
                                binding.rv.adapter?.notifyDataSetChanged()
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
        lifecycleScope.launch {
            viewModel.loadingResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    showHideLoader(response)
                }
        }
        lifecycleScope.launch {
            viewModel.notifyAdapter
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    if (response) {
                        binding.rv.adapter?.notifyDataSetChanged()
                        viewModel.resetNotifyResponse()
                    }
                }
        }
        lifecycleScope.launch {
            viewModel.navigationResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    if(response != null){
                        when(response){
                            is NavigationDirections.Share->{
                                val dialog = ShareInGroupBottomSheet(response)
                                dialog.show(childFragmentManager,"ShareInGroupBottomSheet")
                            }
                            is NavigationDirections.DeletePoints->{
                                if(response.data is MapUiData.FishLogData) {
                                    val dialog = ConfirmBottomSheet(
                                        resources.getString(R.string.confirmation),
                                        resources.getString(R.string.delete_point_alert_delete),
                                        object : ConfirmBottomSheet.OnItemClickListener {
                                            override fun onYesClick() {
                                                viewModel.deleteLocations((response.data.data.id))
                                            }

                                            override fun onCancelClick() {

                                            }

                                        },
                                        positiveButtonText = resources.getString(R.string.remove_))
                                    dialog.show(childFragmentManager, "ConfirmBottomSheet")
                                }
                            }
                            is NavigationDirections.CommonMap->{
                                val sIntent = Intent(requireContext(), NavGraphActivity::class.java)
                                sIntent.putExtra(
                                    Constants.START_DESTINATION,
                                    StartDestination.CommonMap(response.data)
                                )
                                startActivity(sIntent)
                            }
                            is NavigationDirections.ViewPoints->{
                                if(response.data is MapUiData.FishLogData) {
                                    val dialog = SaveLocationBottomSheet(
                                        response.data.data.getLatFromString()?:0.0,
                                        response.data.data.getLangFromString()?:0.0,
                                        isFishLogOnly = true,
                                        data = response.data,
                                        listener = object: SaveLocationBottomSheet.OnClickListener{
                                            override fun onSave() {
                                                binding.rv.adapter?.notifyDataSetChanged()
                                            }

                                        }
                                    )
                                    dialog.show(childFragmentManager, "SaveLocationBottomSheet")
                                }
                            }
                            else->{}
                        }
                        viewModel.resetNavigationResponse()
                    }
                }
        }

    }

    private fun showHideLoader(visibility: Boolean) {
        if (visibility) {
            if (!binding.pullToRefresh.isRefreshing) {
                if(viewModel.list.value.isEmpty()) {
                    binding.loaderLayout.viewVisible()
                }else{
                    binding.paginationLoaderLayout.viewVisible()
                }
            }
        } else {
            binding.loaderLayout.viewGone()
            binding.paginationLoaderLayout.viewGone()
            binding.pullToRefresh.isRefreshing = false
        }
        showHideNoData(viewModel.list.value.isEmpty())
    }

    private fun showHideNoData(visibility: Boolean) {
        if (visibility) {
            binding.noDataLayout.viewVisible()
        } else {
            binding.noDataLayout.viewGone()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        showHideLoader(true)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        requireActivity().checkLocationPermission(childFragmentManager,
            object : PermissionListener {
                override fun onPermissionGranted() {
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        object : CancellationToken() {
                            override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                                CancellationTokenSource().token

                            override fun isCancellationRequested() = false
                        })
                        .addOnSuccessListener { location: Location? ->
                            showHideLoader(false)
                            if (location == null)
                                Toast.makeText(
                                    requireContext(),
                                    "Cannot get location.",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            else {
                                latitude = location.latitude
                                longitude = location.longitude
                                val dialog = SaveLocationBottomSheet(latitude, longitude, isFishLogOnly = true, listener = object: SaveLocationBottomSheet.OnClickListener{
                                    override fun onSave() {
                                        viewModel.getAllSavedFishLogs(1)
                                    }

                                })
                                dialog.show(childFragmentManager, "SaveLocationBottomSheet")
                            }

                        }
                        .addOnCanceledListener {
                            showHideLoader(false)
                        }
                        .addOnFailureListener {
                            showHideLoader(false)
                        }
                }

                override fun onPermissionCancel() {
                    showHideLoader(false)
                }

            })
    }

}