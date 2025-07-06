package com.cp.fishthebreak.screens.fragments.home

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.database.MatrixCursor
import android.os.Bundle
import android.provider.BaseColumns
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
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.home.SearchLocationAdapter
import com.cp.fishthebreak.databinding.FragmentSearchLocationBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.models.home.SearchData
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.cp.fishthebreak.utils.Constants
import com.cp.fishthebreak.utils.Constants.Companion.SPECIAL_REFERENCE_4326
import com.cp.fishthebreak.utils.NavigationDirections
import com.cp.fishthebreak.utils.convertCoordinatesToLatLng
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.isValidLatLang
import com.cp.fishthebreak.utils.isValidLatLangNauticalFormat
import com.cp.fishthebreak.utils.isValidLatNauticalFormat
import com.cp.fishthebreak.utils.onSearch
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.home.SearchLocationViewModel
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters
import com.esri.arcgisruntime.tasks.geocode.LocatorTask
import com.esri.arcgisruntime.tasks.geocode.ReverseGeocodeParameters
import com.esri.arcgisruntime.tasks.geocode.SuggestParameters
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchLocationFragment : Fragment() {
    private val TAG: String = SearchLocationFragment::class.java.simpleName
    private lateinit var binding: FragmentSearchLocationBinding
    val viewModel: SearchLocationViewModel by viewModels()
    private val locationList = ArrayList<SearchData>()
    private val historyList = ArrayList<SearchData>()
    private var addressGeocodeParameters: GeocodeParameters? = null
    private var reverseGeocodeParameters: ReverseGeocodeParameters? = null
    private var selectedTab = 1
    private var isPagination = false
    private var sIntent: Intent? = null
    private val navArgs: SearchLocationFragmentArgs by navArgs()
    // create a locator task from an online service
    private val locatorTask: LocatorTask by lazy {
        LocatorTask("https://geocode-api.arcgis.com/arcgis/rest/services/World/GeocodeServer")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSearchLocationBinding.inflate(layoutInflater, container, false)
        if(requireActivity() is NavGraphActivity){
            (requireActivity() as NavGraphActivity).setStatusBarBackgroundTransparent()
        }
        initAdapter()
        initListeners()
        initViewModelResponse()
        return binding.root
    }

    private fun initListeners() {
        binding.latLangButton.setOnClickListener {
            findNavController().navigate(SearchLocationFragmentDirections.actionSearchLocationFragmentToSearchLatLangFragment())
        }
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Pair<String, String>?>("latLang")
            ?.observe(
                viewLifecycleOwner
            ) { result ->
                // Do something with the result.
                if (result != null) {
                    val (latitude, longitude) = result
                    val newText = "$latitude, $longitude"
                    binding.etSearch.setText(newText)
                    searchData(newText, 1)
                }
            }
        binding.tvMapLocations.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.tvMapLocations.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.selected_tab)
        binding.ivBack.setOnClickListener {
            if (requireActivity() is NavGraphActivity) {
                (requireActivity() as NavGraphActivity).onBack()
            }
        }
        binding.tvMapLocations.setOnClickListener {
            if (selectedTab != 1) {
                setSelection(1, binding.tvMapLocations)
            }
        }
        binding.tvFishLog.setOnClickListener {
            if (selectedTab != 2) {
                setSelection(2, binding.tvFishLog)
            }
        }
        binding.tvSavedPoints.setOnClickListener {
            if (selectedTab != 3) {
                setSelection(3, binding.tvSavedPoints)
            }
        }
        setupAddressSearchView()
        setupReverseSearchView()
    }

    private fun setSelection(position: Int, tab: TextView) {
        val slideTo = (position - selectedTab) * tab.width
        val translateAnimation = TranslateAnimation(0F, slideTo.toFloat(), 0F, 0F)
        translateAnimation.duration = 100
        when (selectedTab) {
            1 -> {
                binding.tvMapLocations.startAnimation(translateAnimation)
            }

            2 -> {
                binding.tvFishLog.startAnimation(translateAnimation)
            }

            3 -> {
                binding.tvSavedPoints.startAnimation(translateAnimation)
            }
        }
        translateAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                binding.tvMapLocations.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.secondary400
                    )
                )
                binding.tvFishLog.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.secondary400
                    )
                )
                binding.tvSavedPoints.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.secondary400
                    )
                )

                binding.tvMapLocations.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.unselected_tab)
                binding.tvFishLog.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.unselected_tab)
                binding.tvSavedPoints.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.unselected_tab)

                tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                tab.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.selected_tab)
            }

            override fun onAnimationRepeat(p0: Animation?) {

            }

        })
        selectedTab = position
        locationList.clear()
        binding.rv.adapter?.notifyDataSetChanged()
        when (position) {
            1 -> {
                if(binding.etSearch.text.toString().trim().isNotEmpty()) {
                    lifecycleScope.launch {
                        getSuggestions(binding.etSearch.text.toString().trim())
                    }
                }else{
                    viewModel.getSearchHistory("", 1, page = 1)
                }
            }
            2 -> {
                viewModel.getSearchHistory(binding.etSearch.text.toString().trim(), 2, page = 1)
            }

            3 -> {
                viewModel.getSearchHistory(binding.etSearch.text.toString().trim(), 3, page = 1)
            }
        }
    }

    private fun initAdapter() {
        val mLayoutManager = LinearLayoutManager(requireContext())
        binding.rv.layoutManager = mLayoutManager
        binding.rv.adapter =
            SearchLocationAdapter(locationList, object : SearchLocationAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    sIntent = Intent()
                    sIntent?.putExtra(
                        "searchTextModel",
                        NavigationDirections.GlobalSearch(locationList[position])
                    )
                    viewModel.saveHistory(
                        locationList[position].search_text ?: locationList[position].name?:"",
                        type = selectedTab,
                        id = locationList[position].id
                    )
//                    val sIntent = Intent()
//                    sIntent.putExtra(
//                        "searchTextModel",
//                        NavigationDirections.GlobalSearch(locationList[position])
//                    )
//                    requireActivity().setResult(RESULT_OK, sIntent)
//                    requireActivity().finish()
                }

            })
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
                            searchData(binding.etSearch.text.toString().trim(), null)
                        }
                    }
                }

            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    private fun setupAddressSearchView() {
        addressGeocodeParameters = GeocodeParameters().apply {
            // get place name and street address attributes
            resultAttributeNames.addAll(listOf("PlaceName", "Place_addr"))
            // return only the closest result
            maxResults = 1
            /*binding.etSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    val newText = binding.etSearch.text.toString().trim()
                    if (newText.isNotEmpty()) {
                        when (selectedTab) {
                            1 -> {
                                lifecycleScope.launch {
                                    getSuggestions(newText)
                                }
                            }
//                            2 -> {
//                                viewModel.getSearchHistory(binding.etSearch.text.toString().trim(),2)
//                            }
//                            3 -> {
//                                viewModel.getSearchHistory(binding.etSearch.text.toString().trim(),3)
//                            }
                        }
                    } else {
                        if (selectedTab == 1){
                            viewModel.getSearchHistory("", 1, page = 1)
                        }
                        //addDataFromSearchHistory()
                    }
                }

                override fun afterTextChanged(p0: Editable?) {

                }

            })*/
            binding.etSearch.onSearch {
                val newText = binding.etSearch.text.toString().trim()
                searchData(newText, 1)
            }
        }
    }
    private fun searchData(newText: String, page: Int?){
        if (newText.isNotEmpty()) {
            when (selectedTab) {
                1 -> {
                    lifecycleScope.launch {
                        getSuggestions(newText)
                    }
                }

                2 -> {
                    viewModel.getSearchHistory(binding.etSearch.text.toString().trim(), 2, page = page)
                }

                3 -> {
                    viewModel.getSearchHistory(binding.etSearch.text.toString().trim(), 3, page = page)
                }
            }
        } else {
            when (selectedTab) {
                1 -> {
                    viewModel.getSearchHistory("", 1, page = page)
                }

                2 -> {
                    viewModel.getSearchHistory(binding.etSearch.text.toString().trim(), 2, page = page)
                }

                3 -> {
                    viewModel.getSearchHistory(binding.etSearch.text.toString().trim(), 3, page = page)
                }
            }
        }
    }
    private fun setupReverseSearchView() {
        reverseGeocodeParameters = ReverseGeocodeParameters().apply {
            // get place name and street address attributes
            resultAttributeNames.addAll(listOf("PlaceName", "Place_addr"))
            // return only the closest result
            maxResults = 1
        }
    }

    private fun getSuggestions(newText: String) {
        if(selectedTab != 1){
            return
        }
        if (newText.trim().isEmpty()) {
            addDataFromSearchHistory()
            return
        }
//        if(newText.isValidLatLang()){
        if(newText.isValidLatLangNauticalFormat()){
            geoViewTapped(newText)
            return
        }
        showHideLoader(true)
        //val params = SuggestParameters()
//        params.preferredSearchLocation = Point(31.47, 74.2761111111111, SpatialReference.create(Constants.SPECIAL_REFERENCE_SPECIAL_REFERENCE_4326))
//        params.countryCode = "+92"
        val suggestionsFuture = locatorTask.suggestAsync(newText)
        suggestionsFuture.addDoneListener {
            try {
                if(selectedTab == 1) {
                    showHideLoader(false)
                    // get the results of the async operation
                    val suggestResults = suggestionsFuture.get()
                    locationList.clear()
//                binding.rv.adapter?.notifyDataSetChanged()
                    // add each address suggestion to a new row
                    for ((key, result) in suggestResults.withIndex()) {
                        locationList.add(
                            SearchData(
                                "",
                                null,
                                result.label,
                                "",
                                -1,
                                null,
                                null,
                                null,
                                null,
                                "1"
                            )
                        )
                    }
                    binding.rv.adapter?.notifyDataSetChanged()
                    showHideNoData(locationList.isEmpty())
                }
            } catch (e: Exception) {
                if(selectedTab == 1) {
                    showHideLoader(false)
                    Log.e(TAG, "Geocode suggestion error: " + e.message)
                    requireActivity().showToast("Geocode suggestion error", false)
//                Toast.makeText(
//                    requireContext(),
//                    "Geocode suggestion error",
//                    Toast.LENGTH_LONG
//                )
//                    .show()
                    showHideNoData(locationList.isEmpty())
                }
            }
            if (binding.etSearch.text.toString().trim().isEmpty()) {
                addDataFromSearchHistory()
                return@addDoneListener
            }
        }
    }

    private fun addDataFromSearchHistory() {
        locationList.clear()
        historyList.forEach {
            locationList.add(
                SearchData(
                    it.created_at,
                    it.id,
                    if(it.type == "1"){it.search_text}else{null},
                    it.updated_at,
                    it.user_id,
                    it.description,
                    it.latitude,
                    it.longitude,
                    if(it.type != "1"){it.name?:it.search_text}else{null},
                    it.type
                )
            )
        }
        binding.rv.adapter?.notifyDataSetChanged()
        showHideNoData(locationList.isEmpty())
    }

    private fun initViewModelResponse() {
        lifecycleScope.launch {
            viewModel.searchHistoryResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    isPagination = false
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true) {
                                historyList.clear()
                                historyList.addAll(viewModel.list.value)
                                addDataFromSearchHistory()
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
            viewModel.saveHistoryResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true) {
                                requireActivity().setResult(RESULT_OK, sIntent)
                                requireActivity().finish()
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
    }

//    private fun showHideLoader(visibility: Boolean) {
//        if (visibility) {
//            binding.loaderLayout.viewVisible()
//        } else {
//            binding.loaderLayout.viewGone()
//        }
//        showHideNoData(locationList.isEmpty())
//    }

    private fun showHideLoader(visibility: Boolean) {
        if (visibility) {
            if((viewModel.currentPage.value?:1) == 1) {
                binding.loaderLayout.viewVisible()
            }else{
                binding.paginationLoaderLayout.viewVisible()
            }
        } else {
            binding.loaderLayout.viewGone()
            binding.paginationLoaderLayout.viewGone()
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

    /**
     * Displays a pin of the tapped location using [mapPoint]
     * and finds address with reverse geocode
     */
    private fun geoViewTapped(newText: String) {
        try {
            if (newText.trim().isEmpty()) {
                addDataFromSearchHistory()
                return
            }
//            if (!newText.isValidLatLang()) {
            if (!newText.isValidLatLangNauticalFormat()) {
                addDataFromSearchHistory()
                return
            }
//            val searchLatLang = newText.split(",")
//            if (searchLatLang.size < 2) {
//                return
//            }
//            val lat = searchLatLang.first().trim().toDouble()
//            val lang = searchLatLang[1].trim().toDouble()
            val latLng = convertCoordinatesToLatLng(newText) ?: return
            val (lat, lang) = latLng
            val mapPoint = Point(
                lang,
                lat,
                SpatialReference.create(SPECIAL_REFERENCE_4326)//3857
            )
            // create graphic for tapped point
            // normalize the geometry - needed if the user crosses the international date line.
            val normalizedPoint = GeometryEngine.normalizeCentralMeridian(mapPoint) as Point
            // reverse geocode to get address
            showHideLoader(true)
            val addresses = locatorTask.reverseGeocodeAsync(mapPoint)
            addresses.addDoneListener {
                if (selectedTab == 1) {
                    showHideLoader(false)
                    // get the first result
                    val address = addresses.get().firstOrNull()
                    if (address == null) {
                        locationList.clear()
                        binding.rv.adapter?.notifyDataSetChanged()
                        showHideNoData(locationList.isEmpty())
                        return@addDoneListener
                    }
                    locationList.clear()
                    locationList.add(
                        SearchData(
                            "",
                            null,
                            newText,
                            "",
                            -1,
                            null,
                            null,
                            null,
                            null,
                            "1"
                        )
                    )
                    locationList.add(
                        SearchData(
                            "",
                            null,
                            address.label,
                            "",
                            -1,
                            null,
                            null,
                            null,
                            null,
                            "1"
                        )
                    )
                    binding.rv.adapter?.notifyDataSetChanged()
                    showHideNoData(locationList.isEmpty())
                    if (binding.etSearch.text.toString().trim().isEmpty()) {
                        addDataFromSearchHistory()
                        return@addDoneListener
                    }
                }
//                binding.rv.adapter?.notifyDataSetChanged()
                // add each address suggestion to a new row
//            for ((key, result) in allAddress.withIndex()) {
//                locationList.add(
//                    SearchData(
//                        "",
//                        null,
//                        result.label,
//                        "",
//                        -1,
//                        null,
//                        null,
//                        null,
//                        null,
//                        "1"
//                    )
//                )
//            }
                // use the street and region for the title
                //val title = address.attributes["Address"].toString()
                // use the metro area for the description details
                //val description = "${address.attributes["City"]} " +
                //        "${address.attributes["Region"]} " +
                //        "${address.attributes["CountryCode"]}"
                // set the strings to the text views
            }
        }catch (ex: Exception){
            if (selectedTab == 1) {
                showHideLoader(false)
            }
        }
    }
/*
locationList.clear()
//                binding.rv.adapter?.notifyDataSetChanged()
                // add each address suggestion to a new row
                for ((key, result) in suggestResults.withIndex()) {
                    locationList.add(
                        SearchData(
                            "",
                            null,
                            result.label,
                            "",
                            -1,
                            null,
                            null,
                            null,
                            null,
                            "1"
                        )
                    )
                }
                binding.rv.adapter?.notifyDataSetChanged()
                showHideNoData(locationList.isEmpty())
 */
}