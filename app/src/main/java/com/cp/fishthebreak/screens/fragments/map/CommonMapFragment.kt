package com.cp.fishthebreak.screens.fragments.map

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.routes.MeasureDistanceAdapter
import com.cp.fishthebreak.databinding.FragmentCommonMapBinding
import com.cp.fishthebreak.databinding.GenerateOfflineMapDialogLayoutBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.models.home.SearchData
import com.cp.fishthebreak.models.map.MapLayer
import com.cp.fishthebreak.models.map.WmsLayersStatusModel
import com.cp.fishthebreak.models.points.SaveFishLogData
import com.cp.fishthebreak.models.points.SavePointsData
import com.cp.fishthebreak.models.routes.MeasureDistanceModel
import com.cp.fishthebreak.models.routes.SaveRoutePoint
import com.cp.fishthebreak.models.trolling.TrollingResponseData
import com.cp.fishthebreak.screens.activities.AuthActivity
import com.cp.fishthebreak.screens.activities.BaseActivityResult
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.cp.fishthebreak.screens.bottom_sheets.CreateMapBottomSheet
import com.cp.fishthebreak.screens.bottom_sheets.LayerBottomSheet
import com.cp.fishthebreak.screens.bottom_sheets.LayerOpacityBottomSheet
import com.cp.fishthebreak.screens.bottom_sheets.locations.SaveLocationBottomSheet
import com.cp.fishthebreak.screens.bottom_sheets.routes.SelectLibraryBottomSheet
import com.cp.fishthebreak.screens.bottom_sheets.trolling.ShowTrollingPointBottomSheet
import com.cp.fishthebreak.screens.fragments.home.HomeFragment.Companion.sharedLayerViewModel
import com.cp.fishthebreak.utils.Constants
import com.cp.fishthebreak.utils.Constants.Companion.DEFAULT_LAYER_OPACITY
import com.cp.fishthebreak.utils.Constants.Companion.Feature_Type
import com.cp.fishthebreak.utils.Constants.Companion.SPECIAL_REFERENCE_3857
import com.cp.fishthebreak.utils.Constants.Companion.SPECIAL_REFERENCE_4326
import com.cp.fishthebreak.utils.Constants.Companion.Self_Hosted_Type
import com.cp.fishthebreak.utils.Constants.Companion.TILE
import com.cp.fishthebreak.utils.Constants.Companion.WMS_TYPE
import com.cp.fishthebreak.utils.GPSCheck
import com.cp.fishthebreak.utils.KmToNauticalMiles
import com.cp.fishthebreak.utils.MapUiData
import com.cp.fishthebreak.utils.NavigationDirections
import com.cp.fishthebreak.utils.PermissionListener
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.cp.fishthebreak.utils.StartDestination
import com.cp.fishthebreak.utils.checkLocationPermission
import com.cp.fishthebreak.utils.convertCoordinatesToLatLng
import com.cp.fishthebreak.utils.getMapDirectory
import com.cp.fishthebreak.utils.getNauticalLatitude
import com.cp.fishthebreak.utils.getNauticalLongitude
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.getTrollingTimeInSeconds
import com.cp.fishthebreak.utils.hideKeyboardFrom
import com.cp.fishthebreak.utils.isNetworkAvailable
import com.cp.fishthebreak.utils.isValidLatLangNauticalFormat
import com.cp.fishthebreak.utils.nauticalMilesToMiles
import com.cp.fishthebreak.utils.roundOffDecimal
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.cp.fishthebreak.utils.showSnack
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.map.LayersViewModel
import com.cp.fishthebreak.viewModels.map.MapRouteViewModel
import com.cp.fishthebreak.viewModels.map.MapTrollingViewModel
import com.cp.fishthebreak.viewModels.map.OfflineMapViewModel
import com.cp.fishthebreak.viewModels.profile.locations.LocationViewModel
import com.cp.fishthebreak.viewModels.profile.preference.PreferenceViewModel
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.concurrent.Job
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.GeodeticCurveType
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.LinearUnit
import com.esri.arcgisruntime.geometry.LinearUnitId
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.PointCollection
import com.esri.arcgisruntime.geometry.Polyline
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.WmsLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Callout
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.security.AuthenticationChallenge
import com.esri.arcgisruntime.security.AuthenticationChallengeHandler
import com.esri.arcgisruntime.security.AuthenticationChallengeResponse
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.UserCredential
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.esri.arcgisruntime.symbology.TextSymbol
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult
import com.esri.arcgisruntime.tasks.geocode.LocatorTask
import com.esri.arcgisruntime.tasks.offlinemap.GenerateOfflineMapJob
import com.esri.arcgisruntime.tasks.offlinemap.GenerateOfflineMapParameters
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapTask
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date
import java.util.concurrent.ExecutionException
import javax.inject.Inject
import kotlin.math.roundToInt


@AndroidEntryPoint
class CommonMapFragment : Fragment() {
    private var identifiedGraphic: Graphic? = null
    private val TAG: String = CommonMapFragment::class.java.simpleName
    private lateinit var binding: FragmentCommonMapBinding
    private val navArgs: CommonMapFragmentArgs by navArgs()
    private var addressGeocodeParameters: GeocodeParameters? = null
    private var callout: Callout? = null
    // create a locator task from an online service
    private val locatorTask: LocatorTask by lazy {
        LocatorTask("https://geocode-api.arcgis.com/arcgis/rest/services/World/GeocodeServer")
    }
    @Inject
    lateinit var sharePreferenceHelper: SharePreferenceHelper
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double? = null
    private var longitude: Double? = null
    private val activityLauncher: BaseActivityResult<Intent, ActivityResult> =
        BaseActivityResult.registerActivityForResult(this)
    private val activeLayers: HashMap<String, WmsLayersStatusModel> = HashMap()
    private var isLocationSaveUiActive = false
    private val handler: Handler = Handler(Looper.getMainLooper())
    private var isTrollingPlaying = false
    private var trollingDuration = 0L
    val viewModelOfflineMap: OfflineMapViewModel by viewModels()
    private var tempDirectoryPath: String? = null
    private var trollingTimerDelay = 300L
    private var trollingResponse: TrollingResponseData? = null
    private var trollingPointIndex = 0
    private fun createMapPath(){
        val date = Date().time
        viewModelOfflineMap.onDateChangeEvent(date)
        tempDirectoryPath = requireContext().getMapDirectory(date)?.path
        Log.i("mapPath", tempDirectoryPath?:"")
    }
    private fun playTrolling(progress: Int){
        lifecycleScope.launch {
            if (progress < (trollingResponse?.trolling_point?.size ?: 0)) {
                trollingPointIndex = progress
                addMarkerOnMap(
                    trollingResponse?.trolling_point?.get(progress)?.getLatFromString() ?: 0.0,
                    trollingResponse?.trolling_point?.get(progress)?.getLangFromString() ?: 0.0
                )
                binding.mapView.setViewpoint(
                    Viewpoint(
                        trollingResponse?.trolling_point?.get(progress)?.getLatFromString()
                            ?: 34.0270,
                        trollingResponse?.trolling_point?.get(progress)?.getLangFromString()
                            ?: -118.8050,
                        Constants.MAP_SCALE_Zoom
//                        72000.0
                    )
                )
            }
        }
    }
    val runableTimer = object : Runnable {
        override fun run() {
            if(binding.seekBar.progress < (trollingResponse?.trolling_point?.size?:0)) {
                binding.seekBar.progress =  binding.seekBar.progress + 1
                if(trollingPointIndex < (trollingResponse?.trolling_point?.size?:0)) {
                    addMarkerOnMap(
                        trollingResponse?.trolling_point?.get(trollingPointIndex)?.getLatFromString()?: 0.0,
                        trollingResponse?.trolling_point?.get(trollingPointIndex)?.getLangFromString() ?: 0.0
                    )
                    binding.mapView.setViewpoint(
                        Viewpoint(
                            trollingResponse?.trolling_point?.get(trollingPointIndex)?.getLatFromString() ?: 34.0270,
                            trollingResponse?.trolling_point?.get(trollingPointIndex)?.getLangFromString() ?: -118.8050,
                            Constants.MAP_SCALE_Zoom
//                        72000.0
                        )
                    )
                    trollingPointIndex++
                }
                handler.postDelayed(this, trollingTimerDelay)// 1 seconds
            }else{
                trollingPointIndex = 0
                if(trollingPointIndex < (trollingResponse?.trolling_point?.size?:0)) {
                    addMarkerOnMap(
                        trollingResponse?.trolling_point?.get(trollingPointIndex)?.getLatFromString() ?: 0.0,
                        trollingResponse?.trolling_point?.get(trollingPointIndex)?.getLangFromString() ?: 0.0
                    )
                    binding.mapView.setViewpoint(
                        Viewpoint(
                            trollingResponse?.trolling_point?.get(trollingPointIndex)?.getLatFromString() ?: 34.0270,
                            trollingResponse?.trolling_point?.get(trollingPointIndex)?.getLangFromString() ?: -118.8050,
                            Constants.MAP_SCALE_Zoom
//                        72000.0
                        )
                    )
                    trollingPointIndex++
                }
                isTrollingPlaying = false
                binding.seekBar.progress = 0
                binding.ivPlayPause.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_play_white))
                handler.removeCallbacks(this)
            }
        }
        /*
        override fun run() {
            if(binding.seekBar.progress < trollingDuration) {
                binding.seekBar.progress =  binding.seekBar.progress + 1
                if(trollingPointIndex < (trollingResponse?.trolling_point?.size?:0)) {
                    addMarkerOnMap(
                        trollingResponse?.trolling_point?.get(trollingPointIndex)?.getLatFromString()?: 0.0,
                        trollingResponse?.trolling_point?.get(trollingPointIndex)?.getLangFromString() ?: 0.0
                    )
                    binding.mapView.setViewpoint(
                        Viewpoint(
                            trollingResponse?.trolling_point?.get(trollingPointIndex)?.getLatFromString() ?: 34.0270,
                            trollingResponse?.trolling_point?.get(trollingPointIndex)?.getLangFromString() ?: -118.8050,
                            Constants.MAP_SCALE_Zoom
//                        72000.0
                        )
                    )
                    trollingPointIndex++
                }
                handler.postDelayed(this, trollingTimerDelay)// 1 seconds
            }else{
                trollingPointIndex = 0
                if(trollingPointIndex < (trollingResponse?.trolling_point?.size?:0)) {
                    addMarkerOnMap(
                        trollingResponse?.trolling_point?.get(trollingPointIndex)?.getLatFromString() ?: 0.0,
                        trollingResponse?.trolling_point?.get(trollingPointIndex)?.getLangFromString() ?: 0.0
                    )
                    binding.mapView.setViewpoint(
                        Viewpoint(
                            trollingResponse?.trolling_point?.get(trollingPointIndex)?.getLatFromString() ?: 34.0270,
                            trollingResponse?.trolling_point?.get(trollingPointIndex)?.getLangFromString() ?: -118.8050,
                            Constants.MAP_SCALE_Zoom
//                        72000.0
                        )
                    )
                    trollingPointIndex++
                }
                isTrollingPlaying = false
                binding.seekBar.progress = 0
                binding.ivPlayPause.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_play_white))
                handler.removeCallbacks(this)
            }
        }
         */

    }

    private var isMapLoaded = false

    // create a new Graphics Overlay
    private val graphicsOverlay: GraphicsOverlay by lazy { GraphicsOverlay() }
    private val graphicsOverlayOffline: GraphicsOverlay by lazy { GraphicsOverlay() }
    private val graphicsOverlayLocation: GraphicsOverlay by lazy { GraphicsOverlay() }
    private val graphicsOverlayTrollingData: GraphicsOverlay by lazy { GraphicsOverlay() }

    // create a picture marker symbol
    private val pinSourceSymbol: PictureMarkerSymbol? by lazy { createPinSymbol(R.drawable.ic_navigation) }//ic_start_point_trolling
    private val pinSourceLocationSymbol: PictureMarkerSymbol? by lazy { createLocationPinSymbol(R.drawable.ic_location_pin_icon) }
    private val pinSourceFishLogSymbol: PictureMarkerSymbol? by lazy { createLocationPinSymbol(R.drawable.ic_fish_library_bg_png, width = 34f, height = 34f) }
    private val startTrollingSymbol: PictureMarkerSymbol? by lazy { createLocationPinSymbol(R.drawable.ic_start_point) }
    private val endTrollingSymbol: PictureMarkerSymbol? by lazy { createLocationPinSymbol(R.drawable.ic_end_point) }
    private val pinSourceBoatLocationSymbol: PictureMarkerSymbol? by lazy { createLocationPinSymbol(R.drawable.ic_boat_location_pin_home, width = 44f, height = 44f) }

    val viewModel: LayersViewModel by activityViewModels()
    val savedTrollingViewModel: MapTrollingViewModel by viewModels()
    val viewModelPreference: PreferenceViewModel by viewModels()
    val viewModelRoute: MapRouteViewModel by viewModels()
    val viewModelLocation: LocationViewModel by viewModels()
    private var measureDistanceList: ArrayList<MeasureDistanceModel> = ArrayList()

    //Boat range
    private var mapPoint: Point? = null
    private val geodesicGraphicsOverlay: GraphicsOverlay by lazy {  GraphicsOverlay()}
    private val planarGraphicsOverlay: GraphicsOverlay by lazy {  GraphicsOverlay()}
    private val tapLocationsOverlay: GraphicsOverlay by lazy {  GraphicsOverlay()}
    private val geodesicGraphicsOverlayInner : GraphicsOverlay by lazy {  GraphicsOverlay()}
    private val planarGraphicsOverlayInner : GraphicsOverlay by lazy {  GraphicsOverlay()}
    private val tapLocationsOverlayInner : GraphicsOverlay by lazy {  GraphicsOverlay()}
    private val downloadArea: Graphic by lazy { Graphic() }
    private val boatPinSourceSymbol: PictureMarkerSymbol? by lazy { createPinSymbol(R.drawable.ic_pin_new) }
    private var locationData: SavePointsData? = null
    private var fishLogData: SaveFishLogData? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCommonMapBinding.inflate(layoutInflater, container, false)
        if(requireActivity() is NavGraphActivity){
            (requireActivity() as NavGraphActivity).setStatusBarBackgroundTransparent()
        }
        showHideLoader(true)
        initListeners()
        if(requireContext().isNetworkAvailable()) {
            getLocation()
        }else{
            showHideLoader(false)
        }
        initViewModelResponse()
        initBoatRangeDataBinding()
        requireActivity().registerReceiver(
            GPSCheck(object : GPSCheck.LocationCallBack {
                override fun turnedOn() {
                    Log.d("GpsReceiver", "is turned on")
                    if (latitude != null) {
                        binding.mapView.viewVisible()
                        binding.ivLayers.isClickable = true
                        binding.locationPermissionLayout.viewGone()
                    }
                }

                override fun turnedOff() {
                    Log.d("GpsReceiver", "is turned off")
                    Log.i("onPermissionCancel", "location")
                    binding.ivLayers.isClickable = false
                    binding.mapView.viewGone()
                    binding.locationPermissionLayout.viewVisible()
                    showHideLoader(false)
                }
            }),
            IntentFilter(LocationManager.MODE_CHANGED_ACTION),
        )
        return binding.root
    }

    private fun initBoatRangeDataBinding(){
        when(navArgs.data){
            is MapUiData.BoatRange -> {
                binding.backButton.viewGone()
                binding.backButton1.viewVisible()
                binding.tvTitle.viewVisible()
                binding.lifecycleOwner = this
                binding.model = viewModelPreference
                initBoatRangeViewModelResponse()
                if((navArgs.data as MapUiData.BoatRange).showBoatRangeControls){
                    binding.bottomLayoutBoatRange.viewVisible()
//                    if((navArgs.data as MapUiData.BoatRange).saveBoatRangeToServer) {
//                        binding.btnLayerOnOff.viewVisible()
//                    }else{
//                        binding.btnLayerOnOff.viewGone()
//                    }
                    binding.btnLayerOnOff.viewVisible()
                    binding.saveLocationButton.viewGone()
                    binding.tvTitle.text = resources.getString(R.string.boat_range)
                }else{
                    val params: ConstraintLayout.LayoutParams = binding.ivLayers.layoutParams as ConstraintLayout.LayoutParams
                    params.goneTopMargin = resources.getDimension(R.dimen.margin_top_115_sdp).toInt()
                    binding.layoutSearch.viewVisible()
                    binding.bottomLayoutBoatRange.viewGone()
                    binding.btnLayerOnOff.viewGone()
                    binding.saveLocationButton.viewVisible()
                    binding.tvTitle.text = resources.getString(R.string.dock_location)
                    binding.layoutLatLong.viewVisible()
                    binding.saveLocationButton.setOnClickListener {
                        if(latitude != null && longitude != null) {
                            val sIntent = Intent()
                            sIntent.putExtra("dockLocation", "${getNauticalLatitude(latitude?:0.0)} , ${getNauticalLongitude(longitude?:0.0)}")
                            requireActivity().setResult(Activity.RESULT_OK,sIntent)
                            requireActivity().finish()
//                            findNavController().previousBackStackEntry?.savedStateHandle?.set(
//                                "dockLocation",
//                                "$latitude , $longitude"
//                            )
//                            findNavController().popBackStack()
                        }else{
                            requireActivity().showToast(resources.getString(R.string.error_vessel_location),false)
                        }
                    }
                }
                binding.btnLayerOnOff.setOnClickListener {view->
                    if((navArgs.data as MapUiData.BoatRange).saveBoatRangeToServer){
                        viewModelPreference.onToggleChangeEvent(binding.btnLayerOnOff.isChecked)
                    }else{
                        viewModelPreference.toggleBoatRange(view,binding.btnLayerOnOff.isChecked)
                    }
                }
                binding.saveButton.setOnClickListener {
                    if(viewModelPreference.preferenceUIStates.value.range in 1..2000) {
                        if (latitude != null && longitude != null && !(navArgs.data as MapUiData.BoatRange).saveBoatRangeToServer) {
                            val sIntent = Intent()
                            sIntent.putExtra(
                                "boatRange",
                                viewModelPreference.preferenceUIStates.value.range.toString()
                            )
                            requireActivity().setResult(Activity.RESULT_OK, sIntent)
                            requireActivity().finish()
//                        findNavController().previousBackStackEntry?.savedStateHandle?.set(
//                            "boatRange",
//                            viewModelPreference.preferenceUIStates.value.range.toString()
//                        )
//                        findNavController().popBackStack()
                        } else {
                            viewModelPreference.onSaveClickedEvent(binding.saveButton)
                        }
                    }else{
                        requireContext().hideKeyboardFrom(binding.saveButton)
                        requireActivity().showToast(resources.getString(R.string.boat_range_limit_error),false)
                    }
                }
                binding.backButton1.setOnClickListener {
                    if(requireActivity() is NavGraphActivity){
                        (requireActivity() as NavGraphActivity).onBack()
                    }
                }
//                binding.ivCurrentLocation.setOnSingleClickListener {
//                    getLocation()
//                }
                binding.etRange.addTextChangedListener(object: TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        if(!p0.isNullOrEmpty()) {
                            //binding.seekBar.progress = p0.toString().toInt()
                            if(mapPoint != null) {
                                drawCircleOnMap()
                            }
                        }else{
                            planarGraphicsOverlay.graphics.clear()
                            geodesicGraphicsOverlay.graphics.clear()
                            tapLocationsOverlay.graphics.clear()
                            planarGraphicsOverlayInner.graphics.clear()
                            geodesicGraphicsOverlayInner.graphics.clear()
                            tapLocationsOverlayInner.graphics.clear()
                            viewModelPreference.onRangeChangeEvent(0)
                        }
                    }

                    override fun afterTextChanged(p0: Editable?) {

                    }

                })
                binding.ivClearSearch.setOnSingleClickListener {
                    binding.tvSearch.text = ""
                    binding.ivClearSearch.viewGone()
                    graphicsOverlay.graphics.clear()
                    binding.mapView.callout.dismiss()
                }
                binding.layoutSearch.setOnSingleClickListener {
                    val sIntent = Intent(requireContext(), NavGraphActivity::class.java)
                    sIntent.putExtra(Constants.START_DESTINATION, StartDestination.SearchLocations(isFromRoute = false))
                    activityLauncher.launch(
                        sIntent,
                        object :
                            BaseActivityResult.OnActivityResult<ActivityResult> {
                            @SuppressLint("NotifyDataSetChanged")
                            override fun onActivityResult(result: ActivityResult) {
                                if (result.resultCode == Activity.RESULT_OK) {
                                    if (result.data != null) {
                                        if (result.data?.hasExtra("searchTextModel") == true) {
                                            val searchModel =
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                    result.data?.getParcelableExtra(
                                                        "searchTextModel",
                                                        NavigationDirections::class.java
                                                    )
                                                } else {
                                                    result.data?.getParcelableExtra("searchTextModel") as NavigationDirections?
                                                }
                                            if (searchModel is NavigationDirections.GlobalSearch) {
                                                if (searchModel.data.type == "1") {
                                                    if (addressGeocodeParameters == null) {
                                                        setupAddressSearchView(
                                                            searchModel.data.search_text
                                                                ?: searchModel.data.name ?: ""
                                                        )
                                                    } else {
                                                        geoCodeTypedAddress(
                                                            searchModel.data.search_text
                                                                ?: searchModel.data.name ?: ""
                                                        )
                                                    }
                                                } else {
                                                    displaySearchResultOnMap(searchModel.data)
                                                }
                                                binding.tvSearch.text =
                                                    searchModel.data.search_text ?: searchModel.data.name
                                                            ?: ""
                                                binding.ivClearSearch.viewVisible()
                                            }
                                        }
                                    }
                                }
                            }
                        })
                }
            }
            is MapUiData.GetLocationFromMap -> {
                val params: ConstraintLayout.LayoutParams = binding.mapView.layoutParams as ConstraintLayout.LayoutParams
                params.goneTopMargin = 0
                binding.ivLayers
                binding.layoutLocation.viewVisible()
                binding.layoutLatLong.viewVisible()
                binding.backButton.viewGone()
                binding.layoutSearch1.viewVisible()
                //binding.backButton2.viewVisible()
                //binding.tvTitle.viewVisible()
                binding.lifecycleOwner = this
                binding.model = viewModelPreference
                binding.bottomLayoutBoatRange.viewGone()
                binding.btnLayerOnOff.viewGone()
                binding.saveLocationButton1.viewVisible()
                binding.tvTitle.text = ""
                binding.tvTitle.viewGone()
                binding.buttonSaveLocation.setOnClickListener {
                    if (latitude != null && longitude != null) {
                        val sIntent = Intent()
                        sIntent.putExtra(
                            "latitude",
                            latitude
                        )
                        sIntent.putExtra(
                            "longitude",
                            longitude
                        )
                        requireActivity().setResult(Activity.RESULT_OK, sIntent)
                        requireActivity().finish()
                    } else{
                        requireActivity().showToast(resources.getString(R.string.error_select_location),false)
                    }
                }
                binding.ivCrossLocation.setOnClickListener {
                    if(requireActivity() is NavGraphActivity){
                        (requireActivity() as NavGraphActivity).onBack()
                    }
                }
//                binding.ivCurrentLocation.setOnSingleClickListener {
//                    getLocation()
//                }
                binding.ivClearSearch1.setOnSingleClickListener {
                    binding.tvSearch1.text = ""
                    binding.ivClearSearch1.viewGone()
                    graphicsOverlay.graphics.clear()
                    binding.mapView.callout.dismiss()
                }
                binding.layoutSearch1.setOnSingleClickListener {
                    val sIntent = Intent(requireContext(), NavGraphActivity::class.java)
                    sIntent.putExtra(Constants.START_DESTINATION, StartDestination.SearchLocations(isFromRoute = false))
                    activityLauncher.launch(
                        sIntent,
                        object :
                            BaseActivityResult.OnActivityResult<ActivityResult> {
                            @SuppressLint("NotifyDataSetChanged")
                            override fun onActivityResult(result: ActivityResult) {
                                if (result.resultCode == Activity.RESULT_OK) {
                                    if (result.data != null) {
                                        if (result.data?.hasExtra("searchTextModel") == true) {
                                            val searchModel =
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                    result.data?.getParcelableExtra(
                                                        "searchTextModel",
                                                        NavigationDirections::class.java
                                                    )
                                                } else {
                                                    result.data?.getParcelableExtra("searchTextModel") as NavigationDirections?
                                                }
                                            if (searchModel is NavigationDirections.GlobalSearch) {
                                                if (searchModel.data.type == "1") {
                                                    if (addressGeocodeParameters == null) {
                                                        setupAddressSearchView(
                                                            searchModel.data.search_text
                                                                ?: searchModel.data.name ?: ""
                                                        )
                                                    } else {
                                                        geoCodeTypedAddress(
                                                            searchModel.data.search_text
                                                                ?: searchModel.data.name ?: ""
                                                        )
                                                    }
                                                } else {
                                                    displaySearchResultOnMap(searchModel.data)
                                                }
                                                binding.tvSearch1.text =
                                                    searchModel.data.search_text ?: searchModel.data.name
                                                            ?: ""
                                                binding.ivClearSearch1.viewVisible()
                                            }
                                        }
                                    }
                                }
                            }
                        })
                }
            }
            else->{}
        }
    }

    private fun initViews(){
        when(navArgs.data){
            is MapUiData.FishLogData -> {
                fishLogData = (navArgs.data as MapUiData.FishLogData).data
                binding.buttonViewDetails.viewVisible()
                createFishLogUi((navArgs.data as MapUiData.FishLogData).data)
            }
            is MapUiData.LocationData -> {
                locationData = (navArgs.data as MapUiData.LocationData).data
                binding.buttonViewDetails.viewVisible()
                createLocationUi((navArgs.data as MapUiData.LocationData).data)
            }
            is MapUiData.RouteData -> {
                measureDistance((navArgs.data as MapUiData.RouteData).data.points)
            }
            is MapUiData.TrollingData -> {
                savedTrollingViewModel.getTrollingById((navArgs.data as MapUiData.TrollingData).data.id)
                initTrollingViewModelResponse()
            }
            is MapUiData.FromMessage -> {
                when ((navArgs.data as MapUiData.FromMessage).type) {
                    "trolling" -> {
                        savedTrollingViewModel.getTrollingById((navArgs.data as MapUiData.FromMessage).pointId)
                        initTrollingViewModelResponse()
                    }
                    "fish_log" -> {
                        viewModelLocation.getFishLogById((navArgs.data as MapUiData.FromMessage).pointId)
                        //createFishLogUi((navArgs.data as MapUiData.FromMessage).lat, (navArgs.data as MapUiData.FromMessage).lang)
                        initLocationViewModelResponse()
                        binding.buttonViewDetails.viewVisible()
                    }
                    "location" -> {
                        viewModelLocation.getLocationById((navArgs.data as MapUiData.FromMessage).pointId)
                        //createLocationUi((navArgs.data as MapUiData.FromMessage).lat, (navArgs.data as MapUiData.FromMessage).lang)
                        initLocationViewModelResponse()
                        binding.buttonViewDetails.viewVisible()
                    }
                    "route" -> {
                        viewModelRoute.getSingleRouteById((navArgs.data as MapUiData.FromMessage).pointId)
                        initRouteViewModelResponse()
                    }
                }
            }
            is MapUiData.BoatRange -> {
                if((navArgs.data as MapUiData.BoatRange).showBoatRangeControls){
                    mapPoint = Point((navArgs.data as MapUiData.BoatRange).longitude?:longitude?:0.0,
                        (navArgs.data as MapUiData.BoatRange).latitude?:latitude?:0.0, SpatialReference.create(SPECIAL_REFERENCE_4326))
                    initCircleOnMap()
                    drawCircleOnMap()
                    if((navArgs.data as MapUiData.BoatRange).latitude != null && (navArgs.data as MapUiData.BoatRange).longitude != null){
                        addMultipleLocationMarkerOnMap(
                            (navArgs.data as MapUiData.BoatRange).latitude?:0.0,
                            (navArgs.data as MapUiData.BoatRange).longitude?:0.0,
                            pinSourceBoatLocationSymbol
                        )
                    }
                    binding.mapView.setViewpointCenterAsync(
                        mapPoint,
                        Constants.MAP_SCALE
                    )
                }else{
                    mapPoint = Point((navArgs.data as MapUiData.BoatRange).longitude?:longitude?:0.0,
                        (navArgs.data as MapUiData.BoatRange).latitude?:latitude?:0.0, SpatialReference.create(SPECIAL_REFERENCE_4326))
                    addBoatMarkerOnMap((navArgs.data as MapUiData.BoatRange).latitude?:latitude?:0.0,(navArgs.data as MapUiData.BoatRange).longitude?:longitude?:0.0)
                    if((navArgs.data as MapUiData.BoatRange).latitude != null && (navArgs.data as MapUiData.BoatRange).longitude != null){
                        addMultipleLocationMarkerOnMap(
                            (navArgs.data as MapUiData.BoatRange).latitude?:0.0,
                            (navArgs.data as MapUiData.BoatRange).longitude?:0.0,
                            pinSourceBoatLocationSymbol
                        )
                    }
                    binding.mapView.setViewpointCenterAsync(
                        mapPoint,
                        Constants.MAP_SCALE
                    )
                    //initMapTouchListener()
                    binding.mapView.addViewpointChangedListener { p0 ->
                        // clear the current graphic position
                        graphicsOverlayLocation.graphics.clear()
                        // create a screen point from where the user tapped
                        val viewPoint = p0?.source?.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE)
                        val screenPoint = Point(
                            viewPoint?.targetGeometry?.extent?.center?.x ?: 0.0,
                            viewPoint?.targetGeometry?.extent?.center?.y ?: 0.0,
                            SpatialReference.create(SPECIAL_REFERENCE_3857)
                        )
                        val projectedPoint = GeometryEngine.project(screenPoint,SpatialReference.create(SPECIAL_REFERENCE_4326))
                        // create a map point from the screen point
                        val locationPoint = projectedPoint as Point
                        longitude = locationPoint.x
                        latitude = locationPoint.y
                        binding.tvLatitude.text = getNauticalLatitude(latitude?:0.0)
                        binding.tvLongitude.text = getNauticalLongitude(longitude?:0.0)
                        val graphic = Graphic(projectedPoint, boatPinSourceSymbol)
                        graphicsOverlayLocation.graphics?.add(graphic)
                    }
                }
            }
            is MapUiData.GetLocationFromMap -> {
                val user = sharePreferenceHelper.getUser()
                if(user?.vessel?.getLatFromString() != null && user.vessel?.getLangFromString() != null) {
                    addMultipleLocationMarkerOnMap(
                        lat = user.vessel?.getLatFromString() ?: 0.0,
                        lang = user.vessel?.getLangFromString() ?: 0.0,
                        pinSourceBoatLocationSymbol
                    )
                }
                mapPoint = Point((navArgs.data as MapUiData.GetLocationFromMap).longitude?:longitude?:0.0,
                    (navArgs.data as MapUiData.GetLocationFromMap).latitude?:latitude?:0.0, SpatialReference.create(SPECIAL_REFERENCE_4326))
                addBoatMarkerOnMap((navArgs.data as MapUiData.GetLocationFromMap).latitude?:latitude?:0.0,(navArgs.data as MapUiData.GetLocationFromMap).longitude?:longitude?:0.0)
                binding.mapView.setViewpointCenterAsync(
                    mapPoint,
                    Constants.MAP_SCALE
                )
                showMeasureDistance((navArgs.data as MapUiData.GetLocationFromMap).pointsList)
                //initMapTouchListener()
                binding.mapView.addViewpointChangedListener { p0 ->
                    // clear the current graphic position
                    graphicsOverlayLocation.graphics.clear()
                    // create a screen point from where the user tapped
                    val viewPoint = p0?.source?.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE)
                    val screenPoint = Point(
                        viewPoint?.targetGeometry?.extent?.center?.x ?: 0.0,
                        viewPoint?.targetGeometry?.extent?.center?.y ?: 0.0,
                        SpatialReference.create(SPECIAL_REFERENCE_3857)
                    )
                    val projectedPoint = GeometryEngine.project(screenPoint,SpatialReference.create(SPECIAL_REFERENCE_4326))
                    // create a map point from the screen point
                    val locationPoint = projectedPoint as Point
                    longitude = locationPoint.x
                    latitude = locationPoint.y
                    binding.tvLatitude.text = getNauticalLatitude(latitude?:0.0)
                    binding.tvLongitude.text = getNauticalLongitude(longitude?:0.0)
                    val graphic = Graphic(projectedPoint, boatPinSourceSymbol)
                    graphicsOverlayLocation.graphics?.add(graphic)
                }
            }
            is MapUiData.OfflineMap -> {
                binding.saveMapButton.viewVisible()
                if((navArgs.data as MapUiData.OfflineMap).mapPath.isNullOrEmpty()) {
                    binding.tvTitleMap.viewVisible()
                    binding.saveMapButton.setOnClickListener {
                        val dialog = CreateMapBottomSheet(name = viewModelOfflineMap.offlineMapUIStates.value.name, description = viewModelOfflineMap.offlineMapUIStates.value.description,object: CreateMapBottomSheet.OnClickListener{
                            override fun onSave(name: String, description: String) {
                                createMapPath()
                                viewModelOfflineMap.onNameChangeEvent(name)
                                viewModelOfflineMap.onDescriptionChangeEvent(description)
                                viewModelOfflineMap.onSaveClickEvent(null)
                                generateOfflineMap()
                            }

                        })
                        dialog.show(childFragmentManager,"CreateMapBottomSheet")
                    }
                }
            }
            else->{}
        }
    }

    /**
     * Create a progress dialog box for tracking the generate offline map job.
     *
     * @param job the generate offline map job progress to be tracked
     * @return an AlertDialog set with the dialog layout view
     */
    private fun createProgressDialog(job: GenerateOfflineMapJob): AlertDialog {
        val builder = AlertDialog.Builder(requireContext()).apply {
            setTitle("Downloading map")
            // provide a cancel button on the dialog
            setNegativeButton("Cancel") { _, _ ->
                job.cancelAsync()
            }
            setCancelable(false)
            val dialogLayoutBinding = GenerateOfflineMapDialogLayoutBinding.inflate(layoutInflater)
            setView(dialogLayoutBinding.root)
        }
        return builder.create()
    }

    /**
     * Use the generate offline map job to generate an offline map.
     *
     * @param view: the button which calls this function
     */
    private fun generateOfflineMap() {
        if(tempDirectoryPath == null){
            return
        }
        // delete any offline map already in the cache
        File(tempDirectoryPath?:"").deleteRecursively()

        // specify the extent, min scale, and max scale as parameters
        var minScale: Double = binding.mapView.mapScale
        val maxScale: Double = binding.mapView.map.maxScale
        // minScale must always be larger than maxScale
        if (minScale <= maxScale) {
            minScale = maxScale + 1
        }

        val generateOfflineMapParameters = GenerateOfflineMapParameters(
            downloadArea.geometry, minScale, maxScale
        ).apply {
            // set job to cancel on any errors
            isContinueOnErrors = true
        }
        // create an offline map task with the map
        val offlineMapTask = OfflineMapTask(binding.mapView.map)

        // create an offline map job with the download directory path and parameters and start the job
        val offlineMapJob =
            offlineMapTask.generateOfflineMap(generateOfflineMapParameters, tempDirectoryPath?:"")

        // create an alert dialog to show the download progress
        val progressDialogLayoutBinding = GenerateOfflineMapDialogLayoutBinding.inflate(layoutInflater)
        val progressDialog = createProgressDialog(offlineMapJob)
        progressDialog.setView(progressDialogLayoutBinding.root)
        progressDialog.show()

        offlineMapJob.apply {
            // link the progress bar to the job's progress
            addProgressChangedListener {
                progressDialogLayoutBinding.progressBar.progress = progress
                progressDialogLayoutBinding.progressTextView.text = "${progress}%"
            }

            // replace the current map with the result offline map when the job finishes
            addJobDoneListener {
                if (status == Job.Status.SUCCEEDED) {
                    val result = result

                    // disable and remove the button to take the map offline once the offline map is showing
                    binding.saveMapButton.isEnabled = false
                    binding.saveMapButton.visibility = View.GONE
                    binding.tvTitleMap.visibility = View.GONE
                    binding.ivLayers.visibility = View.GONE
                    binding.ivCurrentLocation.visibility = View.GONE
                    val mapImage = binding.mapView.exportImageAsync()
                    mapImage.addDoneListener {
                        val image = mapImage.get()
                        val imgPath = (tempDirectoryPath?:"")+"/img_${viewModelOfflineMap.offlineMapUIStates.value.mapDate}.png"
                        val imageFile = File(imgPath)
                        try {
                            val fos: FileOutputStream = FileOutputStream(imageFile)
                            image.compress(Bitmap.CompressFormat.PNG, 90, fos)
                            fos.close()
                            viewModelOfflineMap.onImageChangeEvent(imgPath)
                        } catch (e: FileNotFoundException) {
                            Log.d(TAG, "File not found: " + e.message)
                        } catch (e: IOException) {
                            Log.d(TAG, "Error accessing file: " + e.message)
                        }
                        binding.mapView.map = result.offlineMap
                        graphicsOverlayOffline.graphics.clear()
                        viewModelOfflineMap.onPathChangeEvent(tempDirectoryPath?:"")
                        viewModelOfflineMap.updateMap()
                        Toast.makeText(
                            requireContext(),
                            "Now displaying offline map.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
//                    viewModel.onPathChangeEvent(tempDirectoryPath?:"")
//                    viewModel.updateMap()
//                    Toast.makeText(
//                        requireContext(),
//                        "Now displaying offline map.",
//                        Toast.LENGTH_LONG
//                    ).show()
                } else {
                    viewModelOfflineMap.deleteInCompleteMap()
                    //Log.e(TAG, offlineMapJob.error.additionalMessage)
                    //Server job has failed. ERROR 001564: Requested tile count(25730223) exceeds the maximum allowed number of tiles(150000) to be exported for service Elevation/World_Hillshade:MapServer. Job j12328f16e7044e51a058a630657ba04c not found in Job Registry or the registry record is unavailable.
                    if(offlineMapJob.error.additionalMessage.lowercase().contains("exceeds the maximum allowed number of tiles")){
                        val errorList = offlineMapJob.error.additionalMessage.split(":")
                        if(errorList.size > 1){
                            val errorPart = errorList[1]
                            val errorMessageList = errorPart.split("to be exported")
                            if(errorMessageList.isNotEmpty()){
                                val errorMessage = errorMessageList.first()+" to be exported. Please chose smaller area"
                                //requireActivity().showToast(errorMessage.trim(),false)
                                showError(errorMessage.trim())
                            }else{
                                val error =
                                    "Error in generate offline map job: " + offlineMapJob.error.message
                                val error1 =
                                    "Error in generate offline map job: " + offlineMapJob.error.cause?.message
                                if(offlineMapJob.error.additionalMessage.isEmpty()){
                                    requireActivity().showToast(
                                        error,
                                        false
                                    )
                                }else {
                                    requireActivity().showToast(
                                        offlineMapJob.error.additionalMessage,
                                        false
                                    )
                                }
                                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                                Log.e(TAG, error)
                                Log.e(TAG, error1)
                            }
                        }else{
                            val error =
                                "Error in generate offline map job: " + offlineMapJob.error.message
                            val error1 =
                                "Error in generate offline map job: " + offlineMapJob.error.cause?.message
                            if(offlineMapJob.error.additionalMessage.isEmpty()){
                                requireActivity().showToast(
                                    error,
                                    false
                                )
                            }else {
                                requireActivity().showToast(
                                    offlineMapJob.error.additionalMessage,
                                    false
                                )
                            }
                            Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                            Log.e(TAG, error)
                            Log.e(TAG, error1)
                        }
                    }else{
                        val error =
                            "Error in generate offline map job: " + offlineMapJob.error.message
                        val error1 =
                            "Error in generate offline map job: " + offlineMapJob.error.cause?.message
                        if(offlineMapJob.error.additionalMessage.isEmpty()){
                            requireActivity().showToast(
                                error,
                                false
                            )
                        }else {
                            requireActivity().showToast(
                                offlineMapJob.error.additionalMessage,
                                false
                            )
                        }
                        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                        Log.e(TAG, error)
                        Log.e(TAG, error1)
                    }
                    //Log.e(TAG, offlineMapJob.error.errorCode.toString())
                }
                // close the progress dialog
                progressDialog.dismiss()
            }
            // start the job
            start()
        }
    }

    private fun showError(message: String) {
        Log.e("HomeFragment", message)
        binding.mapView.showSnack(message)
//        Snackbar.make(binding.mapView, message, Snackbar.LENGTH_INDEFINITE).apply {
//            setAction(resources.getString(R.string.dismiss)) {
//                dismiss()
//            }
//            setActionTextColor(resources.getColor(android.R.color.holo_red_light, null))
//            show()
//        }
    }

    private fun createLocationUi(data: SavePointsData){
        addLocationMarkerOnMap(
            data.getLatFromString() ?: 0.0,
            data.getLangFromString() ?: 0.0
        )
        binding.mapView.setViewpoint(
            Viewpoint(
                data.getLatFromString() ?: 34.0270,
                data.getLangFromString() ?: -118.8050,
                Constants.MAP_SCALE
//                        72000.0
            )
        )
    }
    private fun createLocationUi(lat: Double?, lang: Double?){
        addLocationMarkerOnMap(
            lat ?: 0.0,
            lang ?: 0.0
        )
        binding.mapView.setViewpoint(
            Viewpoint(
                lat ?: 34.0270,
                lang ?: -118.8050,
                Constants.MAP_SCALE
//                        72000.0
            )
        )
    }
    private fun createFishLogUi(data: SaveFishLogData){
        addFishLogMarkerOnMap(
            data.getLatFromString() ?: 0.0,
            data.getLangFromString() ?: 0.0
        )
        binding.mapView.setViewpoint(
            Viewpoint(
                data.getLatFromString() ?: latitude?:0.0,
                data.getLangFromString() ?: longitude?:0.0,
                Constants.MAP_SCALE
//                        72000.0
            )
        )
    }

    private fun createFishLogUi(lat: Double?, lang: Double?){
        addFishLogMarkerOnMap(
            lat ?: 0.0,
            lang ?: 0.0
        )
        binding.mapView.setViewpoint(
            Viewpoint(
                lat ?: 34.0270,
                lang ?: -118.8050,
                Constants.MAP_SCALE
//                        72000.0
            )
        )
    }

    private fun createTrollingUi(data: TrollingResponseData){
        lifecycleScope.launch {
            graphicsOverlayLocation.graphics.clear()
            graphicsOverlayOffline.graphics.clear()
            graphicsOverlay.graphics.clear()
            graphicsOverlayTrollingData.graphics.clear()
            binding.seekBar.isEnabled = true
            isTrollingPlaying = false
            trollingDuration = data.duration?.getTrollingTimeInSeconds()?:0L
            binding.seekBar.min = 0
            //binding.seekBar.max = trollingDuration.toInt()
            binding.seekBar.max = if((data.trolling_point.size)> 0){(data.trolling_point.size)}else {1}
            binding.seekBar.progress = 0
            if(data.trolling_point.isNotEmpty()) {
//                trollingTimerDelay = (30000 / data.trolling_point.size).toLong()
                trollingTimerDelay = 300
            }else{
                trollingTimerDelay = 0
            }
            //handler.postDelayed(runableTimer, trollingTimerDelay)
            binding.layoutTrolling.viewVisible()
            binding.trollingBottomLayout.viewVisible()
            binding.buttonViewSavedPoints.viewVisible()
            binding.tvTrollingName.text = data.trolling_name
            binding.tvTrollingDate.text = data.getDateFormat()
            binding.tvTrollingTime.text = data.duration
            binding.tvTopSpeedValue.text = data.highest_speed
            binding.tvTopTempValue.text = data.highest_water_temp
            binding.tvLowTempValue.text = data.lowest_water_temp
            binding.tvDistanceValue.text = data.distance
            // create points for the line
            // create points for the line
            //SpatialReference.create(4326) //4326 is for Geographic coordinate systems (GCSs)
            val points = PointCollection(SpatialReference.create(SPECIAL_REFERENCE_4326))
            data.trolling_point.forEachIndexed { index, it ->
                points.add(it.getLangFromString() ?: 0.0, it.getLatFromString()?: 0.0)
                if(index == 0) {
                    addMultipleLocationMarkerOnMap(
                        it.getLatFromString() ?: 0.0,
                        it.getLangFromString()  ?: 0.0,
                        startTrollingSymbol
                    )
//                    addMarkerOnMap(
//                        it.latitude?.toDouble() ?: 0.0,
//                        it.longitude?.toDouble() ?: 0.0
//                    )
                    binding.mapView.setViewpoint(
                        Viewpoint(
                            it.getLatFromString() ?: 0.0,
                            it.getLangFromString() ?: 0.0,
                            Constants.MAP_SCALE_Zoom
//                        72000.0
                        )
                    )
                }
                else if(index == data.trolling_point.size - 1) {
//                    addMultipleMarkerOnMap(
//                        it.latitude?.toDouble() ?: 0.0,
//                        it.longitude?.toDouble() ?: 0.0
//                    )
                    addMultipleLocationMarkerOnMap(
                        it.getLatFromString() ?: 0.0,
                        it.getLangFromString() ?: 0.0,
                        endTrollingSymbol
                    )
                }
            }
            data.locations.forEach { locations->
                addMultipleLocationMarkerOnMap(
                    locations.getLatFromString() ?: 0.0,
                    locations.getLangFromString() ?: 0.0
                )
            }
            data.fishlogs.forEach { locations->
                addMultipleFishLogMarkerOnMap(
                    locations.getLatFromString() ?: 0.0,
                    locations.getLangFromString() ?: 0.0
                )
            }
//        points.add(-226913.0, 6550477.0)
//        points.add(-226643.0, 6550477.0)
            val line = Polyline(points)

            // creates a solid red simple line symbol

            // creates a solid red simple line symbol
            val lineSymbol =
                SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.parseColor("#2DA0FA"), 6f)

            // add line with symbol to graphics overlay and add overlay to map view
            // add line with symbol to graphics overlay and add overlay to map view
            graphicsOverlay.graphics.add(Graphic(line, lineSymbol))
            binding.mapView.setViewpointGeometryAsync(graphicsOverlay.extent, Constants.MAP_PADDING)
            binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        playTrolling(progress)
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                    isTrollingPlaying = false
                    handler.removeCallbacks(runableTimer)
                    binding.ivPlayPause.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_play_white))

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }

            })
        }
        binding.buttonViewSavedPoints.setOnClickListener {
            val dialog = ShowTrollingPointBottomSheet(data.id, object: SelectLibraryBottomSheet.OnClickListeners{
                override fun onCancel() {

                }

                override fun onNextClick(list: List<Any>) {

                }

            })
            dialog.show(childFragmentManager,"SelectLibraryBottomSheet")
        }
    }

    private fun applyLayerOpacity(data: MapLayer){
        val dialog = LayerOpacityBottomSheet(data.layer_name,data.opacity ?: DEFAULT_LAYER_OPACITY.toInt(),
            object : LayerOpacityBottomSheet.LayerFilterClickListeners {
                override fun onApplyFilter(opacityValue: Int) {
                    data.opacity = opacityValue
                    if( viewModel.layerToggleResponse.value.containsKey(data.layer_calling_name)) {
                        val wmsLayer = viewModel.layerToggleResponse.value[data.layer_calling_name]
                        if (isMapLoaded && wmsLayer != null) {
                            if(data.self_hosted == 0 && wmsLayer.layer is WmsLayer) {
                                wmsLayer.layer.opacity = (wmsLayer.data.opacity?.toFloat()?: DEFAULT_LAYER_OPACITY)/100F
                            }else if(wmsLayer.data.self_hosted == 1 && wmsLayer.layer is FeatureLayer){
                                wmsLayer.layer.opacity = (wmsLayer.data.opacity?.toFloat()?:DEFAULT_LAYER_OPACITY)/100F
                            }else if(wmsLayer.data.self_hosted == 0 && wmsLayer.layer is FeatureLayer){
                                wmsLayer.layer.opacity = (wmsLayer.data.opacity?.toFloat()?:DEFAULT_LAYER_OPACITY)/100F
                            }
                            viewModel.saveDataInSharedPreference()
                        }
                    }
                }

                override fun onApplyFilterClick(opacityValue: Int) {
                    if(requireActivity() is NavGraphActivity){
                        data.opacity = opacityValue
                        if( viewModel.layerToggleResponse.value.containsKey(data.layer_calling_name)) {
                            val wmsLayer = viewModel.layerToggleResponse.value[data.layer_calling_name]
                            if (isMapLoaded && wmsLayer != null) {
                                if(data.self_hosted == 0 && wmsLayer.layer is WmsLayer) {
                                    wmsLayer.layer.opacity = (wmsLayer.data.opacity?.toFloat()?:DEFAULT_LAYER_OPACITY)/100F
                                }else if(wmsLayer.data.self_hosted == 1 && wmsLayer.layer is FeatureLayer){
                                    wmsLayer.layer.opacity = (wmsLayer.data.opacity?.toFloat()?:DEFAULT_LAYER_OPACITY)/100F
                                }else if(wmsLayer.data.self_hosted == 0 && wmsLayer.layer is FeatureLayer){
                                    wmsLayer.layer.opacity = (wmsLayer.data.opacity?.toFloat()?:DEFAULT_LAYER_OPACITY)/100F
                                }
                                viewModel.saveDataInSharedPreference()
                            }
                        }
                        sharedLayerViewModel?.refreshAllLayers()
                    }
                }

            })
        dialog.show(childFragmentManager, "LayerOpacityBottomSheet")
    }

    private fun initListeners() {
        binding.backButton.setOnClickListener {
            if(requireActivity() is NavGraphActivity){
                (requireActivity() as NavGraphActivity).onBack()
            }
        }
        binding.ivCurrentLocation.setOnSingleClickListener {
            if(isMapLoaded){
                if (binding.mapView.locationDisplay != null) {
                    if (binding.mapView.locationDisplay.location != null) {
                        if (binding.mapView.locationDisplay.location.position != null) {
                            val point =
                                binding.mapView.locationDisplay.location.position
                            // Zoom to current location with magnification 1000.
                            binding.mapView.setViewpointCenterAsync(
                                point,
                                Constants.CURRENT_LOCATION_SCALE
                            )
                            Log.d("Latitude", "${point.x}")
                            Log.d("Longitude", "${point.y}")
                        }
                    }
                }
            }else{
                getLocation()
            }
        }
        binding.locationPermissionButton.setOnSingleClickListener {
            getLocation()
        }
        binding.ivLayers.setOnSingleClickListener {
            val dialog = LayerBottomSheet(object : LayerBottomSheet.OnBottomSheetDismissListener {
                override fun onDismiss() {
                    viewModel.layerToggleResponse.value.forEach { (key, wmsLayer) ->
                        activeLayers.put(key, wmsLayer)
                        Log.e("load portal item ", wmsLayer.data.layer_calling_name)
                        if (isMapLoaded) {
                            if(wmsLayer.data.layer_type == WMS_TYPE && wmsLayer.layer is WmsLayer) {
                                binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                wmsLayer.layer.opacity = (wmsLayer.data.opacity?.toFloat()?:DEFAULT_LAYER_OPACITY)/100F
                                wmsLayer.layer.isVisible = wmsLayer.data.isSelected
                                addLayerOnMap(wmsLayer.layer)
                            }else if(wmsLayer.data.layer_type == Self_Hosted_Type && wmsLayer.layer is FeatureLayer){
                                binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                wmsLayer.layer.opacity = (wmsLayer.data.opacity?.toFloat()?:DEFAULT_LAYER_OPACITY)/100F
                                wmsLayer.layer.isVisible = wmsLayer.data.isSelected
                                addLayerOnMapFromPortalId(wmsLayer.layer)
                            }
                            else if(wmsLayer.data.layer_type == Feature_Type && wmsLayer.layer is FeatureLayer){
                                binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                wmsLayer.layer.opacity = (wmsLayer.data.opacity?.toFloat()?:DEFAULT_LAYER_OPACITY)/100F
                                wmsLayer.layer.isVisible = wmsLayer.data.isSelected
                                addLayerOnMapFromPortalId(wmsLayer.layer)
                            }
                            else if (wmsLayer.data.layer_type == TILE && wmsLayer.layer is ArcGISTiledLayer) {
                                binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                wmsLayer.layer.opacity =
                                    (wmsLayer.data.opacity?.toFloat() ?: DEFAULT_LAYER_OPACITY) / 100F
                                wmsLayer.layer.isVisible = wmsLayer.data.isSelected
                                addLayerOnMapFromTile(wmsLayer.layer)
                            }
                        }
                    }
                    viewModel.saveDataInSharedPreference()
                    if(requireActivity() is NavGraphActivity){
                        sharedLayerViewModel?.refreshAllLayers()
                    }
                }

                override fun onFilterApply(data: MapLayer) {
                    applyLayerOpacity(data)
                }

                override fun onViewAll() {
                    val sIntent = Intent(requireContext(),NavGraphActivity::class.java)
                    sIntent.putExtra(Constants.START_DESTINATION, StartDestination.ApplyLayers())
                    activityLauncher.launch(
                        sIntent,
                        object :
                            BaseActivityResult.OnActivityResult<ActivityResult> {
                            @SuppressLint("NotifyDataSetChanged")
                            override fun onActivityResult(result: ActivityResult) {
                                //viewModel.saveDataInSharedPreference()
                                if(requireActivity() is NavGraphActivity){
                                    sharedLayerViewModel?.refreshAllLayers()
                                    viewModel.refreshAllLayers()
                                    viewModel.loadLayersFromSharedPreference()
                                    viewModel.getRecentLayers()
                                    /*viewModel.layerToggleResponse.value.forEach { (key, wmsLayer) ->
                                        activeLayers.put(key, wmsLayer)
                                        Log.e("load portal item ", wmsLayer.data.layer_calling_name)
                                        if (isMapLoaded) {
                                            if(wmsLayer.data.layer_type == WMS_TYPE && wmsLayer.layer is WmsLayer) {
                                                binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                                wmsLayer.layer.opacity = (wmsLayer.data.opacity?.toFloat()?:DEFAULT_LAYER_OPACITY)/100F
                                                wmsLayer.layer.isVisible = wmsLayer.data.isSelected
                                                addLayerOnMap(wmsLayer.layer)
                                            }else if(wmsLayer.data.layer_type == Self_Hosted_Type && wmsLayer.layer is FeatureLayer){
                                                binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                                wmsLayer.layer.opacity = (wmsLayer.data.opacity?.toFloat()?:DEFAULT_LAYER_OPACITY)/100F
                                                wmsLayer.layer.isVisible = wmsLayer.data.isSelected
                                                addLayerOnMapFromPortalId(wmsLayer.layer)
                                            }
                                            else if(wmsLayer.data.layer_type == Feature_Type && wmsLayer.layer is FeatureLayer){
                                                binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                                wmsLayer.layer.opacity = (wmsLayer.data.opacity?.toFloat()?:DEFAULT_LAYER_OPACITY)/100F
                                                wmsLayer.layer.isVisible = wmsLayer.data.isSelected
                                                addLayerOnMapFromPortalId(wmsLayer.layer)
                                            }
                                            else if (wmsLayer.data.layer_type == TILE && wmsLayer.layer is ArcGISTiledLayer) {
                                                binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                                wmsLayer.layer.opacity =
                                                    (wmsLayer.data.opacity?.toFloat() ?: DEFAULT_LAYER_OPACITY) / 100F
                                                wmsLayer.layer.isVisible = wmsLayer.data.isSelected
                                                addLayerOnMapFromTile(wmsLayer.layer)
                                            }
                                        }
                                    }*/
                                }
                                if (result.resultCode == Activity.RESULT_OK) {
                                    if (result.data != null) {
                                        if (result.data?.hasExtra("layer_name") == true) {
                                            val layerCallingName = result.data?.getStringExtra("layer_name")?:""
                                            if (viewModel.layerToggleResponse.value.containsKey(layerCallingName)) {
                                                val wmsLayer = viewModel.layerToggleResponse.value[layerCallingName]
                                                wmsLayer?.let { data->applyLayerOpacity(data.data) }
                                            }
                                        }
                                    }
                                }
                            }
                        })
                }

            })
            dialog.show(childFragmentManager, "LayerBottomSheet")
        }

        binding.ivExpand.setOnClickListener {
            if(binding.expandable.isExpanded){
                binding.ivExpand.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_arrow_down))
            }else{
                binding.ivExpand.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_arrow_up))
            }
            binding.expandable.toggle()
        }
        binding.ivExpandAttributes.setOnClickListener {
            if(binding.expandableAttributes.isExpanded){
                binding.ivExpand.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_arrow_down))
            }else{
                binding.ivExpandAttributes.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_arrow_up))
            }
            binding.expandableAttributes.toggle()
        }
        binding.ivPlayPause.setOnSingleClickListener {
            if(!isTrollingPlaying){
                handler.postDelayed(runableTimer, trollingTimerDelay)
                binding.ivPlayPause.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_pause_white))
            }else{
                handler.removeCallbacks(runableTimer)
                binding.ivPlayPause.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_play_white))
            }
            isTrollingPlaying = !isTrollingPlaying
        }
        binding.buttonViewDetails.setOnClickListener {
            locationData?.let { data->
                val dialog = SaveLocationBottomSheet(
                    data.getLatFromString()?:0.0,
                    data.getLangFromString()?:0.0,
                    data = MapUiData.LocationData(data),
                    disableEditing = true
                )
                dialog.show(childFragmentManager, "SaveLocationBottomSheet")
            }
            fishLogData?.let { data->
                val dialog = SaveLocationBottomSheet(
                    data.getLatFromString()?:0.0,
                    data.getLangFromString()?:0.0,
                    data = MapUiData.FishLogData(data),
                    disableEditing = true
                )
                dialog.show(childFragmentManager, "SaveLocationBottomSheet")
            }
        }
    }

    private fun showMeasureDistance(list: List<SearchData>) {
        graphicsOverlay.graphics.clear()
        graphicsOverlayLocation.graphics.clear()
        // create points for the line
        // create points for the line
        //SpatialReference.create(SPECIAL_REFERENCE_4326) //4326 is for Geographic coordinate systems (GCSs)
        val points = PointCollection(SpatialReference.create(SPECIAL_REFERENCE_4326))
        list.forEachIndexed { index, it ->
            if (index == 0) {
                binding.mapView.setViewpoint(
                    Viewpoint(
                        it.getLatFromString()?: 0.0,
                        it.getLangFromString() ?: 0.0,
                        Constants.MAP_SCALE
                    )
                )
            }
            points.add(it.getLangFromString() ?: 0.0, it.getLatFromString() ?: 0.0)
            addMultipleMarkerOnMapRoute(
                it.getLatFromString() ?: 0.0,
                it.getLangFromString() ?: 0.0,
                "Point ${index+1}"
            )
        }
//        points.add(-226913.0, 6550477.0)
//        points.add(-226643.0, 6550477.0)
        val line = Polyline(points)

        // creates a solid red simple line symbol

        // creates a solid red simple line symbol
        val lineSymbol =
            SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.parseColor("#2DA0FA"), 6f)

        // add line with symbol to graphics overlay and add overlay to map view

        // add line with symbol to graphics overlay and add overlay to map view
        graphicsOverlay.graphics.add(Graphic(line, lineSymbol))
        binding.mapView.setViewpointGeometryAsync(graphicsOverlay.extent, Constants.MAP_PADDING)
    }
    private fun measureDistance(list: List<SaveRoutePoint>){
        lifecycleScope.launch {
            // create points for the line
            // create points for the line
            //SpatialReference.create(SPECIAL_REFERENCE_4326) //4326 is for Geographic coordinate systems (GCSs)
            val points = PointCollection(SpatialReference.create(SPECIAL_REFERENCE_4326))
            list.forEachIndexed { index, it ->
                points.add(it.getLangFromString() ?: 0.0, it.getLatFromString() ?: 0.0)
                addMultipleMarkerOnMap(
                    it.getLatFromString() ?: 0.0,
                    it.getLangFromString() ?: 0.0,
                    "Point ${index+1}"
                )
            }
//        points.add(-226913.0, 6550477.0)
//        points.add(-226643.0, 6550477.0)
            val line = Polyline(points)

            // creates a solid red simple line symbol

            // creates a solid red simple line symbol
            val lineSymbol =
                SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.parseColor("#2DA0FA"), 6f)

            // add line with symbol to graphics overlay and add overlay to map view
            // add line with symbol to graphics overlay and add overlay to map view
            graphicsOverlay.graphics.add(Graphic(line, lineSymbol))
            binding.mapView.setViewpointGeometryAsync(graphicsOverlay.extent, Constants.MAP_PADDING)
            createUiForMeasureDistance(list)
        }
    }

    private fun showCalloutRoute(graphic: Graphic, text: String) {
        val centerOfGraphic = graphic.geometry.extent.center
        val calloutLocation = graphic.computeCalloutLocation(centerOfGraphic, binding.mapView)
        val ts = TextSymbol(
            14f,
            text,
            Color.parseColor("#000000"),
            TextSymbol.HorizontalAlignment.RIGHT,
            TextSymbol.VerticalAlignment.BOTTOM
        )
        ts.backgroundColor = Color.parseColor("#FFFFFF")
        val currentTextGraphic = Graphic(calloutLocation, ts)
        graphicsOverlayLocation.graphics.add(currentTextGraphic)
    }
    private fun showCalloutRouteRoute(graphic: Graphic, text: String) {
        val centerOfGraphic = graphic.geometry.extent.center
        val calloutLocation = graphic.computeCalloutLocation(centerOfGraphic, binding.mapView)
        val ts = TextSymbol(
            14f,
            text,
            Color.parseColor("#000000"),
            TextSymbol.HorizontalAlignment.RIGHT,
            TextSymbol.VerticalAlignment.BOTTOM
        )
        ts.backgroundColor = Color.parseColor("#FFFFFF")
        val currentTextGraphic = Graphic(calloutLocation, ts)
        graphicsOverlayTrollingData.graphics.add(currentTextGraphic)
    }

    private fun createUiForMeasureDistance(list: List<SaveRoutePoint>){
        val dataList = ArrayList<MeasureDistanceModel>()
        var totaldistance = 0F
        list.forEachIndexed { index, item ->
//            if (index == 0){
//                binding.mapView.setViewpoint(
//                    Viewpoint(
//                        item.getLatFromString()?:0.0,
//                        item.getLangFromString()?:0.0,
//                        Constants.MAP_SCALE
//                    )
//                )
//            }
            if(index+1 < list.size) {
                val results = FloatArray(1)
                Location.distanceBetween(
                    item.getLatFromString()?:0.0,
                    item.getLangFromString()?:0.0,
                    list[index+1].getLatFromString()?:0.0,
                    list[index+1].getLangFromString()?:0.0,
                    results
                )
                totaldistance += (results[0]/1000F).KmToNauticalMiles()
                val distance = if(results.isNotEmpty()){
                    "${String.format("%.2f", (results[0]/1000).KmToNauticalMiles())}NM"
                }else{
                    ""
                }
                dataList.add(MeasureDistanceModel(item, list[index + 1],distance))
            }
        }
        binding.tvTotalDistance.text = "Total Distance (${String.format("%.2f", totaldistance)}NM)"
        initAdapter(dataList)
    }


    private fun initAdapter(list: ArrayList<MeasureDistanceModel>){
        binding.layoutMeasureDistanceList.viewVisible()
        //binding.expandable.expand()
        measureDistanceList.addAll(list)
        binding.rvMeasureDistance.adapter = MeasureDistanceAdapter(list)
    }

    //TODO turn on gps if off
    private fun addMarkerOnMap(lat: Double,lang: Double){
        // clear the current graphic position
        graphicsOverlayLocation.graphics.clear()
        // create a map point from lat lang
        val mapPoint = Point(lang,lat, SpatialReference.create(SPECIAL_REFERENCE_4326))
        val graphic = Graphic(mapPoint, pinSourceSymbol)
        graphicsOverlayLocation.graphics?.add(graphic)
    }
    private fun addBoatMarkerOnMap(lat: Double,lang: Double){
        // clear the current graphic position
        graphicsOverlayLocation.graphics.clear()
        // create a map point from lat lang
        val mapPoint = Point(lang,lat, SpatialReference.create(SPECIAL_REFERENCE_4326))
        val graphic = Graphic(mapPoint, boatPinSourceSymbol)
        graphicsOverlayLocation.graphics?.add(graphic)
        binding.tvLatitude.text = getNauticalLatitude(lat)
        binding.tvLongitude.text = getNauticalLongitude(lang)
    }
    private fun addLocationMarkerOnMap(lat: Double,lang: Double){
        // clear the current graphic position
        graphicsOverlayLocation.graphics.clear()
        // create a map point from lat lang
        val mapPoint = Point(lang,lat, SpatialReference.create(SPECIAL_REFERENCE_4326))
        val graphic = Graphic(mapPoint, pinSourceLocationSymbol)
        graphicsOverlayLocation.graphics?.add(graphic)
    }

    private fun addFishLogMarkerOnMap(lat: Double,lang: Double){
        // clear the current graphic position
        graphicsOverlayLocation.graphics.clear()
        // create a map point from lat lang
        val mapPoint = Point(lang,lat, SpatialReference.create(SPECIAL_REFERENCE_4326))
        val graphic = Graphic(mapPoint, pinSourceFishLogSymbol)
        graphicsOverlayLocation.graphics?.add(graphic)
    }

    private fun addMultipleMarkerOnMap(lat: Double,lang: Double, pointName: String?=null){
        // clear the current graphic position
        // create a map point from lat lang
        val mapPoint = Point(lang,lat, SpatialReference.create(SPECIAL_REFERENCE_4326))
        val graphic = Graphic(mapPoint, pinSourceLocationSymbol)
        graphicsOverlayLocation.graphics?.add(graphic)
        if(!pointName.isNullOrEmpty()){
            showCalloutRoute(graphic,pointName)
        }
    }
    private fun addMultipleMarkerOnMapRoute(lat: Double,lang: Double, pointName: String?=null){
        // clear the current graphic position
        // create a map point from lat lang
        val mapPoint = Point(lang,lat, SpatialReference.create(SPECIAL_REFERENCE_4326))
        val graphic = Graphic(mapPoint, pinSourceLocationSymbol)
        graphicsOverlayTrollingData.graphics?.add(graphic)
        if(!pointName.isNullOrEmpty()){
            showCalloutRouteRoute(graphic,pointName)
        }
    }
    private fun addMultipleLocationMarkerOnMap(lat: Double,lang: Double, pinSymbol: PictureMarkerSymbol? = pinSourceLocationSymbol){
        // clear the current graphic position
        // create a map point from lat lang
        val mapPoint = Point(lang,lat, SpatialReference.create(SPECIAL_REFERENCE_4326))
        val graphic = Graphic(mapPoint, pinSymbol)
        graphicsOverlayTrollingData.graphics?.add(graphic)
    }
    private fun addMultipleFishLogMarkerOnMap(lat: Double,lang: Double){
        // clear the current graphic position
        // create a map point from lat lang
        val mapPoint = Point(lang,lat, SpatialReference.create(SPECIAL_REFERENCE_4326))
        val graphic = Graphic(mapPoint, pinSourceFishLogSymbol)
        graphicsOverlayTrollingData.graphics?.add(graphic)
    }

    private fun initMap() {
        //ArcGISRuntimeEnvironment.setApiKey(resources.getString(R.string.api_key))
        ArcGISRuntimeEnvironment.setLicense("runtimelite,1000,rud8211878644,none,ZZ0RJAY3FLED0YRJD186")

        // create and add a map with a light gray basemap style

        // apply mapView assignments
        lifecycleScope.launch {
            binding.mapView.apply {
                val selectedMap = sharePreferenceHelper.getSavedMap()
                if(navArgs.data is MapUiData.OfflineMap && (navArgs.data as MapUiData.OfflineMap).mapPath.isNullOrEmpty()){
                    val portal = Portal(resources.getString(R.string.portal_url), false)
                    val portalItem = PortalItem(portal, resources.getString(R.string.map_portal_id))
                    map = ArcGISMap(portalItem)
                }else{
                    binding.mapView.map = ArcGISMap(if(!selectedMap.isNullOrEmpty()){
                        BasemapStyle.valueOf(selectedMap)
                    }else{
                        BasemapStyle.ARCGIS_OCEANS
                    })
                }
                AuthenticationManager.setAuthenticationChallengeHandler(
                    object: AuthenticationChallengeHandler {
                        override fun handleChallenge(p0: AuthenticationChallenge?): AuthenticationChallengeResponse {
                            val userCredential = UserCredential("dev.fishthebreak","18241Killingit1108!")
                            return AuthenticationChallengeResponse(AuthenticationChallengeResponse.Action.CONTINUE_WITH_CREDENTIAL,userCredential)
                        }

                    }
                )
                val locationDisplay = binding.mapView.locationDisplay
                locationDisplay.autoPanMode = LocationDisplay.AutoPanMode.NAVIGATION
                locationDisplay.startAsync()
                if(navArgs.data is MapUiData.OfflineMap){
                    binding.mapView.setViewpoint(
                        Viewpoint(
                            latitude ?: 34.0270,
                            longitude ?: -118.8050,
                            Constants.MAP_SCALE_Zoom
//                        72000.0
                        )
                    )
                }else{
                    binding.mapView.setViewpoint(
                        Viewpoint(
                            latitude ?: 34.0270,
                            longitude ?: -118.8050,
                            Constants.MAP_SCALE
//                        72000.0
                        )
                    )
                }
                graphicsOverlays.add(graphicsOverlay)//for pin point
//                graphicsOverlays.add(graphicsOverlayLocation)//for location pin point
                graphicsOverlays.add(graphicsOverlayTrollingData)//for trolling location pin point
                binding.mapView.map.addDoneLoadingListener {
                    if(binding.mapView.map.loadStatus == LoadStatus.LOADED){
                        isMapLoaded = true
                        initViews()
                        if(navArgs.data is MapUiData.OfflineMap && !(navArgs.data as MapUiData.OfflineMap).mapPath.isNullOrEmpty()){
                            binding.ivLayers.viewGone()
                            binding.ivCurrentLocation.viewGone()
                        }else {
                            if(navArgs.data is MapUiData.OfflineMap && (navArgs.data as MapUiData.OfflineMap).mapPath.isNullOrEmpty()){
                                interactionOptions.isRotateEnabled = false
                                // create a symbol to show a box around the extent we want to download
                                downloadArea.symbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2F)
                                // add the graphic to the graphics overlay when it is created
                                graphicsOverlayOffline.graphics.add(downloadArea)
                                graphicsOverlays.add(graphicsOverlayOffline)
                                // update the download area box whenever the viewpoint changes
                                addViewpointChangedListener {
                                    if (map.loadStatus == LoadStatus.LOADED) {
                                        // upper left corner of the area to take offline
//                                        val minScreenPoint = android.graphics.Point(200, 200)
                                        val minScreenPoint = android.graphics.Point(100, 100)
                                        // lower right corner of the downloaded area
                                        val maxScreenPoint = android.graphics.Point(
                                            binding.mapView.width - 100,
                                            binding.mapView.height - 100
                                        )
//                                        val maxScreenPoint = android.graphics.Point(
//                                            binding.mapView.width - 200,
//                                            binding.mapView.height - 200
//                                        )
                                        // convert screen points to map points
                                        val minPoint = binding.mapView.screenToLocation(minScreenPoint)
                                        val maxPoint = binding.mapView.screenToLocation(maxScreenPoint)
                                        // use the points to define and return an envelope
                                        if (minPoint != null && maxPoint != null) {
                                            val envelope = Envelope(minPoint, maxPoint)
                                            downloadArea.geometry = envelope
                                            // enable the take map offline button only after the map is loaded
                                            if (!binding.saveMapButton.isEnabled) binding.saveMapButton.isEnabled = true
                                        }
                                    }
                                }
                            }
                            activeLayers.forEach { s, wmsLayersStatusModel ->
                                if (wmsLayersStatusModel.data.isSelected) {
                                    if (wmsLayersStatusModel.data.layer_type == WMS_TYPE && wmsLayersStatusModel.layer is WmsLayer) {
                                        binding.mapView.map.operationalLayers.remove(
                                            wmsLayersStatusModel.layer
                                        )
                                        wmsLayersStatusModel.layer.opacity =
                                            (wmsLayersStatusModel.data.opacity?.toFloat()
                                                ?: DEFAULT_LAYER_OPACITY) / 100F
                                        wmsLayersStatusModel.layer.isVisible =
                                            wmsLayersStatusModel.data.isSelected
                                        addLayerOnMap(wmsLayersStatusModel.layer)
                                    } else if (wmsLayersStatusModel.data.layer_type == Self_Hosted_Type && wmsLayersStatusModel.layer is FeatureLayer) {
                                        binding.mapView.map.operationalLayers.remove(
                                            wmsLayersStatusModel.layer
                                        )
                                        wmsLayersStatusModel.layer.opacity =
                                            (wmsLayersStatusModel.data.opacity?.toFloat()
                                                ?: DEFAULT_LAYER_OPACITY) / 100F
                                        wmsLayersStatusModel.layer.isVisible =
                                            wmsLayersStatusModel.data.isSelected
                                        addLayerOnMapFromPortalId(wmsLayersStatusModel.layer)
                                    } else if (wmsLayersStatusModel.data.layer_type == Feature_Type && wmsLayersStatusModel.layer is FeatureLayer) {
                                        binding.mapView.map.operationalLayers.remove(
                                            wmsLayersStatusModel.layer
                                        )
                                        wmsLayersStatusModel.layer.opacity =
                                            (wmsLayersStatusModel.data.opacity?.toFloat()
                                                ?: DEFAULT_LAYER_OPACITY) / 100F
                                        wmsLayersStatusModel.layer.isVisible =
                                            wmsLayersStatusModel.data.isSelected
                                        addLayerOnMapFromPortalId(wmsLayersStatusModel.layer)
                                    }
                                    else if (wmsLayersStatusModel.data.layer_type == TILE && wmsLayersStatusModel.layer is ArcGISTiledLayer) {
                                        binding.mapView.map.operationalLayers.remove(wmsLayersStatusModel.layer)
                                        wmsLayersStatusModel.layer.opacity =
                                            (wmsLayersStatusModel.data.opacity?.toFloat() ?: DEFAULT_LAYER_OPACITY) / 100F
                                        wmsLayersStatusModel.layer.isVisible = wmsLayersStatusModel.data.isSelected
                                        addLayerOnMapFromTile(wmsLayersStatusModel.layer)
                                    }
                                }
                            }
                        }
                    }else if(binding.mapView.map.loadStatus != LoadStatus.LOADED){
                        val error = "Failed to load portal item ${binding.mapView.map.loadError.message}"
                        val error1 =
                            "Failed to load portal item ${binding.mapView.map.loadError.cause?.message}"
                        Log.e("addDoneLoadingListener", error)
                        Log.e("addDoneLoadingListener1", error1)
                        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                        return@addDoneLoadingListener
                    }
                    showHideLoader(false)
                    graphicsOverlays.add(graphicsOverlayLocation)//for location pin point
                }
            }
//            if(navArgs.data is MapUiData.BoatRange) {
//
//            }else{
//                initMapTouchListener()
//            }
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun initMapTouchListener(){
        binding.mapView.onTouchListener =
            object : DefaultMapViewOnTouchListener(requireContext(),  binding.mapView) {
                // drag a graphic to a new point on map
                override fun onDoubleTouchDrag(e: MotionEvent): Boolean {
                    if(isLocationSaveUiActive) {
                        // identify the pixel at the given screen point
                        val screenPoint = android.graphics.Point(e.x.roundToInt(), e.y.roundToInt())
                        // create a map point from the screen point
                        val mapPoint: Point = binding.mapView.screenToLocation(screenPoint)

//                        val toLatitudeLongitude = CoordinateFormatter.toLatitudeLongitude(mapPoint, CoordinateFormatter.LatitudeLongitudeFormat.DECIMAL_DEGREES, 4)
//                        val point = CoordinateFormatter.fromLatitudeLongitude(toLatitudeLongitude, binding.mapView.spatialReference)
//                        val latitude = point.x
//                        val longitude = point.y
//                        Log.e("mapPoint.x", longitude.toString())
                        val sp = SpatialReference.create(SPECIAL_REFERENCE_4326) //4326 is for Geographic coordinate systems (GCSs)
                        // where Location is the Point
                        val locationPoint = GeometryEngine.project(mapPoint, sp) as Point
                        longitude = locationPoint.x
                        latitude = locationPoint.y
                        // find the map view interaction type
                        when (e.action) {
                            // user selected the graphic
                            MotionEvent.ACTION_DOWN -> {
                                // from the graphics overlay, get the graphics near the tapped location
                                val identifyResultsFuture: ListenableFuture<IdentifyGraphicsOverlayResult> =
                                    binding.mapView.identifyGraphicsOverlayAsync(
                                        graphicsOverlayLocation,
                                        screenPoint,
                                        10.0,
                                        false
                                    )
                                identifyResultsFuture.addDoneListener {
                                    // identify the selected graphic
                                    val identifyGraphicsOverlayResult: IdentifyGraphicsOverlayResult =
                                        identifyResultsFuture.get()
                                    val graphics = identifyGraphicsOverlayResult.graphics
                                    if (graphics.isEmpty()) {
                                        showError("No graphic at point")
                                    } else {
                                        // clear the current graphic position
                                        graphicsOverlayLocation.graphics.clear()
                                        // get the first graphic identified
                                        identifiedGraphic = graphics[0]
                                    }
                                }
                            }
                            // user is moving the graphic
                            MotionEvent.ACTION_MOVE -> {
                                // clear the current graphic position
                                graphicsOverlayLocation.graphics.clear()
                                val graphic = Graphic(mapPoint, pinSourceSymbol)
                                graphicsOverlayLocation.graphics?.add(graphic)
                            }
                            // user released the graphic
                            MotionEvent.ACTION_UP -> {
                                // release selected graphic
                                identifiedGraphic = null
                            }
                        }
                    }
                    return true
                }

                // place a new graphic on map
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    if(isLocationSaveUiActive) {
                        // clear the current graphic position
                        graphicsOverlayLocation.graphics.clear()
                        // create a screen point from where the user tapped
                        val screenPoint = android.graphics.Point(e.x.roundToInt(), e.y.roundToInt())
                        // create a map point from the screen point
                        val mapPoint: Point = binding.mapView.screenToLocation(screenPoint)
                        // create graphic with the location and symbol and add it to the graphics overlay
                        val sp = SpatialReference.create(SPECIAL_REFERENCE_4326) //4326 is for Geographic coordinate systems (GCSs)
                        // where Location is the Point
                        val locationPoint = GeometryEngine.project(mapPoint, sp) as Point
                        longitude = locationPoint.x
                        latitude = locationPoint.y
                        val graphic = Graphic(mapPoint, pinSourceSymbol)
                        graphicsOverlayLocation.graphics?.add(graphic)
                    }
                    return true
                }
            }
    }
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        requireActivity().checkLocationPermission(childFragmentManager,
            object : PermissionListener {
                override fun onPermissionGranted() {
                    binding.mapView.viewVisible()
                    binding.ivLayers.isClickable = true
                    binding.locationPermissionLayout.viewGone()
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        object : CancellationToken() {
                            override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                                CancellationTokenSource().token

                            override fun isCancellationRequested() = false
                        })
                        .addOnSuccessListener { location: Location? ->
                            if (location == null)
                                Toast.makeText(
                                    requireContext(),
                                    "Cannot get location.",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            else {
                                if(latitude == null) {
                                    latitude = location.latitude
                                    longitude = location.longitude
                                    initMap()
                                }else{
                                    binding.mapView.setViewpoint(
                                        Viewpoint(
                                            latitude ?: 34.0270,
                                            longitude ?: -118.8050,
                                            Constants.MAP_SCALE
//                        72000.0
                                        )
                                    )
                                }
                            }

                        }
                }

                override fun onPermissionCancel() {
                    Log.i("onPermissionCancel","location")
                    binding.ivLayers.isClickable = false
                    binding.mapView.viewGone()
                    binding.locationPermissionLayout.viewVisible()
                    showHideLoader(false)
                    //startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }

            })
    }

    /**
     *  Creates a picture marker symbol from the pin icon, and sets it to half of its original size.
     */
    private fun createPinSymbol(@DrawableRes icon: Int): PictureMarkerSymbol? {
        val pinDrawable =
            ContextCompat.getDrawable(requireContext(), icon) as BitmapDrawable?
        val pinSymbol: PictureMarkerSymbol
        try {
            pinSymbol = PictureMarkerSymbol.createAsync(pinDrawable).get()
            pinSymbol.width = 30f
            pinSymbol.height = 30f
            return pinSymbol
        } catch (e: Exception) {
            when (e) {
                is ExecutionException, is InterruptedException -> {
                    Log.e(TAG, "Picture Marker Symbol error: " + e.message)
                    Toast.makeText(
                        requireContext(),
                        "Failed to load pin drawable.",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }

                else -> throw e
            }
        }
        return null
    }
    private fun createLocationPinSymbol(@DrawableRes icon: Int, width:Float = 30f, height: Float = 30f): PictureMarkerSymbol? {
        val pinDrawable =
            ContextCompat.getDrawable(requireContext(), icon) as BitmapDrawable?
        val pinSymbol: PictureMarkerSymbol
        try {
            pinSymbol = PictureMarkerSymbol.createAsync(pinDrawable).get()
            pinSymbol.width = width
            pinSymbol.height = height
            return pinSymbol
        } catch (e: Exception) {
            when (e) {
                is ExecutionException, is InterruptedException -> {
                    Log.e(TAG, "Picture Marker Symbol error: " + e.message)
                    Toast.makeText(
                        requireContext(),
                        "Failed to load pin drawable.",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }

                else -> throw e
            }
        }
        return null
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.resume()
    }

    override fun onPause() {
        binding.mapView.pause()
        super.onPause()
    }

    override fun onDestroy() {
        binding.mapView.dispose()
        try {
            handler.removeCallbacks(runableTimer)
        }catch (ex: Exception){

        }
        super.onDestroy()
    }

//    private fun showError(message: String) {
//        Log.e("HomeFragment", message)
//        Snackbar.make(binding.mapView, message, Snackbar.LENGTH_SHORT).show()
//    }

    private fun initViewModelResponse() {
        lifecycleScope.launch {
            viewModel.selectedMapResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    if (response.isNotEmpty()) {
                        binding.mapView.map.basemap =
                            Basemap(BasemapStyle.valueOf(response.replace(" ", "_")))
                        viewModel.resetMapResponse()
                    }
                }
        }

        lifecycleScope.launch {
            viewModel.layerToggleResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                //.distinctUntilChanged()
                .collect { response ->
                    if (response.isNotEmpty()) {
                        if (viewModel.clearAllLayersResponse.value) {
                            activeLayers.forEach { key, wmsLayer ->
                                if (isMapLoaded) {
                                    if(wmsLayer.data.layer_type == WMS_TYPE && wmsLayer.layer is WmsLayer) {
                                        binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                    }else if(wmsLayer.data.layer_type == Self_Hosted_Type && wmsLayer.layer is FeatureLayer){
                                        binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                    }else if(wmsLayer.data.layer_type == Feature_Type && wmsLayer.layer is FeatureLayer){
                                        binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                    }else if(wmsLayer.data.layer_type == TILE && wmsLayer.layer is ArcGISTiledLayer){
                                        binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                    }
                                }
                            }
                            activeLayers.clear()
                            viewModel.resetClearLayerResponse()
                        }
                        response.forEach { key, wmsLayer ->
                            activeLayers.put(key, wmsLayer)
                            Log.e("load portal item ", wmsLayer.data.layer_calling_name)
                            if (isMapLoaded) {
                                if(wmsLayer.data.layer_type == WMS_TYPE && wmsLayer.layer is WmsLayer) {
                                    binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                    wmsLayer.layer.opacity = (wmsLayer.data.opacity?.toFloat()?:DEFAULT_LAYER_OPACITY)/100F
                                    wmsLayer.layer.isVisible = wmsLayer.data.isSelected
                                    addLayerOnMap(wmsLayer.layer)
                                }else if(wmsLayer.data.layer_type == Self_Hosted_Type && wmsLayer.layer is FeatureLayer){
                                    binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                    wmsLayer.layer.opacity = (wmsLayer.data.opacity?.toFloat()?:DEFAULT_LAYER_OPACITY)/100F
                                    wmsLayer.layer.isVisible = wmsLayer.data.isSelected
                                    addLayerOnMapFromPortalId(wmsLayer.layer)
                                }
                                else if(wmsLayer.data.layer_type == Feature_Type && wmsLayer.layer is FeatureLayer){
                                    binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                    wmsLayer.layer.opacity = (wmsLayer.data.opacity?.toFloat()?:DEFAULT_LAYER_OPACITY)/100F
                                    wmsLayer.layer.isVisible = wmsLayer.data.isSelected
                                    addLayerOnMapFromPortalId(wmsLayer.layer)
                                }
                                else if (wmsLayer.data.layer_type == TILE && wmsLayer.layer is ArcGISTiledLayer) {
                                    binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                    wmsLayer.layer.opacity =
                                        (wmsLayer.data.opacity?.toFloat() ?: DEFAULT_LAYER_OPACITY) / 100F
                                    wmsLayer.layer.isVisible = wmsLayer.data.isSelected
                                    addLayerOnMapFromTile(wmsLayer.layer)
                                }
                            }
                        }
                        //viewModel.saveDataInSharedPreference()
                    }
                }
        }
    }
    private fun initTrollingViewModelResponse() {
        lifecycleScope.launch {
            savedTrollingViewModel.singleTrollingResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true && response.data.data != null) {
                                trollingResponse = response.data.data
                                createTrollingUi(response.data.data)
                            } else {
                                requireActivity().showToast(
                                    response.data?.message ?: resources.getString(
                                        R.string.something_went_wrong
                                    ), false
                                )
                                savedTrollingViewModel.resetResponse()
                            }
                        }

                        is NetworkResult.Error -> {
                            showHideLoader(false)
                            requireActivity().showToast(
                                response.message.getOnErrorMessage(),
                                false
                            )
                            savedTrollingViewModel.resetResponse()
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
                            savedTrollingViewModel.resetResponse()
                        }

                        is NetworkResult.NoCallYet -> {
                        }
                    }
                }
        }
    }
    private fun initLocationViewModelResponse() {
        lifecycleScope.launch {
            viewModelLocation.savePointResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true && response.data.data != null) {
                                locationData = response.data.data
                                createLocationUi(response.data.data)
                            } else {
                                requireActivity().showToast(
                                    response.data?.message ?: resources.getString(
                                        R.string.something_went_wrong
                                    ), false
                                )
                                viewModelLocation.resetResponse()
                            }
                        }

                        is NetworkResult.Error -> {
                            showHideLoader(false)
                            requireActivity().showToast(
                                response.message.getOnErrorMessage(),
                                false
                            )
                            viewModelLocation.resetResponse()
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
                            viewModelLocation.resetResponse()
                        }

                        is NetworkResult.NoCallYet -> {
                        }
                    }
                }
        }
        lifecycleScope.launch {
            viewModelLocation.saveFishLogResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true && response.data.data != null) {
                                fishLogData = response.data.data
                                createFishLogUi(response.data.data)
                            } else {
                                requireActivity().showToast(
                                    response.data?.message ?: resources.getString(
                                        R.string.something_went_wrong
                                    ), false
                                )
                                viewModelLocation.resetResponse()
                            }
                        }

                        is NetworkResult.Error -> {
                            showHideLoader(false)
                            requireActivity().showToast(
                                response.message.getOnErrorMessage(),
                                false
                            )
                            viewModelLocation.resetResponse()
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
                            viewModelLocation.resetResponse()
                        }

                        is NetworkResult.NoCallYet -> {
                        }
                    }
                }
        }
    }
    private fun initRouteViewModelResponse() {
        lifecycleScope.launch {
            viewModelRoute.singleRouteResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true && response.data.data != null) {
                                measureDistance(response.data.data.points)
                            } else {
                                requireActivity().showToast(
                                    response.data?.message ?: resources.getString(
                                        R.string.something_went_wrong
                                    ), false
                                )
                                viewModelRoute.resetResponse()
                            }
                        }

                        is NetworkResult.Error -> {
                            showHideLoader(false)
                            requireActivity().showToast(
                                response.message.getOnErrorMessage(),
                                false
                            )
                            viewModelRoute.resetResponse()
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
                            viewModelRoute.resetResponse()
                        }

                        is NetworkResult.NoCallYet -> {
                        }
                    }
                }
        }
    }

    private fun addLayerOnMap(wmsLayer: WmsLayer) {
        binding.mapView.map.operationalLayers.add(wmsLayer)
        // if loading the layer fails show an error
        wmsLayer.addDoneLoadingListener {
            if (wmsLayer.loadStatus != LoadStatus.LOADED) {
                val error = "Failed to load portal item ${wmsLayer.loadError.message}"
                val error1 = "Failed to load portal item ${wmsLayer.loadError.cause?.message}"
                Log.e("addDoneLoadingListener", error)
                Log.e("addDoneLoadingListener1", error1)
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                return@addDoneLoadingListener
            }
        }
    }

    private fun addLayerOnMapFromTile(layer: ArcGISTiledLayer) {
        binding.mapView.map.operationalLayers.add(layer)
        // if loading the layer fails show an error
        layer.addDoneLoadingListener {
            if (layer.loadStatus != LoadStatus.LOADED) {
                val error = "Failed to load portal item ${layer.loadError.message}"
                val error1 = "Failed to load portal item ${layer.loadError.cause?.message}"
                Log.e("addDoneLoadingListener", error)
                Log.e("addDoneLoadingListener1", error1)
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                return@addDoneLoadingListener
            }
        }
    }

    private fun addLayerOnMapFromPortalId(layer: FeatureLayer) {
        binding.mapView.map.operationalLayers.add(layer)
        // if loading the layer fails show an error
        layer.addDoneLoadingListener {
            if (layer.loadStatus != LoadStatus.LOADED) {
                val error = "Failed to load portal item ${layer.loadError.message}"
                val error1 = "Failed to load portal item ${layer.loadError.cause?.message}"
                Log.e("addDoneLoadingListener", error)
                Log.e("addDoneLoadingListener1", error1)
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                return@addDoneLoadingListener
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

    private fun drawCircleOnMap(){
        //TODO add label on circle border
        planarGraphicsOverlay.graphics.clear()
        geodesicGraphicsOverlay.graphics.clear()
        tapLocationsOverlay.graphics.clear()
        planarGraphicsOverlayInner.graphics.clear()
        geodesicGraphicsOverlayInner.graphics.clear()
        tapLocationsOverlayInner.graphics.clear()
        if (mapPoint == null){
            return
        }
        // val mapPoint: Point = mMapView.screenToLocation(screenPoint)
        // only draw a buffer if a value was entered
        if (binding.etRange.text.toString().trim().isNotEmpty()) {
            // get the buffer distance (miles) entered in the text box.
            val bufferInMiles: Double =
                java.lang.Double.valueOf(binding.etRange.text.toString())

            // convert the input distance to meters, 1609.34 meters in one mile
            val bufferInMeters = (bufferInMiles.nauticalMilesToMiles()) * 1609.34
            // distance in meters
            //val bufferInMeters = bufferInMiles
            viewModelPreference.onRangeChangeEvent(bufferInMiles.toInt())
            // create a planar buffer graphic around the input location at the specified distance
            val bufferGeometryPlanar: Geometry =
                GeometryEngine.buffer(mapPoint, bufferInMeters)
            val planarBufferGraphic = Graphic(bufferGeometryPlanar)

            // create a geodesic buffer graphic using the same location and distance
            val bufferGeometryGeodesic: Geometry = GeometryEngine.bufferGeodetic(
                mapPoint, bufferInMeters,
                LinearUnit(LinearUnitId.METERS), Double.NaN, GeodeticCurveType.GEODESIC
            )
            val geodesicBufferGraphic = Graphic(bufferGeometryGeodesic)

            // create a graphic for the user tap location
            val locationGraphic = Graphic(mapPoint)

            // add the buffer polygons and tap location graphics to the appropriate graphic overlays.
            planarGraphicsOverlay.graphics.add(planarBufferGraphic)
            geodesicGraphicsOverlay.graphics.add(geodesicBufferGraphic)
            tapLocationsOverlay.graphics.add(locationGraphic)
            showCalloutOuter(geodesicBufferGraphic, bufferInMiles)
            drawInnerCircleOnMap()
        } else {
            viewModelPreference.onRangeChangeEvent(0)
            Toast.makeText(
                requireContext(),
                "Please enter a buffer distance first.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showCalloutInner(graphic: Graphic, radius: Double) {
        if(radius <= 0){
            return
        }
        val centerOfGraphic = graphic.geometry.extent.center
        val calloutLocation = graphic.computeCalloutLocation(centerOfGraphic, binding.mapView)
        val ts = TextSymbol(14f, "${radius.roundOffDecimal().toString().replace(".0","")}NM",  Color.parseColor("#FFFFFF") , TextSymbol.HorizontalAlignment.CENTER, TextSymbol.VerticalAlignment.BOTTOM)
        ts.backgroundColor = Color.parseColor("#000000")
        val currentTextGraphic = Graphic(calloutLocation, ts)
        geodesicGraphicsOverlayInner.graphics.add(currentTextGraphic)
    }

    private fun showCalloutOuter(graphic: Graphic, radius: Double) {
        if(radius <= 0){
            return
        }
        val centerOfGraphic = graphic.geometry.extent.center
        val calloutLocation = graphic.computeCalloutLocation(centerOfGraphic, binding.mapView)
        val ts = TextSymbol(14f, "${radius.roundOffDecimal().toString().replace(".0","")}NM",  Color.parseColor("#FFFFFF") , TextSymbol.HorizontalAlignment.CENTER, TextSymbol.VerticalAlignment.BOTTOM)
        ts.backgroundColor = Color.parseColor("#00386B")
        val currentTextGraphic = Graphic(calloutLocation, ts)
        geodesicGraphicsOverlay.graphics.add(currentTextGraphic)
    }


    private fun initCircleOnMap(){
        // create a graphics overlay to contain the buffered geometry graphics
        // create a graphics overlay to contain the buffered geometry graphics
        val graphicsOverlay = GraphicsOverlay()
        binding.mapView.getGraphicsOverlays().add(graphicsOverlay)

        // create a fill symbol for geodesic buffer polygons

        // create a fill symbol for geodesic buffer polygons
        val geodesicOutlineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.parseColor("#2DA0FA"), 2f)
        val geodesicBufferFillSymbol = SimpleFillSymbol(
            SimpleFillSymbol.Style.SOLID, android.R.color.transparent,
            geodesicOutlineSymbol
        )

        // create a fill symbol for planar buffer polygons

        // create a fill symbol for planar buffer polygons
        val planarOutlineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, android.R.color.transparent, 2f)
        val planarBufferFillSymbol = SimpleFillSymbol(
            SimpleFillSymbol.Style.SOLID, android.R.color.transparent,
            planarOutlineSymbol
        )

        // create a marker symbol for tap locations

        // create a marker symbol for tap locations
        val tapSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, android.R.color.transparent, 14f)

        // create a graphics overlay to display geodesic polygons, set its renderer and add it to the map view.

        // create a graphics overlay to display geodesic polygons, set its renderer and add it to the map view.
        geodesicGraphicsOverlay.renderer = SimpleRenderer(geodesicBufferFillSymbol)
        geodesicGraphicsOverlay.opacity = 0.5f
        binding.mapView.getGraphicsOverlays().add(geodesicGraphicsOverlay)

        // create a graphics overlay to display planar polygons, set its renderer and add it to the map view.

        // create a graphics overlay to display planar polygons, set its renderer and add it to the map view.
        planarGraphicsOverlay.renderer = SimpleRenderer(planarBufferFillSymbol)
        planarGraphicsOverlay.opacity = 0.5f
        binding.mapView.getGraphicsOverlays().add(planarGraphicsOverlay)

        // create a graphics overlay to display tap locations for buffers, set its renderer and add it to the map view.

        // create a graphics overlay to display tap locations for buffers, set its renderer and add it to the map view.
        tapLocationsOverlay.renderer = SimpleRenderer(tapSymbol)
        binding.mapView.getGraphicsOverlays().add(tapLocationsOverlay)
        initInnerCircleOnMap()
    }
    private fun drawInnerCircleOnMap(){
        planarGraphicsOverlayInner.graphics.clear()
        geodesicGraphicsOverlayInner.graphics.clear()
        tapLocationsOverlayInner.graphics.clear()
        if (mapPoint == null){
            return
        }
        //val mapPoint: Point = mMapView.screenToLocation(screenPoint)
        // only draw a buffer if a value was entered
        if (binding.etRange.text.toString().trim().isNotEmpty()) {
            // get the buffer distance (miles) entered in the text box.
            val bufferInMiles: Double =
                java.lang.Double.valueOf(binding.etRange.text.toString())

            // convert the input distance to meters, 1609.34 meters in one mile
            val bufferInMeters = ((bufferInMiles.nauticalMilesToMiles()) * 1609.34)/2.0
            // distance in meters
            //val bufferInMeters = bufferInMiles/2
            // create a planar buffer graphic around the input location at the specified distance
            val bufferGeometryPlanar: Geometry =
                GeometryEngine.buffer(mapPoint, bufferInMeters)
            val planarBufferGraphic = Graphic(bufferGeometryPlanar)

            // create a geodesic buffer graphic using the same location and distance
            val bufferGeometryGeodesic: Geometry = GeometryEngine.bufferGeodetic(
                mapPoint, bufferInMeters,
                LinearUnit(LinearUnitId.METERS), Double.NaN, GeodeticCurveType.GEODESIC
            )
            val geodesicBufferGraphic = Graphic(bufferGeometryGeodesic)

            // create a graphic for the user tap location
            val locationGraphic = Graphic(mapPoint)

            // add the buffer polygons and tap location graphics to the appropriate graphic overlays.
            planarGraphicsOverlayInner.graphics.add(planarBufferGraphic)
            geodesicGraphicsOverlayInner.graphics.add(geodesicBufferGraphic)
            tapLocationsOverlayInner.graphics.add(locationGraphic)
            showCalloutInner(geodesicBufferGraphic, bufferInMiles/2)
        } else {
            Toast.makeText(
                requireContext(),
                "Please enter a buffer distance first.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    private fun initInnerCircleOnMap(){
        // create a graphics overlay to contain the buffered geometry graphics
        // create a graphics overlay to contain the buffered geometry graphics
        val graphicsOverlay = GraphicsOverlay()
        binding.mapView.getGraphicsOverlays().add(graphicsOverlay)

        // create a fill symbol for geodesic buffer polygons

        // create a fill symbol for geodesic buffer polygons
        val geodesicOutlineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 2f)
        val geodesicBufferFillSymbol = SimpleFillSymbol(
            SimpleFillSymbol.Style.SOLID, android.R.color.transparent,
            geodesicOutlineSymbol
        )

        // create a fill symbol for planar buffer polygons

        // create a fill symbol for planar buffer polygons
        val planarOutlineSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, android.R.color.transparent, 2f)
        val planarBufferFillSymbol = SimpleFillSymbol(
            SimpleFillSymbol.Style.SOLID, android.R.color.transparent,
            planarOutlineSymbol
        )

        // create a marker symbol for tap locations

        // create a marker symbol for tap locations
        val tapSymbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, android.R.color.transparent, 14f)

        // create a graphics overlay to display geodesic polygons, set its renderer and add it to the map view.

        // create a graphics overlay to display geodesic polygons, set its renderer and add it to the map view.
        geodesicGraphicsOverlayInner.renderer = SimpleRenderer(geodesicBufferFillSymbol)
        geodesicGraphicsOverlayInner.opacity = 0.5f
        binding.mapView.getGraphicsOverlays().add(geodesicGraphicsOverlayInner)

        // create a graphics overlay to display planar polygons, set its renderer and add it to the map view.

        // create a graphics overlay to display planar polygons, set its renderer and add it to the map view.
        planarGraphicsOverlayInner.renderer = SimpleRenderer(planarBufferFillSymbol)
        planarGraphicsOverlayInner.opacity = 0.5f
        binding.mapView.getGraphicsOverlays().add(planarGraphicsOverlayInner)

        // create a graphics overlay to display tap locations for buffers, set its renderer and add it to the map view.

        // create a graphics overlay to display tap locations for buffers, set its renderer and add it to the map view.
        tapLocationsOverlayInner.renderer = SimpleRenderer(tapSymbol)
        binding.mapView.getGraphicsOverlays().add(tapLocationsOverlayInner)

    }

    private fun initBoatRangeViewModelResponse() {
        lifecycleScope.launch {
            viewModelPreference.profileResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true) {
                                requireActivity().showToast(response.data.message, true)
                                //delay(1500)
                                if(requireActivity() is NavGraphActivity){
                                    (requireActivity() as NavGraphActivity).onBack()
                                }
                                viewModelPreference.resetResponse()
                            } else {
                                requireActivity().showToast(
                                    response.data?.message ?: resources.getString(
                                        R.string.something_went_wrong
                                    ), false
                                )
                                viewModelPreference.resetResponse()
                            }
                        }

                        is NetworkResult.Error -> {
                            showHideLoader(false)
                            requireActivity().showToast(
                                response.message.getOnErrorMessage(),
                                false
                            )
                            viewModelPreference.resetResponse()
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
                            viewModelPreference.resetResponse()
                        }

                        is NetworkResult.NoCallYet -> {
                        }
                    }
                }
        }
        lifecycleScope.launch {
            viewModelPreference.toggleResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true) {
                                requireActivity().showToast(response.data.message, true)
                                viewModelPreference.resetResponse()
                            } else {
                                requireActivity().showToast(
                                    response.data?.message ?: resources.getString(
                                        R.string.something_went_wrong
                                    ), false
                                )
                                viewModelPreference.resetResponse()
                            }
                        }

                        is NetworkResult.Error -> {
                            showHideLoader(false)
                            requireActivity().showToast(
                                response.message.getOnErrorMessage(),
                                false
                            )
                            viewModelPreference.resetResponse()
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
                            viewModelPreference.resetResponse()
                        }

                        is NetworkResult.NoCallYet -> {
                        }
                    }
                }
        }
        lifecycleScope.launch {
            viewModelPreference.validationError
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    if (response.isNotEmpty()){
                        requireActivity().showToast(response, false)
                        viewModelPreference.resetErrorResponse()
                    }
                }
        }

    }


    private fun setupAddressSearchView(address: String) {
        addressGeocodeParameters = GeocodeParameters().apply {
            // get place name and street address attributes
            resultAttributeNames.addAll(listOf("PlaceName", "Place_addr"))
            // return only the closest result
            maxResults = 1
            geoCodeTypedAddress(address)
        }
    }
    /**
     * Geocode an address passed in by the user.
     *
     * @param address the address read in from searchViews
     */
    private fun geoCodeTypedAddress(address: String) {
        if(address.isValidLatLangNauticalFormat()){
            val latLng = convertCoordinatesToLatLng(address) ?: return
            val (lat, lang) = latLng
            val searchData = SearchData(
                "",
                null,
                address,
                "",
                -1,
                null,
                lat.toString(),
                lang.toString(),
                null,
                "1"
            )
            displaySearchResultOnMap(searchData)
            binding.tvSearch.text =
                searchData.search_text ?: searchData.name
                        ?: ""
            binding.ivClearSearch.viewVisible()
            //setViewPointDetails(searchData)
            return
        }
        locatorTask.addDoneLoadingListener {
            if (locatorTask.loadStatus == LoadStatus.LOADED) {
                // run the locatorTask geocode task, passing in the address
                val geocodeResultFuture =
                    locatorTask.geocodeAsync(address, addressGeocodeParameters)
                geocodeResultFuture.addDoneListener {
                    try {
                        // get the results of the async operation
                        val geocodeResults = geocodeResultFuture.get()
                        if (geocodeResults.isNotEmpty()) {
                            displaySearchResultOnMap(geocodeResults[0])
                        } else {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.location_not_found) + address,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } catch (e: Exception) {
                        when (e) {
                            is ExecutionException, is InterruptedException -> {
                                Log.e(TAG, "Geocode error: " + e.message)
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.geo_locate_error),
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            }

                            else -> throw e
                        }
                    }
                }
            } else {
                locatorTask.retryLoadAsync()
            }
        }
        locatorTask.loadAsync()
    }

    /**
     * Turns a GeocodeResult into a Point and adds it to a graphic overlay on the map.
     *
     * @param geocodeResult a single geocode result
     */
    private fun displaySearchResultOnMap(geocodeResult: GeocodeResult) {
        // clear graphics overlay of existing graphics
        graphicsOverlay.graphics.clear()
        // create graphic object for resulting location
        val resultPoint = geocodeResult.displayLocation
        val resultLocationGraphic = Graphic(resultPoint, geocodeResult.attributes, pinSourceSymbol)
        // add graphic to location layer
        graphicsOverlay.graphics.add(resultLocationGraphic)
        binding.mapView.setViewpointAsync(Viewpoint(geocodeResult.extent), 0f).addDoneListener {
            showCallout(resultLocationGraphic)
        }
    }
    private fun displaySearchResultOnMap(data: SearchData) {
        try {
            // clear graphics overlay of existing graphics
            graphicsOverlay.graphics.clear()
            // create graphic object for resulting location
            // create a map point from lat lang
            val mapPoint = Point(
                data.getLangFromString() ?: longitude?:0.0,
                data.getLatFromString() ?: latitude?:0.0,
                SpatialReference.create(SPECIAL_REFERENCE_4326)//3857
            )
            val resultLocationGraphic = Graphic(mapPoint, pinSourceSymbol)

            // add graphic to location layer
            graphicsOverlay.graphics.add(resultLocationGraphic)
            binding.mapView.setViewpointAsync(Viewpoint(mapPoint.extent), 0f).addDoneListener {
                showCallout(resultLocationGraphic, data)
            }
        } catch (ex: Exception) {

        }
    }
    private fun showCallout(graphic: Graphic, data: SearchData) {
        // create a text view for the callout
        val calloutContent = TextView(requireContext()).apply {
            setTextColor(Color.BLACK)
            // get the graphic attributes for place name and street address, and display them as text in the callout
            this.text = data.search_text ?: data.name ?: ""
        }
        // get the center of the graphic to set the callout location
        val centerOfGraphic = graphic.geometry.extent.center
        val calloutLocation = graphic.computeCalloutLocation(centerOfGraphic, binding.mapView)
        callout = binding.mapView.callout.apply {
            showOptions = Callout.ShowOptions(true, true, true)
            content = calloutContent
            // set the leader position using the callout location
            setGeoElement(graphic, calloutLocation)
            // show callout beneath graphic
            style.leaderPosition = Callout.Style.LeaderPosition.UPPER_MIDDLE
            // show the callout
            if (!isShowing) {
                show()
            }
        }
    }

    /**
     * Shows the given graphic's attributes as a call out.
     *
     * @param graphic the graphic containing the attributes to be displayed
     */
    private fun showCallout(graphic: Graphic) {
        // create a text view for the callout
        val calloutContent = TextView(requireContext()).apply {
            setTextColor(Color.BLACK)
            // get the graphic attributes for place name and street address, and display them as text in the callout
            this.text = if (graphic.attributes["PlaceName"].toString().isNotEmpty()) {
                graphic.attributes["PlaceName"].toString() + "\n" + graphic.attributes["Place_addr"].toString()
            } else {
                graphic.attributes["Place_addr"].toString()
            }
        }
        // get the center of the graphic to set the callout location
        val centerOfGraphic = graphic.geometry.extent.center
        val calloutLocation = graphic.computeCalloutLocation(centerOfGraphic, binding.mapView)
        callout = binding.mapView.callout.apply {
            showOptions = Callout.ShowOptions(true, true, true)
            content = calloutContent
            // set the leader position using the callout location
            setGeoElement(graphic, calloutLocation)
            // show callout beneath graphic
            style.leaderPosition = Callout.Style.LeaderPosition.UPPER_MIDDLE
            // show the callout
            if (!isShowing) {
                show()
            }
        }
    }

}