package com.cp.fishthebreak.screens.fragments.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.home.AddDestinationAdapter
import com.cp.fishthebreak.adapters.routes.MeasureDistanceAdapter
import com.cp.fishthebreak.databinding.FragmentHomeBinding
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.models.home.AddDestinationModel
import com.cp.fishthebreak.models.home.SearchData
import com.cp.fishthebreak.models.map.MapLayer
import com.cp.fishthebreak.models.map.OfflineMap
import com.cp.fishthebreak.models.map.WmsLayersStatusModel
import com.cp.fishthebreak.models.points.SaveFishLogData
import com.cp.fishthebreak.models.points.SavePointsData
import com.cp.fishthebreak.models.routes.MeasureDistanceModel
import com.cp.fishthebreak.screens.activities.BaseActivityResult
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.cp.fishthebreak.screens.bottom_sheets.LayerBottomSheet
import com.cp.fishthebreak.screens.bottom_sheets.LayerOpacityBottomSheet
import com.cp.fishthebreak.screens.bottom_sheets.OfflineMapBottomSheet
import com.cp.fishthebreak.screens.bottom_sheets.PermissionBottomSheet
import com.cp.fishthebreak.screens.bottom_sheets.locations.SaveLocationBottomSheet
import com.cp.fishthebreak.screens.bottom_sheets.locations.SaveTrollingNameBottomSheet
import com.cp.fishthebreak.screens.bottom_sheets.locations.StartSaveLocationBottomSheet
import com.cp.fishthebreak.screens.bottom_sheets.routes.MeasureDistanceBottomSheet
import com.cp.fishthebreak.screens.bottom_sheets.routes.SaveRouteBottomSheet
import com.cp.fishthebreak.screens.bottom_sheets.trolling.RecordTrollingBottomSheet
import com.cp.fishthebreak.services.LocationService
import com.cp.fishthebreak.utils.Constants
import com.cp.fishthebreak.utils.Constants.Companion.CURRENT_LOCATION_SCALE
import com.cp.fishthebreak.utils.Constants.Companion.DEFAULT_LAYER_OPACITY
import com.cp.fishthebreak.utils.Constants.Companion.Feature_Type
import com.cp.fishthebreak.utils.Constants.Companion.MAP_PADDING
import com.cp.fishthebreak.utils.Constants.Companion.MAP_SCALE
import com.cp.fishthebreak.utils.Constants.Companion.OFFLINE_MAP_SCALE_Zoom
import com.cp.fishthebreak.utils.Constants.Companion.SPECIAL_REFERENCE_3857
import com.cp.fishthebreak.utils.Constants.Companion.SPECIAL_REFERENCE_4326
import com.cp.fishthebreak.utils.Constants.Companion.Self_Hosted_Type
import com.cp.fishthebreak.utils.Constants.Companion.TILE
import com.cp.fishthebreak.utils.Constants.Companion.WMS_TYPE
import com.cp.fishthebreak.utils.GPSCheck
import com.cp.fishthebreak.utils.KmToNauticalMiles
import com.cp.fishthebreak.utils.MapUiData
import com.cp.fishthebreak.utils.MyAuthenticationChallengeHandler
import com.cp.fishthebreak.utils.NavigationDirections
import com.cp.fishthebreak.utils.PermissionListener
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.cp.fishthebreak.utils.StartDestination
import com.cp.fishthebreak.utils.checkLocationPermission
import com.cp.fishthebreak.utils.convertCoordinatesToLatLng
import com.cp.fishthebreak.utils.getNauticalLatitude
import com.cp.fishthebreak.utils.getNauticalLongitude
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.getTrollingTime
import com.cp.fishthebreak.utils.isIgnoringBatteryOptimizations
import com.cp.fishthebreak.utils.isNetworkAvailable
import com.cp.fishthebreak.utils.isValidLatLangNauticalFormat
import com.cp.fishthebreak.utils.nauticalMilesToMiles
import com.cp.fishthebreak.utils.roundOffDecimal
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.SyncViewModel
import com.cp.fishthebreak.viewModels.map.LayersViewModel
import com.cp.fishthebreak.viewModels.profile.locations.LocationViewModel
import com.cp.fishthebreak.viewModels.profile.trolling.TrollingViewModel
import com.cp.fishthebreak.viewModels.saved.SavedLocationViewModel
import com.cp.fishthebreak.viewModels.saved.SavedRouteViewModel
import com.cp.fishthebreak.worker.WorkerStarter
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.concurrent.ListenableFuture
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
import com.esri.arcgisruntime.mapping.MobileMapPackage
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Callout
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Date
import java.util.UUID
import java.util.concurrent.ExecutionException
import javax.inject.Inject
import kotlin.math.roundToInt


@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var identifiedGraphic: Graphic? = null
    private val TAG: String = HomeFragment::class.java.simpleName
    private lateinit var binding: FragmentHomeBinding

    @Inject
    lateinit var sharePreferenceHelper: SharePreferenceHelper
    private var isOpen = false
    private var fab_open: Animation? = null
    private var fab_close: Animation? = null
    private var fab_hide: Animation? = null
    private var fab_clock: Animation? = null
    private var fab_anticlock: Animation? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double? = null
    private var longitude: Double? = null
    private val activityLauncher: BaseActivityResult<Intent, ActivityResult> =
        BaseActivityResult.registerActivityForResult(this)
    private val destinationList = ArrayList<Any?>()
    private var addressGeocodeParameters: GeocodeParameters? = null
    private val activeLayers: HashMap<String, WmsLayersStatusModel> = HashMap()
    private var isLocationSaveUiActive = false
    private val handler: Handler = Handler(Looper.getMainLooper())
    private var trollingStartTime: Date? = null
    private var trollingPauseTime: Date? = null
    private var totalTrollingPauseTime: Long? = null
    private var offlineMap: OfflineMap? = null
    private var onlineMap: ArcGISMap? = null
    val runableTimer = object : Runnable {
        override fun run() {
            binding.tvTrollingTime.text =
                trollingStartTime.getTrollingTime(totalTrollingPauseTime ?: 0)
            handler.postDelayed(this, 1000)// 1 seconds
        }

    }

    // create a locator task from an online service
    private val locatorTask: LocatorTask by lazy {
        LocatorTask("https://geocode-api.arcgis.com/arcgis/rest/services/World/GeocodeServer")
    }

    private val geodesicGraphicsOverlay: GraphicsOverlay by lazy { GraphicsOverlay() }//boat range
    private val planarGraphicsOverlay: GraphicsOverlay by lazy { GraphicsOverlay() }//boat range
    private val tapLocationsOverlay: GraphicsOverlay by lazy { GraphicsOverlay() }//boat range
    private val geodesicGraphicsOverlayInner: GraphicsOverlay by lazy { GraphicsOverlay() }//boat range
    private val planarGraphicsOverlayInner: GraphicsOverlay by lazy { GraphicsOverlay() }//boat range
    private val tapLocationsOverlayInner: GraphicsOverlay by lazy { GraphicsOverlay() }//boat range
    private val graphicsBoatOverlayLocation: GraphicsOverlay by lazy { GraphicsOverlay() }//boat range
    private var mapPoint: Point? = null//boat range
    private var trollingLocation: Location? = null

    private var isMapLoaded = false
    private var isTrolling = false
    private var isMeasureDistance = false

    // create a new Graphics Overlay
    private val graphicsOverlay: GraphicsOverlay by lazy { GraphicsOverlay() }
    private val graphicsOverlayLocation: GraphicsOverlay by lazy { GraphicsOverlay() }

    // create a picture marker symbol
    private val pinSourceSymbol: PictureMarkerSymbol? by lazy { createPinSymbol(R.drawable.ic_pin_new) }
    private val pinSourceLocationSymbol: PictureMarkerSymbol? by lazy { createLocationPinSymbol(R.drawable.ic_location_pin_icon) }
    private val pinSourceBoatLocationSymbol: PictureMarkerSymbol? by lazy { createPinSymbol(R.drawable.ic_boat_location_pin_home) }
    private var callout: Callout? = null

    val viewModel: LayersViewModel by activityViewModels()
    val trollingViewModel: TrollingViewModel by viewModels()
    val saveLocationsViewModel: LocationViewModel by viewModels()
    val viewModelSavedPoints: SavedLocationViewModel by activityViewModels()
    val viewModelSavedRoute: SavedRouteViewModel by activityViewModels()
    val syncSharedViewModel: SyncViewModel by activityViewModels()
    private var mServiceIntent: Intent? = null
    private var measureDistanceList: ArrayList<MeasureDistanceModel> = ArrayList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        sharedLayerViewModel = viewModel
        initAdapter()
        showHideLoader(true)



        binding.mapView.graphicsOverlays.add(graphicsOverlay)//for pin point
        binding.layoutLatLong.viewGone()
        initCircleOnMap()
        binding.mapView.graphicsOverlays.add(graphicsOverlayLocation)//for location pin point
        initListeners()
        initAnimations()
        if (requireContext().isNetworkAvailable()) {
            getLocation()
        } else {
            showHideLoader(false)
        }
        initViewModelResponse()
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

    @SuppressLint("MissingPermission")
    private fun initTrolling() {
        requireActivity().checkLocationPermission(childFragmentManager,
            object : PermissionListener {
                override fun onPermissionGranted() {
                    startLocationService()
                }

                override fun onPermissionCancel() {
                    Log.i("onPermissionCancel", "location")
                    binding.ivLayers.isClickable = false
                    binding.mapView.viewGone()
                    binding.locationPermissionLayout.viewVisible()
                    showHideLoader(false)
                }

            })
    }

    private fun setViewPointDetails(data: SearchData?) {
        binding.layoutViewSearchDetails.viewVisible()
        binding.tvPointDetails.text = data?.description ?: ""
        binding.tvPointTitle.text = data?.search_text ?: data?.name ?: ""
        binding.tvPointLatLang.text =
            "${data?.getLatitudeFormat() ?: ""}, ${data?.getLongitudeFormat() ?: ""}"
    }

    private fun applyLayerOpacity(data: MapLayer) {
        val dialog =
            LayerOpacityBottomSheet(data.layer_name, data.opacity ?: DEFAULT_LAYER_OPACITY.toInt(),
                object : LayerOpacityBottomSheet.LayerFilterClickListeners {
                    override fun onApplyFilter(opacityValue: Int) {
                        data.opacity = opacityValue
                        if (viewModel.layerToggleResponse.value.containsKey(data.layer_calling_name)) {
                            val wmsLayer =
                                viewModel.layerToggleResponse.value[data.layer_calling_name]
                            if (isMapLoaded && wmsLayer != null) {
                                if (data.self_hosted == 0 && wmsLayer.layer is WmsLayer) {
                                    wmsLayer.layer.opacity =
                                        (wmsLayer.data.opacity?.toFloat()
                                            ?: DEFAULT_LAYER_OPACITY) / 100F
                                } else if (wmsLayer.data.self_hosted == 1 && wmsLayer.layer is FeatureLayer) {
                                    wmsLayer.layer.opacity =
                                        (wmsLayer.data.opacity?.toFloat()
                                            ?: DEFAULT_LAYER_OPACITY) / 100F
                                } else if (wmsLayer.data.self_hosted == 0 && wmsLayer.layer is FeatureLayer) {
                                    wmsLayer.layer.opacity =
                                        (wmsLayer.data.opacity?.toFloat()
                                            ?: DEFAULT_LAYER_OPACITY) / 100F
                                }
                                viewModel.saveDataInSharedPreference()
                            }
                        }
                    }

                    override fun onApplyFilterClick(opacityValue: Int) {
                        data.opacity = opacityValue
                        if (viewModel.layerToggleResponse.value.containsKey(data.layer_calling_name)) {
                            val wmsLayer =
                                viewModel.layerToggleResponse.value[data.layer_calling_name]
                            if (isMapLoaded && wmsLayer != null) {
                                if (data.self_hosted == 0 && wmsLayer.layer is WmsLayer) {
                                    wmsLayer.layer.opacity =
                                        (wmsLayer.data.opacity?.toFloat()
                                            ?: DEFAULT_LAYER_OPACITY) / 100F
                                } else if (wmsLayer.data.self_hosted == 1 && wmsLayer.layer is FeatureLayer) {
                                    wmsLayer.layer.opacity =
                                        (wmsLayer.data.opacity?.toFloat()
                                            ?: DEFAULT_LAYER_OPACITY) / 100F
                                } else if (wmsLayer.data.self_hosted == 0 && wmsLayer.layer is FeatureLayer) {
                                    wmsLayer.layer.opacity =
                                        (wmsLayer.data.opacity?.toFloat()
                                            ?: DEFAULT_LAYER_OPACITY) / 100F
                                }
                                viewModel.saveDataInSharedPreference()
                            }
                        }
                    }

                })
        dialog.show(childFragmentManager, "LayerOpacityBottomSheet")
    }

    private fun loadMobileMapPackage(mapPath: String) {
        if (mapPath.isEmpty()) {
            requireActivity().showToast("Map path not found", false)
            return
        }
        // Create a vector tile cache from the local data.
        val mapPackage = MobileMapPackage(mapPath)
        mapPackage.loadAsync()
        mapPackage.addDoneLoadingListener {
            binding.mapView.map = mapPackage.maps.first()
        }
    }

    private fun initListeners() {
        binding.btnAddDestination.setOnClickListener {
//            if(destinationList.isNotEmpty()) {
//                destinationList.add(destinationList.size-1,AddDestinationModel(null))
//            }else{
//                destinationList.add(AddDestinationModel(null))
//            }
            destinationList.add(AddDestinationModel(null))
            binding.rvDestinations.adapter?.notifyDataSetChanged()
            binding.rvDestinations.scrollToPosition(destinationList.size - 1)
        }
        binding.backButtonLibrary.setOnClickListener {
            measureDistanceList.clear()
            binding.layoutMeasureDistance.viewGone()
            binding.layoutMeasureDistanceList.viewGone()
            graphicsOverlay.graphics.clear()
            graphicsOverlayLocation.graphics.clear()
            destinationList.clear()
            binding.rvDestinations.adapter?.notifyDataSetChanged()
            binding.layoutMeasureDistanceInitial.viewGone()
            binding.ivOfflineMap.viewVisible()
            isMeasureDistance = false
            binding.ivPlus.startAnimation(fab_open)
            binding.ivPlus.isClickable = true
        }
        binding.ivOfflineMap.setOnSingleClickListener {
            val dialog = OfflineMapBottomSheet(
                offlineMap,
                onlineMap != null,
                object : OfflineMapBottomSheet.OnItemClickListener {
                    override fun onMapSelect(selectedMap: OfflineMap?) {
                        if (selectedMap?.id != -1) {
                            if (offlineMap?.id != selectedMap?.id) {
                                offlineMap = selectedMap
                                loadMobileMapPackage(offlineMap?.mapPath ?: "")
                            } else {
                                offlineMap = selectedMap
                            }
                            binding.ivLayers.viewGone()
                            binding.ivOfflineMap.background = ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_toggle_map_loaded
                            )
                            binding.ivOfflineMap.setImageDrawable(
                                ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.ic_toggle_map_loaded
                                )
                            )
                            //binding.layoutSearch.viewGone()
                            //binding.ivCurrentLocation.viewGone()
                        } else {
                            if (offlineMap != null) {
                                if (onlineMap == null) {
                                    if (requireContext().isNetworkAvailable()) {
                                        latitude = null
                                        longitude = null
                                        showHideLoader(true)
                                        getLocation()
                                    } else {
                                        requireActivity().showToast(
                                            resources.getString(R.string.no_internet),
                                            false
                                        )
                                    }
                                } else {
                                    onlineMap?.let {
                                        binding.mapView.map = it
                                        binding.ivCurrentLocation.performClick()
                                    }
                                }
                            } else {
                                if (onlineMap == null) {
                                    if (requireContext().isNetworkAvailable()) {
                                        latitude = null
                                        longitude = null
                                        showHideLoader(true)
                                        getLocation()
                                    } else {
                                        requireActivity().showToast(
                                            resources.getString(R.string.no_internet),
                                            false
                                        )
                                    }
                                } else {
                                    onlineMap?.let {
                                        binding.mapView.map = it
                                        binding.ivCurrentLocation.performClick()
                                    }
                                }
                            }
                            binding.ivLayers.viewVisible()
                            binding.layoutSearch.viewVisible()
                            binding.ivCurrentLocation.viewVisible()
                            offlineMap = null
                            binding.ivOfflineMap.background = ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_toggle_map
                            )
                            binding.ivOfflineMap.setImageDrawable(
                                ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.ic_toggle_map
                                )
                            )
                        }
                    }
                })
            dialog.show(childFragmentManager, "OfflineMapBottomSheet")
        }
        binding.ivClearSearch.setOnSingleClickListener {
            binding.tvSearch.text = ""
            binding.ivClearSearch.viewGone()
            binding.layoutViewSearchDetails.viewGone()
            graphicsOverlay.graphics.clear()
            binding.mapView.callout.dismiss()
        }
        binding.ivCurrentLocation.setOnSingleClickListener {
            getLocation()
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
                            if (wmsLayer.data.layer_type == WMS_TYPE && wmsLayer.layer is WmsLayer) {
                                binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                wmsLayer.layer.opacity =
                                    (wmsLayer.data.opacity?.toFloat()
                                        ?: DEFAULT_LAYER_OPACITY) / 100F
                                wmsLayer.layer.isVisible = wmsLayer.data.isSelected
                                addLayerOnMap(wmsLayer.layer)
                            } else if (wmsLayer.data.layer_type == Self_Hosted_Type && wmsLayer.layer is FeatureLayer) {
                                binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                wmsLayer.layer.opacity =
                                    (wmsLayer.data.opacity?.toFloat()
                                        ?: DEFAULT_LAYER_OPACITY) / 100F
                                wmsLayer.layer.isVisible = wmsLayer.data.isSelected
                                addLayerOnMapFromPortalId(wmsLayer.layer)
                            } else if (wmsLayer.data.layer_type == Feature_Type && wmsLayer.layer is FeatureLayer) {
                                binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                wmsLayer.layer.opacity =
                                    (wmsLayer.data.opacity?.toFloat()
                                        ?: DEFAULT_LAYER_OPACITY) / 100F
                                wmsLayer.layer.isVisible = wmsLayer.data.isSelected
                                addLayerOnMapFromPortalId(wmsLayer.layer)
                            } else if (wmsLayer.data.layer_type == TILE && wmsLayer.layer is ArcGISTiledLayer) {
                                binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                wmsLayer.layer.opacity =
                                    (wmsLayer.data.opacity?.toFloat()
                                        ?: DEFAULT_LAYER_OPACITY) / 100F
                                wmsLayer.layer.isVisible = wmsLayer.data.isSelected
                                addLayerOnMapFromTile(wmsLayer.layer)
                            }


                        }
                    }
                    viewModel.saveDataInSharedPreference()
                }

                override fun onFilterApply(data: MapLayer) {
                    applyLayerOpacity(data)
                }

                override fun onViewAll() {
                    val sIntent = Intent(requireContext(), NavGraphActivity::class.java)
                    sIntent.putExtra(Constants.START_DESTINATION, StartDestination.ApplyLayers())
                    activityLauncher.launch(
                        sIntent,
                        object :
                            BaseActivityResult.OnActivityResult<ActivityResult> {
                            @SuppressLint("NotifyDataSetChanged")
                            override fun onActivityResult(result: ActivityResult) {
                                if (result.resultCode == Activity.RESULT_OK) {
                                    if (result.data != null) {
                                        if (result.data?.hasExtra("layer_name") == true) {
                                            val layerCallingName =
                                                result.data?.getStringExtra("layer_name") ?: ""
                                            if (viewModel.layerToggleResponse.value.containsKey(
                                                    layerCallingName
                                                )
                                            ) {
                                                val wmsLayer =
                                                    viewModel.layerToggleResponse.value[layerCallingName]
                                                wmsLayer?.let { data -> applyLayerOpacity(data.data) }
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
        binding.layoutSearch.setOnSingleClickListener {
            val sIntent = Intent(requireContext(), NavGraphActivity::class.java)
            sIntent.putExtra(
                Constants.START_DESTINATION,
                StartDestination.SearchLocations(isFromRoute = false)
            )
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
                                                binding.tvSearch.text =
                                                    searchModel.data.search_text
                                                        ?: searchModel.data.name
                                                                ?: ""
                                                binding.ivClearSearch.viewVisible()
                                                setViewPointDetails(searchModel.data)
                                                setupAddressSearchView(
                                                    searchModel.data.search_text
                                                        ?: searchModel.data.name ?: ""
                                                )
                                            } else {
                                                binding.tvSearch.text =
                                                    searchModel.data.search_text
                                                        ?: searchModel.data.name
                                                                ?: ""
                                                binding.ivClearSearch.viewVisible()
                                                setViewPointDetails(searchModel.data)
                                                geoCodeTypedAddress(
                                                    searchModel.data.search_text
                                                        ?: searchModel.data.name ?: ""
                                                )
                                            }
                                        } else {
                                            displaySearchResultOnMap(searchModel.data)
                                            binding.tvSearch.text =
                                                searchModel.data.search_text
                                                    ?: searchModel.data.name
                                                            ?: ""
                                            binding.ivClearSearch.viewVisible()
                                            setViewPointDetails(searchModel.data)
                                        }
                                    }
                                }
                            }
                        }
                    }
                })
        }
        binding.ivCrossPointDetails.setOnSingleClickListener {
            binding.ivClearSearch.performClick()
        }
        binding.ivCrossLocation.setOnSingleClickListener {
            binding.layoutLocation.viewGone()
            binding.layoutLatLong.viewGone()
            binding.ivPlus.startAnimation(fab_open)
            binding.ivPlus.isClickable = true
            graphicsOverlayLocation.graphics.clear()
            isLocationSaveUiActive = false
        }
        binding.ivSaveLocations.setOnSingleClickListener {
            if (isOpen) {
                hideOptions()
            }
            if (sharePreferenceHelper.isDoNotAskAgainLocationDialog()) {
                binding.layoutLatLong.viewVisible()
                binding.ivPlus.startAnimation(fab_hide)
                binding.ivPlus.isClickable = false
                binding.layoutLocation.viewVisible()
                isLocationSaveUiActive = true
                addMarkerOnMap(latitude ?: 0.0, longitude ?: 0.0)
            } else {
                val dialog = StartSaveLocationBottomSheet(
                    latitude,
                    longitude,
                    object : StartSaveLocationBottomSheet.OnStartClickListener {
                        override fun onStartClick(lat: Double?, long: Double?) {
                            binding.layoutLatLong.viewVisible()
                            binding.ivPlus.startAnimation(fab_hide)
                            binding.ivPlus.isClickable = false
                            binding.layoutLocation.viewVisible()
                            isLocationSaveUiActive = true
                            addMarkerOnMap(latitude ?: 0.0, longitude ?: 0.0)
//                    val dialog1 = SaveLocationBottomSheet(lat,long)
//                    dialog1.show(childFragmentManager,"SaveLocationBottomSheet")
                        }

                        override fun onCancelClick() {
                        }

                    })
                dialog.show(childFragmentManager, "StartSaveLocationBottomSheet")
            }
        }
        binding.ivTrolling.setOnSingleClickListener {
            if (!requireContext().isIgnoringBatteryOptimizations()) {
                if (isOpen) {
                    closeOptions()
                }
                val permissionDialogueDialogFragment =
                    PermissionBottomSheet(resources.getString(R.string.request_for_ignore_battery_optimization),
                        object : PermissionBottomSheet.OnItemClickListeners {
                            override fun onSettingsClick() {
                                startActivity(Intent().apply {
                                    action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                                })
                            }

                            override fun onCancelClick() {

                            }

                        })
                permissionDialogueDialogFragment.show(
                    childFragmentManager,
                    "permissionDialogueDialogFragment"
                )
            } else {
                startTrolling()
            }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                val intent = Intent()
//                val packageName: String = requireActivity().packageName
//                val pm = getSystemService(Context.POWER_SERVICE) as PowerManager?
//                if (!pm!!.isIgnoringBatteryOptimizations(packageName)) {
//                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
//                    intent.setData(Uri.parse("package:$packageName"))
//                    startActivity(intent)
//                }
//            }
        }
        binding.saveTrollingBtn.setOnClickListener {
            isTrolling = false
            val totalTrollingTime = trollingStartTime.getTrollingTime(totalTrollingPauseTime ?: 0)
            stopLocationService()
            handler.removeCallbacks(runableTimer)
            trollingStartTime = null
            trollingPauseTime = null
            totalTrollingPauseTime = null
            binding.tvTrollingTime.text = resources.getString(R.string._00_00)
            binding.layoutTrolling.viewGone()
            showBoatRange(isTrollingRange = isTrolling)
            binding.ivPlus.startAnimation(fab_open)
            binding.ivPlus.isClickable = true
            val dialog = SaveTrollingNameBottomSheet(object :
                SaveTrollingNameBottomSheet.OnItemClickListener {
                override fun onSaveClick(name: String) {
                    //syncSharedViewModel.updateSync(true)
                    trollingViewModel.updateTrollingName(name, totalTrollingTime ?: "00:00:00")
                    trollingLocation = null
                    trollingViewModel.syncTrolling()
                    /*Log.i("workRequestId", WorkerStarter.workRequestId.toString())
                    WorkerStarter.workRequestId?.let { requestId ->
                        WorkManager.getInstance(requireContext()).getWorkInfoByIdLiveData(requestId)
                            .observe(viewLifecycleOwner) { workInfo ->
                                if (workInfo != null) {
                                    when (workInfo.state) {
                                        WorkInfo.State.ENQUEUED -> {
                                            // Worker has been enqueued
                                            // You can start collecting output data here if needed
                                            Log.i("SaveFragmentListener", "WorkInfo.State.ENQUEUED ")
                                        }

                                        WorkInfo.State.RUNNING -> {
                                            // Worker is running
                                            // You can update UI or show progress if needed
                                            Log.i("SaveFragmentListener", "WorkInfo.State.RUNNING ")
                                        }

                                        WorkInfo.State.SUCCEEDED -> {
                                            // Worker has completed successfully
                                            // Retrieve output data
                                            // val outputData = workInfo.outputData
                                            // val result = outputData.getString("result")
                                            // Process the result as needed
                                            lifecycleScope.launch {
                                                delay(6000)
                                                Log.i("SaveFragmentListener", "WorkInfo.State.SUCCEEDED ")
                                                syncSharedViewModel.resetResponse()
                                            }
                                        }

                                        WorkInfo.State.FAILED -> {
                                            // Worker has failed
                                            // You can handle the failure scenario here
                                            Log.i("SaveFragmentListener", "WorkInfo.State.FAILED ")
                                            syncSharedViewModel.resetResponse()
                                        }

                                        WorkInfo.State.CANCELLED -> {
                                            // Worker has been cancelled
                                            // You can handle the cancellation scenario here
                                            Log.i("SaveFragmentListener", "WorkInfo.State.CANCELLED ")
                                            syncSharedViewModel.resetResponse()
                                        }
                                        // Handle other states as needed
                                        else -> {
                                            Log.i("SaveFragmentListener", "WorkInfo.State.Else ")
                                        }
                                    }
                                }
                            }
                    }*/
                }

            })
            dialog.show(childFragmentManager, "SaveTrollingNameBottomSheet")
        }
        binding.ivCrossTrolling.setOnClickListener {
            isTrolling = false
            stopLocationService()
            handler.removeCallbacks(runableTimer)
            trollingStartTime = null
            trollingPauseTime = null
            totalTrollingPauseTime = null
            binding.tvTrollingTime.text = resources.getString(R.string._00_00)
            binding.layoutTrolling.viewGone()
            showBoatRange(isTrollingRange = isTrolling)
            binding.ivPlus.startAnimation(fab_open)
            binding.ivPlus.isClickable = true
            trollingViewModel.deleteTrolling()
            trollingLocation = null
        }
        binding.ivStartStopTrolling.setOnSingleClickListener {
            if (isTrolling) {
                handler.removeCallbacks(runableTimer)
                trollingPauseTime = Date()
                isTrolling = false
                stopLocationService()
                binding.ivStartStopTrolling.setImageResource(R.drawable.ic_start_trolling)
            } else {
                isTrolling = true
                trollingPauseTime?.time?.let { pauseTime ->
                    totalTrollingPauseTime = (totalTrollingPauseTime ?: 0) + Date().time - pauseTime
                }
                handler.postDelayed(runableTimer, 1000)
                startLocationService()
                binding.ivStartStopTrolling.setImageResource(R.drawable.ic_pause_trolling)
            }
        }
        binding.buttonSaveLocation.setOnClickListener {
            val dialog1 = SaveLocationBottomSheet(
                latitude,
                longitude,
                listener = object : SaveLocationBottomSheet.OnClickListener {
                    override fun onSave() {
                        binding.layoutLatLong.viewGone()
                        binding.layoutLocation.viewGone()
                        binding.ivPlus.startAnimation(fab_open)
                        binding.ivPlus.isClickable = true
                        graphicsOverlayLocation.graphics.clear()
                        isLocationSaveUiActive = false
                        viewModelSavedPoints.getAllSavedLocations(1)
                    }

                })
            dialog1.show(childFragmentManager, "SaveLocationBottomSheet")
        }
        binding.savePointBtn.setOnSingleClickListener {
            if (trollingLocation != null) {
                saveLocationsViewModel.onRadioButtonChangeEvent(R.id.rbLocation)
                saveLocationsViewModel.onTrollingIdChangeEvent(trollingViewModel.trollingId.value)
                saveLocationsViewModel.onLatChangeEvent(
                    trollingLocation?.latitude?.toString() ?: ""
                )
                saveLocationsViewModel.onLangChangeEvent(
                    trollingLocation?.longitude?.toString() ?: ""
                )
                saveLocationsViewModel.onLatFormatChangeEvent(
                    getNauticalLatitude(trollingLocation?.latitude ?: 0.0)
                )
                saveLocationsViewModel.onLangFormatChangeEvent(
                    getNauticalLongitude(trollingLocation?.longitude ?: 0.0)
                )
                saveLocationsViewModel.onSaveClickEvent(binding.savePointBtn)
            }
        }
        binding.saveFishLogBtn.setOnSingleClickListener {
            if (trollingLocation != null) {
                saveLocationsViewModel.onRadioButtonChangeEvent(R.id.rbFishlog)
                saveLocationsViewModel.onTrollingIdChangeEvent(trollingViewModel.trollingId.value)
                saveLocationsViewModel.onLatChangeEvent(
                    trollingLocation?.latitude?.toString() ?: ""
                )
                saveLocationsViewModel.onLangChangeEvent(
                    trollingLocation?.longitude?.toString() ?: ""
                )
                saveLocationsViewModel.onLatFormatChangeEvent(
                    getNauticalLatitude(trollingLocation?.latitude ?: 0.0)
                )
                saveLocationsViewModel.onLangFormatChangeEvent(
                    getNauticalLongitude(trollingLocation?.longitude ?: 0.0)
                )
                saveLocationsViewModel.onSaveClickEvent(binding.savePointBtn)
            }
        }
        binding.nextButtonLibrary.setOnClickListener {
            val selectedList = getSelectedRoutes()
            if (selectedList.size >= 2) {
                binding.layoutMeasureDistanceInitial.viewGone()
                binding.layoutMeasureDistance.viewVisible()
                binding.layoutMeasureDistanceList.viewVisible()
                binding.expandable.expand()
                measureDistanceList.clear()
                measureDistance(selectedList)
            } else {
                if (selectedList.isEmpty()) {
                    requireActivity().showToast(
                        resources.getString(R.string.please_select_points_to_measure_distance),
                        false
                    )
                } else {
                    requireActivity().showToast(
                        resources.getString(R.string.error_points_to_measure_distance),
                        false
                    )
                }
            }
        }
        binding.ivDistance.setOnSingleClickListener {
            if (offlineMap != null) {
                requireActivity().showToast(
                    resources.getString(R.string.create_route_offline_error),
                    false
                )
                hideOptions()
                return@setOnSingleClickListener
            }
            if (isOpen) {
                hideOptions()
            }
            if (sharePreferenceHelper.isDoNotAskAgainRouteDialog()) {
                destinationList.clear()
                destinationList.add(AddDestinationModel(null))
                destinationList.add(AddDestinationModel(null))
                binding.rvDestinations.adapter?.notifyDataSetChanged()
                binding.layoutMeasureDistanceInitial.viewVisible()
                isMeasureDistance = true
                binding.ivPlus.startAnimation(fab_hide)
                binding.ivPlus.isClickable = false
                binding.ivOfflineMap.viewGone()
                zoomToBoatRange()
            } else {
                val dialog = MeasureDistanceBottomSheet(object :
                    MeasureDistanceBottomSheet.OnClickListeners {
                    override fun onCancel() {
                        measureDistanceList.clear()
                        isMeasureDistance = false
                        binding.ivPlus.startAnimation(fab_open)
                        binding.ivPlus.isClickable = true
                        binding.ivOfflineMap.viewVisible()
                    }

                    override fun startMeasureDistanceClick() {
                        measureDistanceList.clear()
                        isMeasureDistance = true
                        binding.ivPlus.startAnimation(fab_hide)
                        binding.ivPlus.isClickable = false
                        binding.ivOfflineMap.viewGone()
                        zoomToBoatRange()
                    }

                    override fun startMeasureFromLibraryClick() {
                        destinationList.clear()
                        destinationList.add(AddDestinationModel(null))
                        destinationList.add(AddDestinationModel(null))
                        binding.rvDestinations.adapter?.notifyDataSetChanged()
                        binding.layoutMeasureDistanceInitial.viewVisible()

                        isMeasureDistance = true
                        binding.ivPlus.startAnimation(fab_hide)
                        binding.ivPlus.isClickable = false
                        binding.ivOfflineMap.viewGone()
                        zoomToBoatRange()
                    }


                })
                dialog.show(childFragmentManager, "MeasureDistanceBottomSheet")
            }
        }
        /*binding.ivDistance.setOnSingleClickListener {
            if (isOpen) {
                hideOptions()
            }
            if (sharePreferenceHelper.isDoNotAskAgainRouteDialog()) {
                isMeasureDistance = true
                binding.ivPlus.startAnimation(fab_hide)
                binding.ivPlus.isClickable = false
                val library =
                    SelectLibraryBottomSheet(object : SelectLibraryBottomSheet.OnClickListeners {
                        override fun onCancel() {
                            measureDistanceList.clear()
                            isMeasureDistance = false
                            binding.ivPlus.startAnimation(fab_open)
                            binding.ivPlus.isClickable = true
                        }

                        override fun onNextClick(list: List<Any>) {
                            binding.layoutMeasureDistance.viewVisible()
                            binding.layoutMeasureDistanceList.viewVisible()
                            binding.expandable.expand()
                            measureDistanceList.clear()
                            measureDistance(list)
                        }

                    })
                library.show(childFragmentManager, "SelectLibraryBottomSheet")
            } else {
                val dialog = MeasureDistanceBottomSheet(object :
                    MeasureDistanceBottomSheet.OnClickListeners {
                    override fun onCancel() {
                        measureDistanceList.clear()
                        isMeasureDistance = false
                        binding.ivPlus.startAnimation(fab_open)
                        binding.ivPlus.isClickable = true
                    }

                    override fun startMeasureDistanceClick() {
                        measureDistanceList.clear()
                        isMeasureDistance = true
                        binding.ivPlus.startAnimation(fab_hide)
                        binding.ivPlus.isClickable = false
                    }

                    override fun startMeasureFromLibraryClick() {

                        isMeasureDistance = true
                        binding.ivPlus.startAnimation(fab_hide)
                        binding.ivPlus.isClickable = false
                        val library = SelectLibraryBottomSheet(object :
                            SelectLibraryBottomSheet.OnClickListeners {
                            override fun onCancel() {
                                measureDistanceList.clear()
                                isMeasureDistance = false
                                binding.ivPlus.startAnimation(fab_open)
                                binding.ivPlus.isClickable = true
                            }

                            override fun onNextClick(list: List<Any>) {
                                binding.layoutMeasureDistance.viewVisible()
                                binding.layoutMeasureDistanceList.viewVisible()
                                binding.expandable.expand()
                                measureDistanceList.clear()
                                measureDistance(list)
                            }

                        })
                        library.show(childFragmentManager, "SelectLibraryBottomSheet")
                    }


                })
                dialog.show(childFragmentManager, "MeasureDistanceBottomSheet")
            }
        }*/
        binding.backMeasureDistanceBtn.setOnSingleClickListener {
            //measureDistanceList.clear()
            binding.layoutMeasureDistance.viewGone()
            binding.layoutMeasureDistanceList.viewGone()
            //isMeasureDistance = false
            //binding.ivPlus.startAnimation(fab_open)
            //binding.ivPlus.isClickable = true
            //graphicsOverlay.graphics.clear()
            //graphicsOverlayLocation.graphics.clear()
            //destinationList.clear()
            binding.rvDestinations.adapter?.notifyDataSetChanged()
            binding.layoutMeasureDistanceInitial.viewVisible()
        }
        binding.deleteMeasureDistanceBtn.setOnSingleClickListener {
            measureDistanceList.clear()
            binding.layoutMeasureDistance.viewGone()
            binding.layoutMeasureDistanceList.viewGone()
            isMeasureDistance = false
            binding.ivPlus.startAnimation(fab_open)
            binding.ivPlus.isClickable = true
            graphicsOverlay.graphics.clear()
            graphicsOverlayLocation.graphics.clear()
            destinationList.clear()
            binding.rvDestinations.adapter?.notifyDataSetChanged()
            binding.layoutMeasureDistanceInitial.viewGone()
            binding.ivOfflineMap.viewVisible()
        }
        binding.saveRouteBtn.setOnSingleClickListener {
            val dialog = SaveRouteBottomSheet(measureDistanceList,
                object : SaveRouteBottomSheet.OnClickListener {
                    override fun onClose() {
                        binding.layoutMeasureDistance.viewGone()
                        binding.layoutMeasureDistanceList.viewGone()
                        binding.ivOfflineMap.viewVisible()
                        isMeasureDistance = false
                        binding.ivPlus.startAnimation(fab_open)
                        binding.ivPlus.isClickable = true
                        graphicsOverlay.graphics.clear()
                        graphicsOverlayLocation.graphics.clear()
                        measureDistanceList.clear()
                        viewModelSavedRoute.getAllSavedRoute(1)
                    }

                })
            dialog.show(childFragmentManager, "SaveRouteBottomSheet")
        }
        binding.ivExpand.setOnClickListener {
            if (binding.expandable.isExpanded) {
                binding.ivExpand.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_arrow_down
                    )
                )
            } else {
                binding.ivExpand.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_arrow_up
                    )
                )
            }
            binding.expandable.toggle()
        }
    }

    private fun getSelectedRoutes(): ArrayList<Any?> {
        val selectedList = ArrayList<Any?>()
        destinationList.forEach { item ->
            when (item) {
                is SavePointsData -> {
                    selectedList.add(item)
                }

                is SaveFishLogData -> {
                    selectedList.add(item)
                }

                is SearchData -> {
                    selectedList.add(item)
                }
            }
        }
        return selectedList
    }

    private fun startTrolling() {
        if (isOpen) {
            hideOptions()
        }
        if (sharePreferenceHelper.isDoNotAskAgainTrollingDialog()) {
            isTrolling = true
            binding.ivPlus.startAnimation(fab_hide)
            binding.ivPlus.isClickable = false
            binding.layoutTrolling.viewVisible()
            val time = Date()
            trollingViewModel.saveTrolling(time.time)
            Log.i("HomeTrollingId", time.time.toString())
            trollingStartTime = time
            handler.postDelayed(runableTimer, 1000)
            showBoatRange(isTrollingRange = isTrolling)
            initTrolling()
        } else {
            val dialog = RecordTrollingBottomSheet(object :
                RecordTrollingBottomSheet.OnTrollingListeners {
                override fun startTrolling() {
                    isTrolling = true
                    binding.ivPlus.startAnimation(fab_hide)
                    binding.ivPlus.isClickable = false
                    binding.layoutTrolling.viewVisible()
                    val time = Date()
                    trollingViewModel.saveTrolling(time.time)
                    Log.i("HomeTrollingId", time.time.toString())
                    trollingStartTime = time
                    handler.postDelayed(runableTimer, 1000)
                    showBoatRange(isTrollingRange = isTrolling)
                    initTrolling()
                }

            })
            dialog.show(childFragmentManager, "RecordTrollingBottomSheet")
        }
    }

    private fun showMeasureDistance(list: List<Any?>) {
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
                        when (it) {
                            is SavePointsData -> {
                                it.getLatFromString() ?: 0.0
                            }

                            is SaveFishLogData -> {
                                it.getLatFromString() ?: 0.0
                            }

                            is SearchData -> {
                                it.getLatFromString() ?: 0.0
                            }

                            else -> {
                                0.0
                            }
                        },
                        when (it) {
                            is SavePointsData -> {
                                it.getLangFromString() ?: 0.0
                            }

                            is SaveFishLogData -> {
                                it.getLangFromString() ?: 0.0
                            }

                            is SearchData -> {
                                it.getLangFromString() ?: 0.0
                            }

                            else -> {
                                0.0
                            }
                        },
                        if (offlineMap == null) {
                            MAP_SCALE
                        } else {
                            OFFLINE_MAP_SCALE_Zoom
                        }
                    )
                )
            }
            when (it) {
                is SavePointsData -> {
                    points.add(it.getLangFromString() ?: 0.0, it.getLatFromString() ?: 0.0)
                    addMultipleMarkerOnMap(
                        it.getLatFromString() ?: 0.0,
                        it.getLangFromString() ?: 0.0,
                        "Point ${index + 1}"
                    )
                }

                is SaveFishLogData -> {
                    points.add(it.getLangFromString() ?: 0.0, it.getLatFromString() ?: 0.0)
                    addMultipleMarkerOnMap(
                        it.getLatFromString() ?: 0.0,
                        it.getLangFromString() ?: 0.0,
                        "Point ${index + 1}"
                    )
                }

                is SearchData -> {
                    points.add(it.getLangFromString() ?: 0.0, it.getLatFromString() ?: 0.0)
                    addMultipleMarkerOnMap(
                        it.getLatFromString() ?: 0.0,
                        it.getLangFromString() ?: 0.0,
                        "Point ${index + 1}"
                    )
                }
            }
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
        binding.mapView.setViewpointGeometryAsync(graphicsOverlay.extent, MAP_PADDING)
    }

    private fun measureDistance(list: List<Any?>) {
        // create points for the line
        // create points for the line
        //SpatialReference.create(SPECIAL_REFERENCE_4326) //4326 is for Geographic coordinate systems (GCSs)
        val points = PointCollection(SpatialReference.create(SPECIAL_REFERENCE_4326))
        list.forEachIndexed { index, it ->
            when (it) {
                is SavePointsData -> {
                    points.add(it.getLangFromString() ?: 0.0, it.getLatFromString() ?: 0.0)
                    addMultipleMarkerOnMap(
                        it.getLatFromString() ?: 0.0,
                        it.getLangFromString() ?: 0.0,
                        "Point ${index + 1}"
                    )
                }

                is SaveFishLogData -> {
                    points.add(it.getLangFromString() ?: 0.0, it.getLatFromString() ?: 0.0)
                    addMultipleMarkerOnMap(
                        it.getLatFromString() ?: 0.0,
                        it.getLangFromString() ?: 0.0,
                        "Point ${index + 1}"
                    )
                }

                is SearchData -> {
                    points.add(it.getLangFromString() ?: 0.0, it.getLatFromString() ?: 0.0)
                    addMultipleMarkerOnMap(
                        it.getLatFromString() ?: 0.0,
                        it.getLangFromString() ?: 0.0,
                        "Point ${index + 1}"
                    )
                }
            }
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
        binding.mapView.setViewpointGeometryAsync(graphicsOverlay.extent, MAP_PADDING)
        lifecycleScope.launch {
            createUiForMeasureDistance(list)
        }
    }

    private fun createUiForMeasureDistance(list: List<Any?>) {
        val dataList = ArrayList<MeasureDistanceModel>()
        var totaldistance = 0F
        list.forEachIndexed { index, item ->
            /*if (index == 0) {
                binding.mapView.setViewpoint(
                    Viewpoint(
                        when (item) {
                            is SavePointsData -> {
                                item.getLatFromString() ?: 0.0
                            }

                            is SaveFishLogData -> {
                                item.getLatFromString() ?: 0.0
                            }

                            is SearchData -> {
                                item.getLatFromString() ?: 0.0
                            }

                            else -> {
                                0.0
                            }
                        },
                        when (item) {
                            is SavePointsData -> {
                                item.getLangFromString() ?: 0.0
                            }

                            is SaveFishLogData -> {
                                item.getLangFromString() ?: 0.0
                            }

                            is SearchData -> {
                                item.getLangFromString() ?: 0.0
                            }

                            else -> {
                                0.0
                            }
                        },
                        if (offlineMap == null) {
                            MAP_SCALE
                        } else {
                            OFFLINE_MAP_SCALE_Zoom
                        }
                    )
                )
            }*/
            if (index + 1 < list.size) {
                val results = FloatArray(1)
                Location.distanceBetween(
                    when (item) {
                        is SavePointsData -> {
                            item.getLatFromString() ?: 0.0
                        }

                        is SaveFishLogData -> {
                            item.getLatFromString() ?: 0.0
                        }

                        is SearchData -> {
                            item.getLatFromString() ?: 0.0
                        }

                        else -> {
                            0.0
                        }
                    },
                    when (item) {
                        is SavePointsData -> {
                            item.getLangFromString() ?: 0.0
                        }

                        is SaveFishLogData -> {
                            item.getLangFromString() ?: 0.0
                        }

                        is SearchData -> {
                            item.getLangFromString() ?: 0.0
                        }

                        else -> {
                            0.0
                        }
                    },
                    when (list[index + 1]) {
                        is SavePointsData -> {
                            (list[index + 1] as SavePointsData).getLatFromString() ?: 0.0
                        }

                        is SaveFishLogData -> {
                            (list[index + 1] as SaveFishLogData).getLatFromString() ?: 0.0
                        }

                        is SearchData -> {
                            (list[index + 1] as SearchData).getLatFromString() ?: 0.0
                        }

                        else -> {
                            0.0
                        }
                    },
                    when (list[index + 1]) {
                        is SavePointsData -> {
                            (list[index + 1] as SavePointsData).getLangFromString() ?: 0.0
                        }

                        is SaveFishLogData -> {
                            (list[index + 1] as SaveFishLogData).getLangFromString() ?: 0.0
                        }

                        is SearchData -> {
                            (list[index + 1] as SearchData).getLangFromString() ?: 0.0
                        }

                        else -> {
                            0.0
                        }
                    },
                    results
                )//Computes the approximate distance in meters between two locations
                totaldistance += (results[0] / 1000F).KmToNauticalMiles()
                val distance = if (results.isNotEmpty()) {
                    "${String.format("%.2f", (results[0] / 1000).KmToNauticalMiles())}NM"
                } else {
                    ""
                }
                dataList.add(MeasureDistanceModel(item, list[index + 1], distance))
            }
        }
        binding.tvTotalDistance.text = "Total Distance (${String.format("%.2f", totaldistance)}NM)"
        initAdapter(dataList)
    }


    private fun initAdapter(list: ArrayList<MeasureDistanceModel>) {
        measureDistanceList.addAll(list)
        binding.rvMeasureDistance.adapter = MeasureDistanceAdapter(list)
    }

    private fun initAdapter() {
        binding.rvDestinations.adapter = AddDestinationAdapter(
            destinationList,
            object : AddDestinationAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    val sIntent = Intent(requireContext(), NavGraphActivity::class.java)
                    sIntent.putExtra(
                        Constants.START_DESTINATION,
                        StartDestination.SearchLocations(isFromRoute = true)
                    )
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
                                                                ?: searchModel.data.name ?: "",
                                                            true,
                                                            position
                                                        )
                                                    } else {
                                                        geoCodeTypedAddressForRoute(
                                                            searchModel.data.search_text
                                                                ?: searchModel.data.name ?: "",
                                                            position
                                                        )
                                                    }
                                                } else {
                                                    destinationList[position] = searchModel.data
                                                    binding.rvDestinations.adapter?.notifyDataSetChanged()
                                                    showMeasureDistance(getSelectedRoutes())
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        })
                }

                override fun onItemCrossClick(position: Int) {
                    destinationList.removeAt(position)
                    binding.rvDestinations.adapter?.notifyDataSetChanged()
                    showMeasureDistance(getSelectedRoutes())
                }

                override fun onItemMapClick(position: Int) {
                    val pointList = ArrayList<SearchData>()
                    destinationList.forEach { item ->
                        if (item is SearchData) {
                            pointList.add(item)
                        }
                    }
                    val sIntent = Intent(requireContext(), NavGraphActivity::class.java)
                    sIntent.putExtra(
                        Constants.START_DESTINATION,
                        StartDestination.CommonMap(
                            MapUiData.GetLocationFromMap(
                                latitude = latitude,
                                longitude = longitude,
                                pointList
                            )
                        )
                    )
                    activityLauncher.launch(
                        sIntent,
                        object :
                            BaseActivityResult.OnActivityResult<ActivityResult> {
                            @SuppressLint("NotifyDataSetChanged")
                            override fun onActivityResult(result: ActivityResult) {
                                if (result.resultCode == Activity.RESULT_OK) {
                                    if (result.data != null) {
                                        if (result.data?.hasExtra("latitude") == true) {
                                            val lat = result.data?.getDoubleExtra("latitude", 0.0)
                                            val lang = result.data?.getDoubleExtra("longitude", 0.0)
                                            val model = SearchData(
                                                "",
                                                null,
                                                "${getNauticalLatitude(lat ?: 0.0)}, ${
                                                    getNauticalLongitude(lang ?: 0.0)
                                                }",
                                                "",
                                                -1,
                                                null,
                                                lat.toString(),
                                                lang.toString(),
                                                null,
                                                "1"
                                            )
                                            destinationList[position] = model
                                            binding.rvDestinations.adapter?.notifyDataSetChanged()
                                            showMeasureDistance(getSelectedRoutes())
                                        }
                                    }
                                }
                            }
                        })
                }

            })
    }

    private fun startLocationService() {
        mServiceIntent = Intent(requireContext(), LocationService::class.java)
        mServiceIntent?.putExtra("trollingId", trollingViewModel.trollingId.value)
        Log.i("startHomeTrollingId", trollingViewModel.trollingId.value.toString())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireActivity().startForegroundService(mServiceIntent)
        } else {
            requireActivity().startService(mServiceIntent)
        }
    }

    private fun stopLocationService() {
        mServiceIntent?.let { requireActivity().stopService(it) }
    }

    //TODO turn on gps if off
    private fun addMarkerOnMap(lat: Double, lang: Double) {
        // clear the current graphic position
        graphicsOverlayLocation.graphics.clear()
        // create a map point from lat lang
        val mapPoint = Point(lang, lat, SpatialReference.create(SPECIAL_REFERENCE_4326))
        val graphic = Graphic(mapPoint, pinSourceSymbol)
        graphicsOverlayLocation.graphics?.add(graphic)
        binding.tvLatitude.text = getNauticalLatitude(lat)
        binding.tvLongitude.text = getNauticalLongitude(lang)
    }

    private fun addMultipleMarkerOnMap(lat: Double, lang: Double, pointName: String?) {
        // clear the current graphic position
        // create a map point from lat lang
        val mapPoint = Point(lang, lat, SpatialReference.create(SPECIAL_REFERENCE_4326))
        val graphic = Graphic(mapPoint, pinSourceLocationSymbol)
        graphicsOverlayLocation.graphics?.add(graphic)
        if (!pointName.isNullOrEmpty()) {
            showCalloutRoute(graphic, pointName)
        }
    }

    private fun addBoatMarkerOnMap(lat: Double, lang: Double) {
        graphicsBoatOverlayLocation.graphics.clear()
        // clear the current graphic position
        // create a map point from lat lang
        val mapPoint = Point(lang, lat, SpatialReference.create(SPECIAL_REFERENCE_4326))
        val graphic = Graphic(mapPoint, pinSourceBoatLocationSymbol)
        graphicsBoatOverlayLocation.graphics?.add(graphic)
    }

    private fun initAnimations() {
        fab_close = AnimationUtils.loadAnimation(requireContext(), R.anim.fab_close)
        fab_hide = AnimationUtils.loadAnimation(requireContext(), R.anim.fab_hide)
        fab_open = AnimationUtils.loadAnimation(requireContext(), R.anim.fab_open)
        fab_clock = AnimationUtils.loadAnimation(requireContext(), R.anim.fab_rotate_clock)
        fab_anticlock = AnimationUtils.loadAnimation(requireContext(), R.anim.fab_rotate_anticlock)
        binding.ivPlus.setOnSingleClickListener {
            if (isOpen) {
                closeOptions()
            } else {
                openOptions()
            }
        }
    }

    private fun openOptions() {
        binding.ivTrolling.startAnimation(fab_open)
        binding.tvIvTrolling.startAnimation(fab_open)
        binding.tvIvSaveLocation.startAnimation(fab_open)
        binding.ivSaveLocations.startAnimation(fab_open)
        binding.tvIvMeasure.startAnimation(fab_open)
        binding.ivDistance.startAnimation(fab_open)
//        binding.ivDistance.isClickable = offlineMap == null && isMapLoaded
        binding.ivPlus.startAnimation(fab_clock)

        binding.ivTrolling.isClickable = true
        binding.ivSaveLocations.isClickable = true
        binding.ivPlus.setImageResource(R.drawable.ic_cross_home)
        isOpen = true
        binding.dimBackground.viewVisible()
    }

    private fun closeOptions() {
        binding.ivTrolling.startAnimation(fab_close)
        binding.tvIvTrolling.startAnimation(fab_close)
        binding.tvIvSaveLocation.startAnimation(fab_close)
        binding.ivSaveLocations.startAnimation(fab_close)
        binding.tvIvMeasure.startAnimation(fab_close)
        binding.ivDistance.startAnimation(fab_close)
        binding.ivPlus.startAnimation(fab_anticlock)
        binding.ivTrolling.isClickable = false
        binding.ivSaveLocations.isClickable = false
        //binding.ivDistance.isClickable = false
        binding.ivPlus.setImageResource(R.drawable.ic_add_home)
        isOpen = false
        binding.dimBackground.viewGone()
    }

    private fun hideOptions() {
        binding.ivTrolling.startAnimation(fab_hide)
        binding.tvIvTrolling.startAnimation(fab_hide)
        binding.tvIvSaveLocation.startAnimation(fab_hide)
        binding.ivSaveLocations.startAnimation(fab_hide)
        binding.ivDistance.startAnimation(fab_hide)
        binding.tvIvMeasure.startAnimation(fab_hide)
        binding.ivPlus.startAnimation(fab_anticlock)
        binding.ivTrolling.isClickable = false
        binding.ivSaveLocations.isClickable = false
        //binding.ivDistance.isClickable = false
        binding.ivPlus.setImageResource(R.drawable.ic_add_home)
        isOpen = false
        binding.dimBackground.viewGone()
    }

    private fun zoomToBoatRange() {
        val user = sharePreferenceHelper.getUser()
        if (user?.vessel != null && user.vessel?.getLatFromString() != null && user.vessel?.getLangFromString() != null) {
            binding.mapView.setViewpoint(
                Viewpoint(
                    user.vessel?.getLatFromString() ?: 34.0270,
                    user.vessel?.getLangFromString() ?: -118.8050,
                    if (offlineMap == null) {
                        MAP_SCALE
                    } else {
                        OFFLINE_MAP_SCALE_Zoom
                    }
                )
            )
        }
    }

    private fun showBoatRange(isTrollingRange: Boolean) {
        graphicsBoatOverlayLocation.graphics.clear()
        planarGraphicsOverlay.graphics.clear()
        geodesicGraphicsOverlay.graphics.clear()
        tapLocationsOverlay.graphics.clear()
        planarGraphicsOverlayInner.graphics.clear()
        geodesicGraphicsOverlayInner.graphics.clear()
        tapLocationsOverlayInner.graphics.clear()
        val user = sharePreferenceHelper.getUser()
        if (user?.vessel != null && !isTrollingRange) {
            if (user.user_configuration?.boat_range == 1) {
                mapPoint = Point(
                    user.vessel?.getLangFromString() ?: 0.0,
                    user.vessel?.getLatFromString() ?: 0.0,
                    SpatialReference.create(SPECIAL_REFERENCE_4326)
                )
                if (!user.vessel?.range.isNullOrEmpty()) {
                    drawCircleOnMap(user.vessel?.range ?: "")
                }
            }
            addBoatMarkerOnMap(
                lat = user.vessel?.getLatFromString() ?: 0.0,
                lang = user.vessel?.getLangFromString() ?: 0.0
            )
        } else if (user?.vessel != null && isTrollingRange) {
            mapPoint = Point(
                longitude ?: 0.0,
                latitude ?: 0.0, SpatialReference.create(SPECIAL_REFERENCE_4326)
            )
            if (!user.vessel?.range.isNullOrEmpty()) {
                drawCircleOnMap(user.vessel?.range ?: "")
            }
            graphicsBoatOverlayLocation.graphics.clear()
        }
    }

    private fun initMap() {
        //ArcGISRuntimeEnvironment.setApiKey(resources.getString(R.string.api_key))
        ArcGISRuntimeEnvironment.setLicense("runtimelite,1000,rud8211878644,none,ZZ0RJAY3FLED0YRJD186")

        // create and add a map with a light gray basemap style

        // apply mapView assignments
        lifecycleScope.launch {
            binding.mapView.apply {
                interactionOptions.isMagnifierEnabled = true
                val selectedMap = sharePreferenceHelper.getSavedMap()
                binding.mapView.map = ArcGISMap(
                    if (!selectedMap.isNullOrEmpty()) {
                        BasemapStyle.valueOf(selectedMap)
                    } else {
                        BasemapStyle.ARCGIS_OCEANS
                    }
                )
//                AuthenticationManager.setAuthenticationChallengeHandler {
//                    val userCredential = UserCredential("dev.fishthebreak", "18241Killingit1108!")
//                    val response = AuthenticationChallengeResponse(
//                        AuthenticationChallengeResponse.Action.CONTINUE_WITH_CREDENTIAL,
//                        userCredential
//                    )
//                    Log.d(TAG, "Responding to authentication challenge with provided credentials.")
//                    response
//                }
                // Set the Authentication Challenge Handler
                // Set the Authentication Challenge Handler
                val authenticationChallengeHandler =
                    MyAuthenticationChallengeHandler("dev.fishthebreak", "18241Killingit1108!")
                AuthenticationManager.setAuthenticationChallengeHandler(
                    authenticationChallengeHandler
                )

                val locationDisplay = binding.mapView.locationDisplay
                locationDisplay.autoPanMode = LocationDisplay.AutoPanMode.NAVIGATION
                locationDisplay.startAsync()
//                binding.mapView.locationDisplay.addLocationChangedListener { liveLocation->
//                    val projectedPoint =
//                        GeometryEngine.project(liveLocation.location.position, SpatialReference.create(SPECIAL_REFERENCE_4326))
//                    // create a map point from the screen point
//                    val locationPoint = projectedPoint as Point
//                    longitude = locationPoint.x
//                    latitude = locationPoint.y
//                    binding.tvLatitude.text = latitude.toString()
//                    binding.tvLongitude.text = longitude.toString()
//                }
                binding.mapView.setViewpoint(
                    Viewpoint(
                        latitude ?: 34.0270,
                        longitude ?: -118.8050,
                        if (offlineMap == null) {
                            MAP_SCALE
                        } else {
                            OFFLINE_MAP_SCALE_Zoom
                        }
//                        72000.0
                    )
                )
//                graphicsOverlays.add(graphicsOverlay)//for pin point
//                graphicsOverlays.add(graphicsOverlayLocation)//for location pin point
//                initCircleOnMap()
                binding.mapView.map.addDoneLoadingListener {
                    if (binding.mapView.map.loadStatus == LoadStatus.LOADED) {
                        showBoatRange(isTrollingRange = isTrolling)
                        onlineMap = binding.mapView.map
                        isMapLoaded = true
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
                                } else if (wmsLayersStatusModel.data.layer_type == TILE && wmsLayersStatusModel.layer is ArcGISTiledLayer) {
                                    binding.mapView.map.operationalLayers.remove(
                                        wmsLayersStatusModel.layer
                                    )
                                    wmsLayersStatusModel.layer.opacity =
                                        (wmsLayersStatusModel.data.opacity?.toFloat()
                                            ?: DEFAULT_LAYER_OPACITY) / 100F
                                    wmsLayersStatusModel.layer.isVisible =
                                        wmsLayersStatusModel.data.isSelected
                                    addLayerOnMapFromTile(wmsLayersStatusModel.layer)
                                }
                            }
                        }
                    } else if (binding.mapView.map.loadStatus != LoadStatus.LOADED) {
                        val error =
                            "Failed to load portal item ${binding.mapView.map.loadError.message}"
                        val error1 =
                            "Failed to load portal item ${binding.mapView.map.loadError.cause?.message}"
                        Log.e("addDoneLoadingListener", error)
                        Log.e("addDoneLoadingListener1", error1)
                        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                        return@addDoneLoadingListener
                    }
                    showHideLoader(false)
                }
            }
            initMapTouchListener()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initMapTouchListener() {
        binding.mapView.addViewpointChangedListener { p0 ->
            if (isLocationSaveUiActive) {
                // clear the current graphic position
                graphicsOverlayLocation.graphics.clear()
                // create a screen point from where the user tapped
                val viewPoint = p0?.source?.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE)
                val screenPoint = Point(
                    viewPoint?.targetGeometry?.extent?.center?.x ?: 0.0,
                    viewPoint?.targetGeometry?.extent?.center?.y ?: 0.0,
                    SpatialReference.create(SPECIAL_REFERENCE_3857)
                )
                val projectedPoint =
                    GeometryEngine.project(
                        screenPoint,
                        SpatialReference.create(SPECIAL_REFERENCE_4326)
                    )
                // create a map point from the screen point
                val locationPoint = projectedPoint as Point
                longitude = locationPoint.x
                latitude = locationPoint.y
                binding.tvLatitude.text = getNauticalLatitude(latitude ?: 0.0)
                binding.tvLongitude.text = getNauticalLongitude(longitude ?: 0.0)
                val graphic = Graphic(projectedPoint, pinSourceSymbol)
                graphicsOverlayLocation.graphics?.add(graphic)
            }
        }
        binding.mapView.onTouchListener =
            object : DefaultMapViewOnTouchListener(requireContext(), binding.mapView) {
                // drag a graphic to a new point on map
                override fun onDoubleTouchDrag(e: MotionEvent): Boolean {

                    when (e.action) {
                        // user selected the graphic
                        MotionEvent.ACTION_DOWN -> {

                        }
                        // user is moving the graphic
                        MotionEvent.ACTION_MOVE -> {

                        }
                        // user released the graphic
                        MotionEvent.ACTION_UP -> {
                            // release selected graphic

                        }
                    }
                    return true
                }

                // place a new graphic on map
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    if (binding.mapView.callout.isShowing) {
                        binding.mapView.callout.dismiss()
                    }
//                   e.let {
//                       val screenPoint = android.graphics.Point(
//                           it.x.roundToInt(),
//                           it.y.roundToInt()
//                       )
//                       identifyResult(screenPoint)
//                   }
                    return true
                }
            }
        /*binding.mapView.onTouchListener =
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
            }*/
    }

    /**
     * Performs an identify on layers at the given screenpoint and calls handleIdentifyResults(...) to process them.
     *
     * @param screenPoint in Android graphic coordinates.
     */
    private fun identifyResult(screenPoint: android.graphics.Point) {

        val identifyLayerResultsFuture = binding.mapView
            .identifyLayersAsync(screenPoint, 12.0, false, 10)

        identifyLayerResultsFuture.addDoneListener {
            try {
                val identifyLayerResults = identifyLayerResultsFuture.get()
                handleIdentifyResults(identifyLayerResults)
            } catch (e: Exception) {
                logError("Error identifying results ${e.message}")
            }
        }
    }

    /**
     * Processes identify results into a string which is passed to showAlertDialog(...).
     *
     * @param identifyLayerResults a list of identify results generated in identifyResult().
     */
    private fun handleIdentifyResults(identifyLayerResults: List<IdentifyLayerResult>) {
        val message = StringBuilder()
        var totalCount = 0
        for (identifyLayerResult in identifyLayerResults) {
            val count = recursivelyCountIdentifyResultsForSublayers(identifyLayerResult)
            val layerName = identifyLayerResult.layerContent.name
            message.append(layerName).append(": ").append(count)

            // add new line character if not the final element in array
            if (identifyLayerResult != identifyLayerResults[identifyLayerResults.size - 1]) {
                message.append("\n")
            }
            totalCount += count
        }

        // if any elements were found show the results, else notify user that no elements were found
        if (totalCount > 0) {
            showAlertDialog(message)
        } else {
            logError("No element found")
        }
    }

    /**
     * Shows message in an AlertDialog.
     *
     * @param message contains identify results processed into a string.
     */
    private fun showAlertDialog(message: StringBuilder) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())

        // set title
        alertDialogBuilder.setTitle("Number of elements found")

        // set dialog message
        alertDialogBuilder
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Ok") { dialog, which -> }

        // create alert dialog
        val alertDialog = alertDialogBuilder.create()

        // show the alert dialog
        alertDialog.show()
    }

    /**
     * Log an error to logcat and to the screen via Toast.
     * @param message the text to log.
     */
    private fun logError(message: String?) {
        message?.let {
            Log.e(
                TAG,
                message
            )
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Gets a count of the GeoElements in the passed result layer.
     * This method recursively calls itself to descend into sublayers and count their results.
     * @param result from a single layer.
     * @return the total count of GeoElements.
     */
    private fun recursivelyCountIdentifyResultsForSublayers(result: IdentifyLayerResult): Int {
        var subLayerGeoElementCount = 0

        for (sublayerResult in result.sublayerResults) {
            // recursively call this function to accumulate elements from all sublayers
            subLayerGeoElementCount += recursivelyCountIdentifyResultsForSublayers(sublayerResult)
        }

        return subLayerGeoElementCount + result.elements.size
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
                                if (latitude == null) {
                                    latitude = location.latitude
                                    longitude = location.longitude
                                    initMap()
                                } else {
                                    if (binding.mapView.locationDisplay != null) {
                                        if (binding.mapView.locationDisplay.location != null) {
                                            if (binding.mapView.locationDisplay.location.position != null) {
                                                val point =
                                                    binding.mapView.locationDisplay.location.position
                                                // Zoom to current location with magnification 1000.
                                                binding.mapView.setViewpointCenterAsync(
                                                    point,
                                                    if (offlineMap == null) {
                                                        CURRENT_LOCATION_SCALE
                                                    } else {
                                                        OFFLINE_MAP_SCALE_Zoom
                                                    }
                                                )
                                                Log.d("Latitude", "${point.x}")
                                                Log.d("Longitude", "${point.y}")
                                            }
                                        }
                                    }
//                                    binding.mapView.setViewpoint(
//                                        Viewpoint(
//                                            latitude ?: 34.0270,
//                                            longitude ?: -118.8050,
//                                            MAP_SCALE
////                        72000.0
//                                        )
//                                    )
                                }
                            }

                        }
                }

                override fun onPermissionCancel() {
                    Log.i("onPermissionCancel", "location")
                    binding.ivLayers.isClickable = false
                    binding.mapView.viewGone()
                    binding.locationPermissionLayout.viewVisible()
                    showHideLoader(false)
                    //startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }

            })
    }

    private fun setupAddressSearchView(
        address: String,
        forRoute: Boolean = false,
        position: Int = -1
    ) {
        addressGeocodeParameters = GeocodeParameters().apply {
            // get place name and street address attributes
            resultAttributeNames.addAll(listOf("PlaceName", "Place_addr"))
            // return only the closest result
            maxResults = 1
            if (!forRoute) {
                geoCodeTypedAddress(address)
            } else {
                geoCodeTypedAddressForRoute(address, position)
            }
        }
    }

    /**
     * Geocode an address passed in by the user.
     *
     * @param address the address read in from searchViews
     */
    private fun geoCodeTypedAddress(address: String) {
        if (address.isValidLatLangNauticalFormat()) {
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
            setViewPointDetails(searchData)
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

    private fun geoCodeTypedAddressForRoute(address: String, position: Int) {
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
                            val loc = geocodeResults.first()
                            loc.displayLocation
                            val projectedPoint =
                                GeometryEngine.project(
                                    loc.displayLocation,
                                    SpatialReference.create(SPECIAL_REFERENCE_4326)
                                )
                            // create a map point from the screen point
                            val locationPoint = projectedPoint as Point
                            val longitude_ = locationPoint.x
                            val latitude_ = locationPoint.y
                            val model = SearchData(
                                "",
                                null,
                                address,
                                "",
                                -1,
                                null,
                                latitude_.toString(),
                                longitude_.toString(),
                                null,
                                "1"
                            )
                            destinationList[position] = model
                            binding.rvDestinations.adapter?.notifyDataSetChanged()
                            showMeasureDistance(getSelectedRoutes())
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
        binding.tvPointLatLang.text =
            "${getNauticalLatitude(resultPoint.y)}, ${getNauticalLongitude(resultPoint.x)}"
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
                data.getLangFromString() ?: longitude ?: 0.0,
                data.getLatFromString() ?: latitude ?: 0.0,
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
    /*
        private fun displaySearchResultOnMap(data: SearchData) {
            try {
                // clear graphics overlay of existing graphics
                graphicsOverlay.graphics.clear()
                // create graphic object for resulting location
                // create a map point from lat lang
                val mapPoint = Point(
                    data.longitude?.toDouble() ?: longitude?:0.0,
                    data.latitude?.toDouble() ?: latitude?:0.0,
                    SpatialReference.create(SPECIAL_REFERENCE_4326)//3857
                )
                val resultLocationGraphic = Graphic(mapPoint, pinSourceSymbol)

                // add graphic to location layer
                graphicsOverlay.graphics.add(resultLocationGraphic)
    //            binding.mapView.setViewpointAsync(Viewpoint(mapPoint.extent), 1f).addDoneListener {
    //                showCallout(resultLocationGraphic, data)
    //            }
                binding.mapView.setViewpoint(Viewpoint(
                    data.latitude?.toDouble() ?: latitude?:0.0,
                    data.longitude?.toDouble() ?: longitude?:0.0,
                    SEARCH_MAP_SCALE_Zoom
    //                        72000.0
                ))
                showCallout(resultLocationGraphic, data)
    //            binding.mapView.setViewpointAsync(
    //                Viewpoint(
    //                    data.latitude?.toDouble() ?: 34.0270,
    //                    data.longitude?.toDouble() ?: -118.8050,
    //                    SEARCH_MAP_SCALE_Zoom
    ////                        72000.0
    //                ), 0.5f
    //            ).addDoneListener {
    //                showCallout(resultLocationGraphic, data)
    //            }
            } catch (ex: Exception) {

            }
        }
    */
    /**
     * Identifies and shows a call out on a tapped graphic.
     *
     * @param motionEvent the motion event containing a tapped screen point
     */
    private fun identifyGraphic(motionEvent: MotionEvent) {
        // get the screen point
        val screenPoint: android.graphics.Point = android.graphics.Point(
            motionEvent.x.roundToInt(), motionEvent.y.roundToInt()
        )
        // from the graphics overlay, get the graphics near the tapped location
        val identifyResultsFuture: ListenableFuture<IdentifyGraphicsOverlayResult> =
            binding.mapView.identifyGraphicsOverlayAsync(graphicsOverlay, screenPoint, 10.0, false)
        identifyResultsFuture.addDoneListener {
            try {
                val identifyGraphicsOverlayResult: IdentifyGraphicsOverlayResult =
                    identifyResultsFuture.get()
                val graphics = identifyGraphicsOverlayResult.graphics
                // get the first graphic identified
                if (graphics.isNotEmpty()) {
                    val identifiedGraphic: Graphic = graphics[0]
                    // show the callout of the identified graphic
                    showCallout(identifiedGraphic)
                } else {
                    // dismiss the callout if no graphic is identified (e.g. tapping away from the graphic)
                    callout?.dismiss()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Identify error: " + e.message)
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
            if (binding.tvPointTitle.text.toString()
                    .trim() != graphic.attributes["Place_addr"].toString().trim()
            ) {
                binding.tvPointDetails.text = graphic.attributes["Place_addr"].toString()
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
     *  Creates a picture marker symbol from the pin icon, and sets it to half of its original size.
     */
    private fun createPinSymbol(@DrawableRes icon: Int): PictureMarkerSymbol? {
        val pinDrawable =
            ContextCompat.getDrawable(requireContext(), icon) as BitmapDrawable?
        val pinSymbol: PictureMarkerSymbol
        try {
            pinSymbol = PictureMarkerSymbol.createAsync(pinDrawable).get()
            pinSymbol.width = 48f
            pinSymbol.height = 48f
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

    private fun createLocationPinSymbol(@DrawableRes icon: Int): PictureMarkerSymbol? {
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

    override fun onResume() {
//        if(isMapLoaded){
//            viewModel.refreshAllLayers()
//        }
        super.onResume()
        binding.mapView.resume()
        showBoatRange(isTrollingRange = isTrolling)
    }

    override fun onPause() {
        binding.mapView.pause()
        super.onPause()
    }

    override fun onDestroy() {
        binding.mapView.dispose()
        stopLocationService()
        try {
            handler.removeCallbacks(runableTimer)
        } catch (ex: Exception) {

        }
        super.onDestroy()
    }

    private fun showError(message: String) {
        Log.e("HomeFragment", message)
        Snackbar.make(binding.mapView, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun initViewModelResponse() {
        lifecycleScope.launch {
            trollingViewModel.trollingResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    if (response != null) {
                        if (response > 0) {
                            requireActivity().showToast(
                                resources.getString(R.string.trolling_save_success),
                                true
                            )
                        } else {
                            requireActivity().showToast(
                                resources.getString(R.string.error_save_trolling),
                                false
                            )
                        }
                        //trollingViewModel.getTrolling()
                        trollingViewModel.resetResponse()
                    }
                }
        }
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
                                    if (wmsLayer.data.layer_type == WMS_TYPE && wmsLayer.layer is WmsLayer) {
                                        binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                    } else if (wmsLayer.data.layer_type == Self_Hosted_Type && wmsLayer.layer is FeatureLayer) {
                                        binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                    } else if (wmsLayer.data.layer_type == Feature_Type && wmsLayer.layer is FeatureLayer) {
                                        binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                    } else if (wmsLayer.data.layer_type == TILE && wmsLayer.layer is ArcGISTiledLayer) {
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
                                if (wmsLayer.data.layer_type == WMS_TYPE && wmsLayer.layer is WmsLayer) {
                                    binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                    wmsLayer.layer.opacity =
                                        (wmsLayer.data.opacity?.toFloat()
                                            ?: DEFAULT_LAYER_OPACITY) / 100F
                                    wmsLayer.layer.isVisible = wmsLayer.data.isSelected
                                    addLayerOnMap(wmsLayer.layer)
                                } else if (wmsLayer.data.layer_type == Self_Hosted_Type && wmsLayer.layer is FeatureLayer) {
                                    binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                    wmsLayer.layer.opacity =
                                        (wmsLayer.data.opacity?.toFloat()
                                            ?: DEFAULT_LAYER_OPACITY) / 100F
                                    wmsLayer.layer.isVisible = wmsLayer.data.isSelected
                                    addLayerOnMapFromPortalId(wmsLayer.layer)
                                } else if (wmsLayer.data.layer_type == Feature_Type && wmsLayer.layer is FeatureLayer) {
                                    binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                    wmsLayer.layer.opacity =
                                        (wmsLayer.data.opacity?.toFloat()
                                            ?: DEFAULT_LAYER_OPACITY) / 100F
                                    wmsLayer.layer.isVisible = wmsLayer.data.isSelected
                                    addLayerOnMapFromPortalId(wmsLayer.layer)
                                } else if (wmsLayer.data.layer_type == TILE && wmsLayer.layer is ArcGISTiledLayer) {
                                    binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                    wmsLayer.layer.opacity =
                                        (wmsLayer.data.opacity?.toFloat()
                                            ?: DEFAULT_LAYER_OPACITY) / 100F
                                    wmsLayer.layer.isVisible = wmsLayer.data.isSelected
                                    addLayerOnMapFromTile(wmsLayer.layer)
                                }
                            }
                        }
                        //viewModel.saveDataInSharedPreference()
                    }
                }
        }
        /*lifecycleScope.launch {
            viewModel.layerToggleResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                //.distinctUntilChanged()
                .collect { response ->
                    if (response.isNotEmpty()) {
                        if (viewModel.clearAllLayersResponse.value) {
                            activeLayers.forEach { key, wmsLayer ->
                                if (isMapLoaded) {
                                    if (wmsLayer.data.self_hosted == 0 && wmsLayer.layer is WmsLayer) {
                                        binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                    } else if (wmsLayer.data.self_hosted == 1 && wmsLayer.layer is FeatureLayer) {
                                        binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                    } else if (wmsLayer.data.self_hosted == 0 && wmsLayer.layer is FeatureLayer) {
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
                                if (wmsLayer.data.self_hosted == 0 && wmsLayer.layer is WmsLayer) {
                                    binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                    wmsLayer.layer.opacity =
                                        (wmsLayer.data.opacity?.toFloat() ?: DEFAULT_LAYER_OPACITY) / 100F
                                    wmsLayer.layer.isVisible = wmsLayer.data.isSelected
                                    addLayerOnMap(wmsLayer.layer)
                                } else if (wmsLayer.data.self_hosted == 1 && wmsLayer.layer is FeatureLayer) {
                                    binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                    wmsLayer.layer.opacity =
                                        (wmsLayer.data.opacity?.toFloat() ?: DEFAULT_LAYER_OPACITY) / 100F
                                    wmsLayer.layer.isVisible = wmsLayer.data.isSelected
                                    addLayerOnMapFromPortalId(wmsLayer.layer)
                                } else if (wmsLayer.data.self_hosted == 0 && wmsLayer.layer is FeatureLayer) {
                                    binding.mapView.map.operationalLayers.remove(wmsLayer.layer)
                                    wmsLayer.layer.opacity =
                                        (wmsLayer.data.opacity?.toFloat() ?: DEFAULT_LAYER_OPACITY) / 100F
                                    wmsLayer.layer.isVisible = wmsLayer.data.isSelected
                                    addLayerOnMapFromPortalId(wmsLayer.layer)
                                }
                            }
                        }
                        //viewModel.saveDataInSharedPreference()
                    }
                }
        }*/

        lifecycleScope.launch {
            saveLocationsViewModel.savePointResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true) {
                                requireActivity().showToast(
                                    response.data.message, true
                                )
                            } else {
                                requireActivity().showToast(
                                    response.data?.message ?: resources.getString(
                                        R.string.something_went_wrong
                                    ), false
                                )
                                saveLocationsViewModel.resetResponse()
                            }
                        }

                        is NetworkResult.Error -> {
                            showHideLoader(false)
                            requireActivity().showToast(
                                response.message.getOnErrorMessage(),
                                false
                            )
                            saveLocationsViewModel.resetResponse()
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
                            saveLocationsViewModel.resetResponse()
                        }

                        is NetworkResult.NoCallYet -> {
                        }
                    }
                }
        }
        lifecycleScope.launch {
            saveLocationsViewModel.saveFishLogResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            showHideLoader(false)
                            if (response.data?.status == true) {
                                requireActivity().showToast(
                                    response.data.message, true
                                )
                            } else {
                                requireActivity().showToast(
                                    response.data?.message ?: resources.getString(
                                        R.string.something_went_wrong
                                    ), false
                                )
                                saveLocationsViewModel.resetResponse()
                            }
                        }

                        is NetworkResult.Error -> {
                            showHideLoader(false)
                            requireActivity().showToast(
                                response.message.getOnErrorMessage(),
                                false
                            )
                            saveLocationsViewModel.resetResponse()
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
                            saveLocationsViewModel.resetResponse()
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

    private fun addLayerOnMapFromPortal(layer: FeatureLayer) {
        val portalItemId = "portal id here"
        val portal = Portal(resources.getString(R.string.portal_url), false)
        val portalItem = PortalItem(portal, portalItemId)

        val layer = FeatureLayer(portalItem)
        layer.definitionExpression = "esrignss_receiver_id = '2'"

    }

    private fun showHideLoader(visibility: Boolean) {
        if (visibility) {
            binding.loaderLayout.viewVisible()
        } else {
            binding.loaderLayout.viewGone()
        }
    }

    private fun initCircleOnMap() {
        // create a graphics overlay to contain the buffered geometry graphics
        // create a graphics overlay to contain the buffered geometry graphics
        val graphicsOverlay = GraphicsOverlay()
        binding.mapView.graphicsOverlays.add(graphicsOverlay)

        // create a fill symbol for geodesic buffer polygons

        // create a fill symbol for geodesic buffer polygons
        val geodesicOutlineSymbol =
            SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.parseColor("#2DA0FA"), 2f)
        val geodesicBufferFillSymbol = SimpleFillSymbol(
            SimpleFillSymbol.Style.SOLID, android.R.color.transparent,
            geodesicOutlineSymbol
        )

        // create a fill symbol for planar buffer polygons

        // create a fill symbol for planar buffer polygons
        val planarOutlineSymbol =
            SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, android.R.color.transparent, 2f)
        val planarBufferFillSymbol = SimpleFillSymbol(
            SimpleFillSymbol.Style.SOLID, android.R.color.transparent,
            planarOutlineSymbol
        )

        // create a marker symbol for tap locations

        // create a marker symbol for tap locations
        val tapSymbol =
            SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, android.R.color.transparent, 14f)

        // create a graphics overlay to display geodesic polygons, set its renderer and add it to the map view.

        // create a graphics overlay to display geodesic polygons, set its renderer and add it to the map view.
        geodesicGraphicsOverlay.renderer = SimpleRenderer(geodesicBufferFillSymbol)
        geodesicGraphicsOverlay.opacity = 0.5f
        binding.mapView.graphicsOverlays.add(geodesicGraphicsOverlay)

        // create a graphics overlay to display planar polygons, set its renderer and add it to the map view.

        // create a graphics overlay to display planar polygons, set its renderer and add it to the map view.
        planarGraphicsOverlay.renderer = SimpleRenderer(planarBufferFillSymbol)
        planarGraphicsOverlay.opacity = 0.5f
        binding.mapView.graphicsOverlays.add(planarGraphicsOverlay)

        // create a graphics overlay to display tap locations for buffers, set its renderer and add it to the map view.

        // create a graphics overlay to display tap locations for buffers, set its renderer and add it to the map view.
        tapLocationsOverlay.renderer = SimpleRenderer(tapSymbol)
        binding.mapView.graphicsOverlays.add(tapLocationsOverlay)
        binding.mapView.graphicsOverlays.add(graphicsBoatOverlayLocation)
        initInnerCircleOnMap()
    }

    private fun initInnerCircleOnMap() {
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
        val planarOutlineSymbol =
            SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, android.R.color.transparent, 2f)
        val planarBufferFillSymbol = SimpleFillSymbol(
            SimpleFillSymbol.Style.SOLID, android.R.color.transparent,
            planarOutlineSymbol
        )

        // create a marker symbol for tap locations

        // create a marker symbol for tap locations
        val tapSymbol =
            SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, android.R.color.transparent, 14f)

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

    private fun drawCircleOnMap(boatRange: String) {
        if (mapPoint == null) {
            return
        }
        planarGraphicsOverlay.graphics.clear()
        geodesicGraphicsOverlay.graphics.clear()
        tapLocationsOverlay.graphics.clear()
        planarGraphicsOverlayInner.graphics.clear()
        geodesicGraphicsOverlayInner.graphics.clear()
        tapLocationsOverlayInner.graphics.clear()
        // val mapPoint: Point = mMapView.screenToLocation(screenPoint)
        // only draw a buffer if a value was entered
        if (boatRange.trim().isNotEmpty()) {
            // get the buffer distance (miles) entered in the text box.
            val bufferInMiles: Double =
                java.lang.Double.valueOf(boatRange)

            // convert the input distance to meters, 1609.34 meters in one mile
            val bufferInMeters = (bufferInMiles.nauticalMilesToMiles()) * 1609.34
            // distance in meters
            //val bufferInMeters = bufferInMiles
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
            drawInnerCircleOnMap(boatRange)
        } else {
            Toast.makeText(
                requireContext(),
                "Please enter a buffer distance first.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun drawInnerCircleOnMap(boatRange: String) {
        if (mapPoint == null) {
            return
        }
        planarGraphicsOverlayInner.graphics.clear()
        geodesicGraphicsOverlayInner.graphics.clear()
        tapLocationsOverlayInner.graphics.clear()
        //val mapPoint: Point = mMapView.screenToLocation(screenPoint)
        // only draw a buffer if a value was entered
        if (boatRange.trim().isNotEmpty()) {
            // get the buffer distance (miles) entered in the text box.
            val bufferInMiles: Double =
                java.lang.Double.valueOf(boatRange)

            // convert the input distance to meters, 1609.34 meters in one mile
            val bufferInMeters = ((bufferInMiles.nauticalMilesToMiles()) * 1609.34) / 2.0
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
            showCalloutInner(geodesicBufferGraphic, bufferInMiles / 2)
        } else {
            Toast.makeText(
                requireContext(),
                "Please enter a buffer distance first.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showCalloutInner(graphic: Graphic, radius: Double) {
        if (radius <= 0) {
            return
        }
        val centerOfGraphic = graphic.geometry.extent.center
        val calloutLocation = graphic.computeCalloutLocation(centerOfGraphic, binding.mapView)
        val ts = TextSymbol(
            14f,
            "${radius.roundOffDecimal().toString().replace(".0", "")}NM",
            Color.parseColor("#FFFFFF"),
            TextSymbol.HorizontalAlignment.CENTER,
            TextSymbol.VerticalAlignment.BOTTOM
        )
        ts.backgroundColor = Color.parseColor("#000000")
        val currentTextGraphic = Graphic(calloutLocation, ts)
        geodesicGraphicsOverlayInner.graphics.add(currentTextGraphic)
    }

    private fun showCalloutOuter(graphic: Graphic, radius: Double) {
        if (radius <= 0) {
            return
        }
        val centerOfGraphic = graphic.geometry.extent.center
        val calloutLocation = graphic.computeCalloutLocation(centerOfGraphic, binding.mapView)
        val ts = TextSymbol(
            14f,
            "${radius.roundOffDecimal().toString().replace(".0", "")}NM",
            Color.parseColor("#FFFFFF"),
            TextSymbol.HorizontalAlignment.CENTER,
            TextSymbol.VerticalAlignment.BOTTOM
        )
        ts.backgroundColor = Color.parseColor("#00386B")
        val currentTextGraphic = Graphic(calloutLocation, ts)
        geodesicGraphicsOverlay.graphics.add(currentTextGraphic)
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLocationEvent(location: Location) {
        trollingLocation = location
        //trollingViewModel.saveTrollingPoint(location)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSyncEvent(workerRequestId: UUID) {
        syncSharedViewModel.updateSync(true)
        Log.i("workRequestId", WorkerStarter.workRequestId.toString())
        WorkerStarter.workRequestId?.let { requestId ->
            WorkManager.getInstance(requireContext()).getWorkInfoByIdLiveData(requestId)
                .observe(viewLifecycleOwner) { workInfo ->
                    if (workInfo != null) {
                        when (workInfo.state) {
                            WorkInfo.State.ENQUEUED -> {
                                // Worker has been enqueued
                                // You can start collecting output data here if needed
                                Log.i("SaveFragmentListener", "WorkInfo.State.ENQUEUED ")
                            }

                            WorkInfo.State.RUNNING -> {
                                // Worker is running
                                // You can update UI or show progress if needed
                                Log.i("SaveFragmentListener", "WorkInfo.State.RUNNING ")
                            }

                            WorkInfo.State.SUCCEEDED -> {
                                // Worker has completed successfully
                                // Retrieve output data
                                // val outputData = workInfo.outputData
                                // val result = outputData.getString("result")
                                // Process the result as needed
                                lifecycleScope.launch {
                                    delay(6000)
                                    Log.i("SaveFragmentListener", "WorkInfo.State.SUCCEEDED ")
                                    syncSharedViewModel.resetResponse()
                                }
                            }

                            WorkInfo.State.FAILED -> {
                                // Worker has failed
                                // You can handle the failure scenario here
                                Log.i("SaveFragmentListener", "WorkInfo.State.FAILED ")
                                syncSharedViewModel.resetResponse()
                            }

                            WorkInfo.State.CANCELLED -> {
                                // Worker has been cancelled
                                // You can handle the cancellation scenario here
                                Log.i("SaveFragmentListener", "WorkInfo.State.CANCELLED ")
                                syncSharedViewModel.resetResponse()
                            }
                            // Handle other states as needed
                            else -> {
                                Log.i("SaveFragmentListener", "WorkInfo.State.Else ")
                            }
                        }
                    }
                }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    companion object {
        var sharedLayerViewModel: LayersViewModel? = null
    }
}