package com.cp.fishthebreak.screens.fragments.save

import android.content.Intent
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.saved.SavedRouteAdapter
import com.cp.fishthebreak.adapters.saved.SavedTrollingAdapter
import com.cp.fishthebreak.databinding.FragmentSavedRoutesBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.models.routes.MeasureDistanceModel
import com.cp.fishthebreak.models.routes.SaveRoutePoint
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.cp.fishthebreak.screens.bottom_sheets.ConfirmBottomSheet
import com.cp.fishthebreak.screens.bottom_sheets.group.ShareInGroupBottomSheet
import com.cp.fishthebreak.screens.bottom_sheets.routes.SaveRouteBottomSheet
import com.cp.fishthebreak.utils.Constants
import com.cp.fishthebreak.utils.KmToNauticalMiles
import com.cp.fishthebreak.utils.MapUiData
import com.cp.fishthebreak.utils.NavigationDirections
import com.cp.fishthebreak.utils.StartDestination
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.saved.SavedRouteViewModel
import com.esri.arcgisruntime.mapping.Viewpoint
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
@AndroidEntryPoint
class SavedRoutesFragment : Fragment() {
    private lateinit var binding: FragmentSavedRoutesBinding
    val viewModel: SavedRouteViewModel by activityViewModels()
    private var isPagination = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSavedRoutesBinding.inflate(layoutInflater,container,false)
        initDataBinding()
        initViewModelResponse()
        return binding.root
    }

    private fun initDataBinding() {
        binding.lifecycleOwner = this@SavedRoutesFragment
        binding.viewModel = viewModel
        val mLayoutManager = LinearLayoutManager(requireContext())
        binding.rv.layoutManager = mLayoutManager
        binding.locationAdapter = SavedRouteAdapter(listOf(), viewModel)
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
                            viewModel.getAllSavedRoute()
                        }
                    }
                }

            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    private fun initViewModelResponse() {
        lifecycleScope.launch {
            viewModel.routeResponse
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
                    if(response){
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
                            is NavigationDirections.CommonMap->{
                                val sIntent = Intent(requireContext(), NavGraphActivity::class.java)
                                sIntent.putExtra(
                                    Constants.START_DESTINATION,
                                    StartDestination.CommonMap(response.data)
                                )
                                startActivity(sIntent)
                            }
                            is NavigationDirections.DeletePoints->{
                                if(response.data is MapUiData.RouteData) {
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
                            is NavigationDirections.Share->{
                                val dialog = ShareInGroupBottomSheet(response)
                                dialog.show(childFragmentManager,"ShareInGroupBottomSheet")
                            }
                            is NavigationDirections.ViewPoints-> {
                                if (response.data is MapUiData.RouteData) {
                                    val dialog = SaveRouteBottomSheet(createListMeasureDistance(response.data.data.points),object: SaveRouteBottomSheet.OnClickListener{
                                        override fun onClose() {
                                            binding.rv.adapter?.notifyDataSetChanged()
                                        }

                                    },data = response.data.data)
                                    dialog.show(childFragmentManager,"SaveRouteBottomSheet")
                                }
                            }
                            else->{}
                        }
                        viewModel.resetNavigationResponse()
                    }
                }
        }
    }

    private fun createListMeasureDistance(list: List<SaveRoutePoint>): ArrayList<MeasureDistanceModel>{
        val dataList = ArrayList<MeasureDistanceModel>()
        var totaldistance = 0F
        list.forEachIndexed { index, item ->
            if(index+1 < list.size) {
                val results = FloatArray(1)
                Location.distanceBetween(
                    item.getLatFromString()?:0.0,
                    item.getLangFromString()?:0.0,
                    list[index+1].getLatFromString()?:0.0,
                    list[index+1].getLangFromString()?:0.0,
                    results
                )
                totaldistance += (results[0]/1000F)
                val distance = if(results.isNotEmpty()){
                    "${String.format("%.2f", (results[0]/1000).KmToNauticalMiles())}NM"
                }else{
                    ""
                }
                dataList.add(MeasureDistanceModel(item, list[index + 1],distance))
            }
        }
        return dataList
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

}