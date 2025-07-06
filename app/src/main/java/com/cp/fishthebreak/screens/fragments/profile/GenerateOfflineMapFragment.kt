package com.cp.fishthebreak.screens.fragments.profile

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.FragmentGenerateOfflineMapBinding
import com.cp.fishthebreak.databinding.GenerateOfflineMapDialogLayoutBinding
import com.cp.fishthebreak.screens.activities.NavGraphActivity
import com.cp.fishthebreak.screens.bottom_sheets.CreateMapBottomSheet
import com.cp.fishthebreak.utils.Constants
import com.cp.fishthebreak.utils.PermissionListener
import com.cp.fishthebreak.utils.checkLocationPermission
import com.cp.fishthebreak.utils.getMapDirectory
import com.cp.fishthebreak.utils.getMapPathToLoadOffline
import com.cp.fishthebreak.utils.getMapPathToLoadOfflineDb
import com.cp.fishthebreak.utils.showSnack
import com.cp.fishthebreak.utils.showToast
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.viewModels.map.OfflineMapViewModel
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.concurrent.Job
import com.esri.arcgisruntime.data.Geodatabase
import com.esri.arcgisruntime.data.GeodatabaseFeatureTable
import com.esri.arcgisruntime.data.VectorTileCache
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.ItemResourceCache
import com.esri.arcgisruntime.mapping.MobileMapPackage
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
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


@AndroidEntryPoint
class GenerateOfflineMapFragment : Fragment() {
    private val TAG = GenerateOfflineMapFragment::class.java.simpleName
    private lateinit var binding: FragmentGenerateOfflineMapBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var tempDirectoryPath: String? = null
    private val navArgs: GenerateOfflineMapFragmentArgs by navArgs()
    private fun createMapPath(){
        val date = Date().time
        viewModel.onDateChangeEvent(date)
        tempDirectoryPath = requireContext().getMapDirectory(date)?.path
        Log.i("mapPath", tempDirectoryPath?:"")
    }

    private val graphicsOverlay: GraphicsOverlay by lazy { GraphicsOverlay() }

    private val downloadArea: Graphic by lazy { Graphic() }

    val viewModel: OfflineMapViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentGenerateOfflineMapBinding.inflate(layoutInflater,container,false)
        if(requireActivity() is NavGraphActivity){
            (requireActivity() as NavGraphActivity).setStatusBarBackgroundTransparent()
        }
        binding.saveButton.isEnabled = false
        initViewModelResponse()
        initListeners()
        if(navArgs.mapPath.isNullOrEmpty()) {
            getLocation()
        }else{
//            loadMobileMapPackage(File(
//                "${navArgs.mapPath?:""}/p13/a746o4t76jqo2ocqsizhddqsae.vtpk"
//            ).path)
            loadMobileMapPackage(File(
                requireContext().getMapPathToLoadOffline("${navArgs.mapPath?:""}/p13")
            ).path, requireContext().getMapPathToLoadOfflineDb("${navArgs.mapPath?:""}/p13"))
        }
        return binding.root
    }

    private fun loadMobileMapPackage(vectorTileCachePath: String, dbPath: List<String>) {
        if (vectorTileCachePath.isEmpty()){
            requireActivity().showToast("Map path not found",false)
//            if(requireActivity() is NavGraphActivity){
//                (requireActivity() as NavGraphActivity).onBack()
//            }
            return
        }
        binding.saveButton.viewGone()
        // Create a vector tile cache from the local data.
        val mapPackage = MobileMapPackage(navArgs.mapPath)
        mapPackage.loadAsync()
        mapPackage.addDoneLoadingListener {
            binding.mapView.map = mapPackage.maps.first()
        }
        //return
        /*val cache = VectorTileCache(vectorTileCachePath)
        // Use the tile cache to create an ArcGISVectorTiledLayer.
        val tiledLayer = ArcGISVectorTiledLayer(cache)
        // Display the vector tiled layer as a basemap.
        binding.mapView.map = ArcGISMap(Basemap(tiledLayer)).apply {
            addDoneLoadingListener {
                if (loadStatus == LoadStatus.LOADED) {
                    val locationDisplay = binding.mapView.locationDisplay
                    locationDisplay.autoPanMode = LocationDisplay.AutoPanMode.NAVIGATION
                    locationDisplay.startAsync()
                    dbPath.forEach { path->
                        val localGdb = Geodatabase(path)
                        lifecycleScope.launch {
                            localGdb.loadAsync()
                            localGdb.addDoneLoadingListener {
                                if (localGdb.loadStatus == LoadStatus.LOADED) {
                                    val trailheadsTable: GeodatabaseFeatureTable =
                                        localGdb.geodatabaseFeatureTables.first()
                                    val trailheadsLayer = FeatureLayer(trailheadsTable)

                                    //val viewpoint = Viewpoint(34.0772, -118.7989, 600000.0)

                                    binding.mapView.apply {
                                        // Clears the existing layer on the map.
                                        //map?.operationalLayers?.clear()
                                        // Adds the new layer to the map
                                        map?.operationalLayers?.add(trailheadsLayer)
                                        // Updates the viewpoint to the given viewpoint.
                                        //setViewpoint(viewpoint)
                                    }
                                }else{
                                    Log.e(TAG, localGdb.loadError.message?:"")
                                }
                            }
                        }
                    }
                } else {
                    val error = "Map failed to load: " + loadError.message
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                    Log.e(TAG, error)
                    Log.e(TAG, loadError.cause?.message.toString())
                }
            }
        }*/

        // create the mobile map package
//        val mapPackage = MobileMapPackage(mmpkFile).also {
//            // load the mobile map package asynchronously
//            it.loadAsync()
//        }
//
//        // add done listener which will invoke when mobile map package has loaded
//        mapPackage.addDoneLoadingListener {
//            // check load status and that the mobile map package has maps
//            if (mapPackage.loadStatus === LoadStatus.LOADED && mapPackage.maps.isNotEmpty()) {
//                // add the map from the mobile map package to the MapView
//                binding.mapView.map = mapPackage.maps[0]
//            } else {
//                // log an issue if the mobile map package fails to load
//                requireActivity().showToast(mapPackage.loadError.message?:resources.getString(R.string.something_went_wrong), false)
//            }
//        }
    }

    private fun initListeners(){
        binding.backButton.setOnClickListener {
            if(requireActivity() is NavGraphActivity){
                (requireActivity() as NavGraphActivity).onBack()
            }
        }
        binding.saveButton.setOnClickListener {
            val dialog = CreateMapBottomSheet(name = viewModel.offlineMapUIStates.value.name, description = viewModel.offlineMapUIStates.value.description,object: CreateMapBottomSheet.OnClickListener{
                override fun onSave(name: String, description: String) {
                    createMapPath()
                    viewModel.onNameChangeEvent(name)
                    viewModel.onDescriptionChangeEvent(description)
                    viewModel.onSaveClickEvent(null)
                    generateOfflineMap()
                }

            })
            dialog.show(childFragmentManager,"CreateMapBottomSheet")
        }
    }

    private fun initMap(){
        ArcGISRuntimeEnvironment.setApiKey(resources.getString(R.string.api_key))
        ArcGISRuntimeEnvironment.setLicense("runtimelite,1000,rud8211878644,none,ZZ0RJAY3FLED0YRJD186")

        lifecycleScope.launch {
            val portal = Portal(resources.getString(R.string.portal_url), false)
            val portalItem = PortalItem(portal, resources.getString(R.string.map_portal_id))
            binding.mapView.apply {
                // create a map with the portal item
                map = ArcGISMap(portalItem).apply {
                    addDoneLoadingListener {
                        if (loadStatus == LoadStatus.LOADED) {
                            val locationDisplay = binding.mapView.locationDisplay
                            locationDisplay.autoPanMode = LocationDisplay.AutoPanMode.NAVIGATION
                            locationDisplay.startAsync()
                        } else {
                            val error = "Map failed to load: " + loadError.message
                            Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                            Log.e(TAG, error)
                            Log.e(TAG, loadError.cause?.message.toString())
                        }
                    }
                }
                binding.mapView.setViewpoint(Viewpoint(latitude?:34.0270, longitude?:-118.8050, Constants.MAP_SCALE_Zoom))
            }
            // create a symbol to show a box around the extent we want to download
            downloadArea.symbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2F)
            // add the graphic to the graphics overlay when it is created
            graphicsOverlay.graphics.add(downloadArea)
            binding.mapView.apply {
                graphicsOverlays.add(graphicsOverlay)
                // update the download area box whenever the viewpoint changes
                addViewpointChangedListener {
                    if (map.loadStatus == LoadStatus.LOADED) {
                        // upper left corner of the area to take offline
                        val minScreenPoint = Point(200, 200)
                        // lower right corner of the downloaded area
                        val maxScreenPoint = Point(
                            binding.mapView.width - 200,
                            binding.mapView.height - 200
                        )
                        // convert screen points to map points
                        val minPoint = binding.mapView.screenToLocation(minScreenPoint)
                        val maxPoint = binding.mapView.screenToLocation(maxScreenPoint)
                        // use the points to define and return an envelope
                        if (minPoint != null && maxPoint != null) {
                            val envelope = Envelope(minPoint, maxPoint)
                            downloadArea.geometry = envelope
                            // enable the take map offline button only after the map is loaded
                            if (!binding.saveButton.isEnabled) binding.saveButton.isEnabled = true
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        requireActivity().checkLocationPermission(childFragmentManager,object: PermissionListener {
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
                            latitude = location.latitude
                            longitude = location.longitude
                            initMap()
                        }

                    }
            }

            override fun onPermissionCancel() {

            }

        })
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
        //File(tempDirectoryPath).deleteRecursively()

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
            isContinueOnErrors = false
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
                    binding.saveButton.isEnabled = false
                    binding.saveButton.visibility = View.GONE
                    val mapImage = binding.mapView.exportImageAsync()
                    mapImage.addDoneListener {
                        val image = mapImage.get()
                        val imgPath = (tempDirectoryPath?:"")+"/img_${viewModel.offlineMapUIStates.value.mapDate}.png"
                        val imageFile = File(imgPath)
                        try {
                            val fos: FileOutputStream = FileOutputStream(imageFile)
                            image.compress(Bitmap.CompressFormat.PNG, 90, fos)
                            fos.close()
                            viewModel.onImageChangeEvent(imgPath)
                        } catch (e: FileNotFoundException) {
                            Log.d(TAG, "File not found: " + e.message)
                        } catch (e: IOException) {
                            Log.d(TAG, "Error accessing file: " + e.message)
                        }
                        binding.mapView.map = result.offlineMap
                        graphicsOverlay.graphics.clear()
                        viewModel.onPathChangeEvent(tempDirectoryPath?:"")
                        viewModel.updateMap()
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
                    viewModel.deleteInCompleteMap()
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
                                requireActivity().showToast(offlineMapJob.error.additionalMessage,false)
                                val error =
                                    "Error in generate offline map job: " + offlineMapJob.error.message
                                val error1 =
                                    "Error in generate offline map job: " + offlineMapJob.error.cause?.message
                                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                                Log.e(TAG, error)
                                Log.e(TAG, error1)
                            }
                        }else{
                            requireActivity().showToast(offlineMapJob.error.additionalMessage,false)
                            val error =
                                "Error in generate offline map job: " + offlineMapJob.error.message
                            val error1 =
                                "Error in generate offline map job: " + offlineMapJob.error.cause?.message
                            Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                            Log.e(TAG, error)
                            Log.e(TAG, error1)
                        }
                    }else{
                        requireActivity().showToast(offlineMapJob.error.additionalMessage,false)
                        val error =
                            "Error in generate offline map job: " + offlineMapJob.error.message
                        val error1 =
                            "Error in generate offline map job: " + offlineMapJob.error.cause?.message
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

    /**
     * Create a progress dialog box for tracking the generate offline map job.
     *
     * @param job the generate offline map job progress to be tracked
     * @return an AlertDialog set with the dialog layout view
     */
    private fun createProgressDialog(job: GenerateOfflineMapJob): AlertDialog {
        val builder = AlertDialog.Builder(requireContext()).apply {
            setTitle("Generating offline map...")
            // provide a cancel button on the dialog
            setNegativeButton("Cancel") { _, _ ->
                job.cancelAsync()
            }
            setCancelable(true)
            val dialogLayoutBinding = GenerateOfflineMapDialogLayoutBinding.inflate(layoutInflater)
            setView(dialogLayoutBinding.root)
        }
        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.resume()
    }

    override fun onPause() {
        binding.mapView.pause()
        // delete the temporary cache when the app loses focus
        //File(tempDirectoryPath).deleteRecursively()
        super.onPause()
    }

    override fun onDestroy() {
        binding.mapView.dispose()
        super.onDestroy()
    }

    private fun initViewModelResponse() {
        lifecycleScope.launch {
            viewModel.offlineMapResponse
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collect { response ->
                    if (response != null){
                        //viewModel.resetResponse()
                    }
                }
        }

    }

//    private fun showHideLoader(visibility: Boolean) {
//        if (visibility) {
//            binding.loaderLayout.viewVisible()
//        } else {
//            binding.loaderLayout.viewGone()
//        }
//    }
}