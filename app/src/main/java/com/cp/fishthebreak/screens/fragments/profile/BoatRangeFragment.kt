package com.cp.fishthebreak.screens.fragments.profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.FragmentBoatRangeBinding
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Viewpoint
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.view.MotionEvent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cp.fishthebreak.di.NetworkResult
import com.cp.fishthebreak.utils.Constants.Companion.MAP_SCALE
import com.cp.fishthebreak.utils.PermissionListener
import com.cp.fishthebreak.utils.checkLocationPermission
import com.cp.fishthebreak.utils.getOnErrorMessage
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible
import com.cp.fishthebreak.viewModels.profile.preference.PreferenceViewModel
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.geometry.GeodeticCurveType
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.LinearUnit
import com.esri.arcgisruntime.geometry.LinearUnitId
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.concurrent.ExecutionException
import kotlin.math.roundToInt

@AndroidEntryPoint
class BoatRangeFragment : Fragment() {
    private val TAG: String = BoatRangeFragment::class.java.simpleName
    private lateinit var binding: FragmentBoatRangeBinding
    private val mMapView: MapView by lazy {
        binding.mapView
    }
    val geodesicGraphicsOverlay = GraphicsOverlay()
    val planarGraphicsOverlay = GraphicsOverlay()
    val tapLocationsOverlay = GraphicsOverlay()
    val geodesicGraphicsOverlayInner = GraphicsOverlay()
    val planarGraphicsOverlayInner = GraphicsOverlay()
    val tapLocationsOverlayInner = GraphicsOverlay()
    var screenPoint: android.graphics.Point? = null
    var mapPoint: Point? = null
    val viewModel: PreferenceViewModel by viewModels()
    private val graphicsOverlayLocation: GraphicsOverlay by lazy { GraphicsOverlay() }
    // create a picture marker symbol
    private val pinSourceSymbol: PictureMarkerSymbol? by lazy { createPinSymbol() }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double? = null
    private var longitude: Double? = null
    private val navArgs: BoatRangeFragmentArgs by navArgs()
    private var identifiedGraphic: Graphic? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentBoatRangeBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        binding.model = viewModel
        initViews()
        initViewModelResponse()
        initListeners()
        getLocation()
        return binding.root
    }
    private fun createPinSymbol(): PictureMarkerSymbol? {
        val pinDrawable =
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_pin_new) as BitmapDrawable?
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
    private fun initViews(){
        if(navArgs.showBoatRangeControls){
            binding.bottomLayout.viewVisible()
            if(navArgs.saveBoatRangeToServer) {
                binding.btnLayerOnOff.viewVisible()
            }else{
                binding.btnLayerOnOff.viewGone()
            }
            binding.saveLocationButton.viewGone()
            binding.tvTitle.text = resources.getString(R.string.boat_range)
        }else{
            binding.bottomLayout.viewGone()
            binding.btnLayerOnOff.viewGone()
            binding.saveLocationButton.viewVisible()
            binding.tvTitle.text = resources.getString(R.string.dock_location)
            binding.saveLocationButton.setOnClickListener {
                if(latitude != null && longitude != null) {
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        "dockLocation",
                        "$latitude , $longitude"
                    )
                    findNavController().popBackStack()
                }else{
                    requireActivity().showToast(resources.getString(R.string.error_vessel_location),false)
                }
            }
        }
    }

    private fun initListeners(){
        binding.saveButton.setOnClickListener {
            if(latitude != null && longitude != null) {
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    "boatRange",
                    viewModel.preferenceUIStates.value.range.toString()
                )
                findNavController().popBackStack()
            }else{
                viewModel.onSaveClickedEvent(binding.saveButton)
            }
        }
        binding.backButton.setOnClickListener {
            if(requireActivity() is NavGraphActivity){
                (requireActivity() as NavGraphActivity).onBack()
            }
        }
        binding.ivCurrentLocation.setOnSingleClickListener {
            getLocation()
        }
        binding.etRange.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(!p0.isNullOrEmpty()) {
                    //binding.seekBar.progress = p0.toString().toInt()
                    if(mapPoint != null) {
                        drawCircleOnMap()
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
//        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
//            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
//                if(p2) {
//                    binding.etRange.setText(p1.toString())
//                }
//                drawCircleOnMap()
//            }
//
//            override fun onStartTrackingTouch(p0: SeekBar?) {
//
//            }
//
//            override fun onStopTrackingTouch(p0: SeekBar?) {
//
//            }
//
//        })

        object : DefaultMapViewOnTouchListener(requireContext(), mMapView) {
            @SuppressLint("ClickableViewAccessibility")
            override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {

                // get the point that was clicked and convert it to a point in the map
                screenPoint = android.graphics.Point(
                    Math.round(motionEvent.x),
                    Math.round(motionEvent.y)
                )
                mapPoint = mMapView.screenToLocation(screenPoint)
                drawCircleOnMap()
                return true
            }
        }.also { mMapView.onTouchListener = it }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initMapTouchListener(){
        binding.mapView.addViewpointChangedListener { p0 ->
            // clear the current graphic position
            graphicsOverlayLocation.graphics.clear()
            // create a screen point from where the user tapped
            val viewPoint = p0?.source?.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE)
            val screenPoint = Point(
                viewPoint?.targetGeometry?.extent?.center?.x ?: 0.0,
                viewPoint?.targetGeometry?.extent?.center?.y ?: 0.0,
                SpatialReference.create(3857)
            )
            val projectedPoint = GeometryEngine.project(screenPoint,SpatialReference.create(4326))
            // create a map point from the screen point
            val locationPoint = projectedPoint as Point
            longitude = locationPoint.x
            latitude = locationPoint.y
            val graphic = Graphic(projectedPoint, pinSourceSymbol)
            graphicsOverlayLocation.graphics?.add(graphic)
        }
        /*binding.mapView.onTouchListener =
            object : DefaultMapViewOnTouchListener(requireContext(),  binding.mapView) {
                // drag a graphic to a new point on map
                override fun onDoubleTouchDrag(e: MotionEvent): Boolean {
                    if(!navArgs.showBoatRangeControls) {
                        // identify the pixel at the given screen point
                        val screenPoint = android.graphics.Point(e.x.roundToInt(), e.y.roundToInt())
                        // create a map point from the screen point
                        val mapPoint: Point = binding.mapView.screenToLocation(screenPoint)

//                        val toLatitudeLongitude = CoordinateFormatter.toLatitudeLongitude(mapPoint, CoordinateFormatter.LatitudeLongitudeFormat.DECIMAL_DEGREES, 4)
//                        val point = CoordinateFormatter.fromLatitudeLongitude(toLatitudeLongitude, binding.mapView.spatialReference)
//                        val latitude = point.x
//                        val longitude = point.y
//                        Log.e("mapPoint.x", longitude.toString())
                        val sp = SpatialReference.create(4326) //4326 is for Geographic coordinate systems (GCSs)
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
                                        requireActivity().showToast("No graphic at point", false)
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
                    if(!navArgs.showBoatRangeControls) {
                        // clear the current graphic position
                        graphicsOverlayLocation.graphics.clear()
                        // create a screen point from where the user tapped
                        val screenPoint = android.graphics.Point(e.x.roundToInt(), e.y.roundToInt())
                        // create a map point from the screen point
                        val mapPoint: Point = binding.mapView.screenToLocation(screenPoint)
                        // create graphic with the location and symbol and add it to the graphics overlay
                        val sp = SpatialReference.create(4326) //4326 is for Geographic coordinate systems (GCSs)
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

    private fun initMap(){
        ArcGISRuntimeEnvironment.setApiKey(resources.getString(R.string.api_key))

        lifecycleScope.launch {
            binding.mapView.apply {
                binding.mapView.map = ArcGISMap(BasemapStyle.ARCGIS_OCEANS)
                binding.mapView.setViewpoint(Viewpoint(latitude?:34.0270, longitude?:-118.8050, MAP_SCALE))
                graphicsOverlays.add(graphicsOverlayLocation)//for location pin point
                val locationDisplay = binding.mapView.locationDisplay
                locationDisplay.autoPanMode = LocationDisplay.AutoPanMode.NAVIGATION
                locationDisplay.startAsync()
                binding.mapView.setViewpoint(
                    Viewpoint(
                        latitude ?: 34.0270,
                        longitude ?: -118.8050,
                        MAP_SCALE
//                        72000.0
                    )
                )
                binding.mapView.map.addDoneLoadingListener {
                    if (binding.mapView.map.loadStatus == LoadStatus.LOADED) {
                        mapPoint = Point(longitude?:0.0,latitude?:0.0, SpatialReference.create(4326))
                        if(navArgs.showBoatRangeControls){
                            initCircleOnMap()
                            drawCircleOnMap()
                        }else{
                            addMarkerOnMap()
                            initMapTouchListener()
                        }
                    } else if (binding.mapView.map.loadStatus != LoadStatus.LOADED) {
                        val error = "Failed to load portal item ${binding.mapView.map.loadError.message}"
                        val error1 =
                            "Failed to load portal item ${binding.mapView.map.loadError.cause?.message}"
                        Log.e("addDoneLoadingListener", error)
                        Log.e("addDoneLoadingListener1", error1)
                        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                        return@addDoneLoadingListener
                    }
                }
            }
        }
    }
    private fun addMarkerOnMap(){
        // clear the current graphic position
        graphicsOverlayLocation.graphics.clear()
        // create a map point from lat lang
        val mapPoint = Point(longitude?:0.0,latitude?:0.0, SpatialReference.create(4326))
        val graphic = Graphic(mapPoint, pinSourceSymbol)
        graphicsOverlayLocation.graphics?.add(graphic)
        binding.mapView.setViewpointCenterAsync(mapPoint)
//        binding.mapView.setViewpoint(
//            Viewpoint(
//                latitude ?: 34.0270,
//                longitude ?: -118.8050,
//                MAP_SCALE
////                        72000.0
//            )
//        )
    }
    private fun drawCircleOnMap(){
        if (mapPoint == null){
            return
        }
        planarGraphicsOverlay.graphics.clear()
        geodesicGraphicsOverlay.graphics.clear()
        tapLocationsOverlay.graphics.clear()
       // val mapPoint: Point = mMapView.screenToLocation(screenPoint)
        // only draw a buffer if a value was entered
        if (binding.etRange.text.toString().trim().isNotEmpty()) {
            // get the buffer distance (miles) entered in the text box.
            val bufferInMiles: Double =
                java.lang.Double.valueOf(binding.etRange.text.toString())

            // convert the input distance to meters, 1609.34 meters in one mile
            val bufferInMeters = bufferInMiles * 1609.34
            // distance in meters
            //val bufferInMeters = bufferInMiles
            viewModel.onRangeChangeEvent(bufferInMiles.toInt())
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
            drawInnerCircleOnMap()
        } else {
            Toast.makeText(
                requireContext(),
                "Please enter a buffer distance first.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    private fun initCircleOnMap(){
        // create a graphics overlay to contain the buffered geometry graphics
        // create a graphics overlay to contain the buffered geometry graphics
        val graphicsOverlay = GraphicsOverlay()
        mMapView.getGraphicsOverlays().add(graphicsOverlay)

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
        mMapView.getGraphicsOverlays().add(geodesicGraphicsOverlay)

        // create a graphics overlay to display planar polygons, set its renderer and add it to the map view.

        // create a graphics overlay to display planar polygons, set its renderer and add it to the map view.
        planarGraphicsOverlay.renderer = SimpleRenderer(planarBufferFillSymbol)
        planarGraphicsOverlay.opacity = 0.5f
        mMapView.getGraphicsOverlays().add(planarGraphicsOverlay)

        // create a graphics overlay to display tap locations for buffers, set its renderer and add it to the map view.

        // create a graphics overlay to display tap locations for buffers, set its renderer and add it to the map view.
        tapLocationsOverlay.renderer = SimpleRenderer(tapSymbol)
        mMapView.getGraphicsOverlays().add(tapLocationsOverlay)
        initInnerCircleOnMap()
    }
    private fun drawInnerCircleOnMap(){
        if (mapPoint == null){
            return
        }
        planarGraphicsOverlayInner.graphics.clear()
        geodesicGraphicsOverlayInner.graphics.clear()
        tapLocationsOverlayInner.graphics.clear()
        //val mapPoint: Point = mMapView.screenToLocation(screenPoint)
        // only draw a buffer if a value was entered
        if (binding.etRange.text.toString().trim().isNotEmpty()) {
            // get the buffer distance (miles) entered in the text box.
            val bufferInMiles: Double =
                java.lang.Double.valueOf(binding.etRange.text.toString())

            // convert the input distance to meters, 1609.34 meters in one mile
            val bufferInMeters = (bufferInMiles * 1609.34)/2.0
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
        mMapView.getGraphicsOverlays().add(graphicsOverlay)

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
        mMapView.getGraphicsOverlays().add(geodesicGraphicsOverlayInner)

        // create a graphics overlay to display planar polygons, set its renderer and add it to the map view.

        // create a graphics overlay to display planar polygons, set its renderer and add it to the map view.
        planarGraphicsOverlayInner.renderer = SimpleRenderer(planarBufferFillSymbol)
        planarGraphicsOverlayInner.opacity = 0.5f
        mMapView.getGraphicsOverlays().add(planarGraphicsOverlayInner)

        // create a graphics overlay to display tap locations for buffers, set its renderer and add it to the map view.

        // create a graphics overlay to display tap locations for buffers, set its renderer and add it to the map view.
        tapLocationsOverlayInner.renderer = SimpleRenderer(tapSymbol)
        mMapView.getGraphicsOverlays().add(tapLocationsOverlayInner)

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
        super.onDestroy()
    }

    private fun initViewModelResponse() {
        lifecycleScope.launch {
            viewModel.profileResponse
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
            viewModel.validationError
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    if (response.isNotEmpty()){
                        requireActivity().showToast(response, false)
                        viewModel.resetErrorResponse()
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

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        requireActivity().checkLocationPermission(childFragmentManager,object: PermissionListener{
            override fun onPermissionGranted() {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    object : CancellationToken() {
                        override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                            CancellationTokenSource().token

                        override fun isCancellationRequested() = false
                    })
                    .addOnSuccessListener { location: Location? ->
                        if (location == null)
                            Toast.makeText(requireContext(), "Cannot get location.", Toast.LENGTH_SHORT)
                                .show()
                        else {
                            if(latitude == null) {
                                latitude = location.latitude
                                longitude = location.longitude
                                initMap()
                            }else{
                                if (binding.mapView.locationDisplay != null) {
                                    if (binding.mapView.locationDisplay.location != null) {
                                        if (binding.mapView.locationDisplay.location.position != null) {
                                            val point =
                                                binding.mapView.locationDisplay.location.position
                                            // Zoom to current location with magnification 1000.
                                            binding.mapView.setViewpointCenterAsync(
                                                point,
                                                MAP_SCALE
                                            )
                                            Log.d("Latitude", "${point.x}")
                                            Log.d("Longitude", "${point.y}")
                                        }
                                    }
                                }
//                                binding.mapView.setViewpoint(
//                                    Viewpoint(
//                                        latitude ?: 34.0270,
//                                        longitude ?: -118.8050,
//                                        MAP_SCALE
////                        72000.0
//                                    )
//                                )
                            }
                        }

                    }
            }

            override fun onPermissionCancel() {

            }

        })
    }

}