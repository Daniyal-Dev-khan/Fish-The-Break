package com.cp.fishthebreak.utils

import android.Manifest
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.database.Cursor
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.cp.fishthebreak.R
import com.cp.fishthebreak.screens.activities.BaseActivityResult
import com.cp.fishthebreak.screens.bottom_sheets.ImagePickerBottomSheet
import com.cp.fishthebreak.screens.bottom_sheets.PermissionBottomSheet
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date


fun Activity.selectImage(
    fragmentManager: FragmentManager,
    launcher: BaseActivityResult<Intent, ActivityResult>,
    listener: SelectImageListener
) {
    val context = this
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        mutableListOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        mutableListOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.CAMERA
        )
    } else {
        mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
    Dexter.withContext(context)
        .withPermissions(
            permissions
        )
        .withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                p0?.let {
                    if (
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                        ContextCompat.checkSelfPermission(
                            context,
                            READ_MEDIA_VISUAL_USER_SELECTED
                        ) == PERMISSION_GRANTED
                    ) {
                        this@selectImage.openCamera(fragmentManager, launcher, listener)
                    } else if (p0.areAllPermissionsGranted()) {
                        this@selectImage.openCamera(fragmentManager, launcher, listener)
                    } else {
                        listener.onImageCancel()
                        val permissionDialogueDialogFragment =
                            PermissionBottomSheet(resources.getString(R.string.request_for_camera),
                                object : PermissionBottomSheet.OnItemClickListeners {
                                    override fun onSettingsClick() {
                                        startActivity(Intent().apply {
                                            action =
                                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                            data = Uri.fromParts("package", packageName, null)
                                        })
                                    }

                                    override fun onCancelClick() {

                                    }

                                })
                        permissionDialogueDialogFragment.show(fragmentManager, "")
                    }
                }

            }

            override fun onPermissionRationaleShouldBeShown(
                p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                p1: PermissionToken?
            ) {
                p1?.continuePermissionRequest()
            }

        })
        .check()

}

fun Activity.selectFromImagePicker(
    fragmentManager: FragmentManager,
    launcher: BaseActivityResult<Intent, ActivityResult>,
    listener: SelectImageListener,
    imagePicker: ActivityResultLauncher<PickVisualMediaRequest>?
) {
    val context = this
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        mutableListOf(
//            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.CAMERA,
            //Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        mutableListOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.CAMERA
        )
    } else {
        mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
    Dexter.withContext(context)
        .withPermissions(
            permissions
        )
        .withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                p0?.let {
//                    if (
//                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
//                        ContextCompat.checkSelfPermission(context, READ_MEDIA_VISUAL_USER_SELECTED) == PERMISSION_GRANTED
//                    )
                    if (
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
                    ) {
                        this@selectFromImagePicker.openCameraForImagePicker(
                            fragmentManager,
                            launcher,
                            listener,
                            imagePicker
                        )
                    } else if (p0.areAllPermissionsGranted()) {
                        this@selectFromImagePicker.openCamera(fragmentManager, launcher, listener)
                    } else {
                        listener.onImageCancel()
                        val permissionDialogueDialogFragment =
                            PermissionBottomSheet(resources.getString(R.string.request_for_camera),
                                object : PermissionBottomSheet.OnItemClickListeners {
                                    override fun onSettingsClick() {
                                        startActivity(Intent().apply {
                                            action =
                                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                            data = Uri.fromParts("package", packageName, null)
                                        })
                                    }

                                    override fun onCancelClick() {

                                    }

                                })
                        permissionDialogueDialogFragment.show(fragmentManager, "")
                    }
                }

            }

            override fun onPermissionRationaleShouldBeShown(
                p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                p1: PermissionToken?
            ) {
                p1?.continuePermissionRequest()
            }

        })
        .check()

}

private fun Activity.openCameraForImagePicker(
    fragmentManager: FragmentManager,
    launcher: BaseActivityResult<Intent, ActivityResult>,
    listener: SelectImageListener,
    pickMultipleMedia: ActivityResultLauncher<PickVisualMediaRequest>?
) {
    val dialog =
        ImagePickerBottomSheet(object : ImagePickerBottomSheet.OnItemClickListener {
            override fun onGallerySelect() {
                pickMultipleMedia?.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

            override fun onCameraSelect() {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (cameraIntent.resolveActivity(packageManager) != null) {
                    // Create the File where the photo should go
                    var photoFile: File? = null
                    try {
//                                photoFile = createImageFile()
                        photoFile = createImageFile_()
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                        Log.i("captureImage", ex.message.toString())
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        val values = ContentValues()
                        val timeStamp: String =
                            SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                        val imageFileName = "JPEG_" + timeStamp + "_.jpg"
                        values.put(MediaStore.Images.Media.TITLE, imageFileName)
                        values.put(
                            MediaStore.Images.Media.DESCRIPTION,
                            "Image capture by camera"
                        )
                        val imageUri =
                            getContentResolver().insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                values
                            )!!
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                        launcher.launch(
                            cameraIntent,
                            object :
                                BaseActivityResult.OnActivityResult<ActivityResult> {
                                @SuppressLint("NotifyDataSetChanged")
                                override fun onActivityResult(result: ActivityResult) {
                                    if (result.resultCode == Activity.RESULT_OK) {
                                        try {
                                            listener.onImageSelect(
                                                getFilePath(
                                                    applicationContext,
                                                    imageUri
                                                )
                                            )
                                        } catch (ex: Exception) {
                                        }
                                    }
                                }
                            })
                    } else {
                        Log.e("Failed to create file", "File cration failed")
                    }
                }
            }

            override fun onCancel() {
                listener.onImageCancel()
            }

        })
    dialog.show(fragmentManager, "ImagePickerDialog")
}

/*
fun Activity.selectImage(
    fragmentManager: FragmentManager,
    launcher: BaseActivityResult<Intent, ActivityResult>,
    listener: SelectImageListener
) {
    val context = this
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Dexter.withContext(context)
            .withPermissions(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.CAMERA,
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    p0?.let {
                        if (p0.areAllPermissionsGranted()) {
                            this@selectImage.openCamera(fragmentManager, launcher, listener)
                        } else {
                            listener.onImageCancel()
                            val permissionDialogueDialogFragment =
                                PermissionBottomSheet(resources.getString(R.string.request_for_camera),
                                    object : PermissionBottomSheet.OnItemClickListeners {
                                        override fun onSettingsClick() {
                                            startActivity(Intent().apply {
                                                action =
                                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                                data = Uri.fromParts("package", packageName, null)
                                            })
                                        }

                                        override fun onCancelClick() {

                                        }

                                    })
                            permissionDialogueDialogFragment.show(fragmentManager, "")
                        }
                    }

                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }

            })
            .check()
    } else {
        Dexter.withContext(context)
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    p0?.let {
                        if (p0.areAllPermissionsGranted()) {
                            this@selectImage.openCamera(fragmentManager, launcher, listener)
                        } else {
                            listener.onImageCancel()
                            val permissionDialogueDialogFragment =
                                PermissionBottomSheet(resources.getString(R.string.request_for_camera),
                                    object : PermissionBottomSheet.OnItemClickListeners {
                                        override fun onSettingsClick() {
                                            startActivity(Intent().apply {
                                                action =
                                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                                data = Uri.fromParts("package", packageName, null)
                                            })
                                        }

                                        override fun onCancelClick() {

                                        }
                                    })
                            permissionDialogueDialogFragment.show(fragmentManager, "")
                        }
                    }

                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }

            })
            .check()
    }

}
 */

private fun Activity.openCamera(
    fragmentManager: FragmentManager,
    launcher: BaseActivityResult<Intent, ActivityResult>,
    listener: SelectImageListener
) {
    val dialog =
        ImagePickerBottomSheet(object : ImagePickerBottomSheet.OnItemClickListener {
            override fun onGallerySelect() {
                val pickIntent =
                    Intent(
                        Intent.ACTION_PICK
                    )
//                    Intent(
//                        Intent.ACTION_PICK,
//                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//                    )
                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
                //pickIntent.type = "image/*"
                launcher.launch(
                    pickIntent,
                    object :
                        BaseActivityResult.OnActivityResult<ActivityResult> {
                        @SuppressLint("NotifyDataSetChanged")
                        override fun onActivityResult(result: ActivityResult) {
                            if (result.resultCode == Activity.RESULT_OK) {
                                if (result.data != null) {
                                    try {
                                        val selectedImage: Uri =
                                            result.data?.data ?: return
                                        if (selectedImage != null && "content" == selectedImage.scheme) {
                                            val cursor: Cursor? =
                                                contentResolver.query(
                                                    selectedImage,
                                                    arrayOf(MediaStore.Images.ImageColumns.DATA),
                                                    null,
                                                    null,
                                                    null
                                                )
                                            cursor?.moveToFirst()
                                            listener.onImageSelect(
                                                cursor?.getString(
                                                    0
                                                ) ?: ""
                                            )
                                            cursor?.close()
                                        } else {
                                            listener.onImageSelect(
                                                selectedImage?.path ?: ""
                                            )
                                        }
                                    } catch (ex: Exception) {
                                        listener.onImageCancel()
                                    }
                                }
                            }
                        }
                    })
            }

            override fun onCameraSelect() {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (cameraIntent.resolveActivity(packageManager) != null) {
                    // Create the File where the photo should go
                    var photoFile: File? = null
                    try {
//                                photoFile = createImageFile()
                        photoFile = createImageFile_()
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                        Log.i("captureImage", ex.message.toString())
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        val values = ContentValues()
                        val timeStamp: String =
                            SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                        val imageFileName = "JPEG_" + timeStamp + "_.jpg"
                        values.put(MediaStore.Images.Media.TITLE, imageFileName)
                        values.put(
                            MediaStore.Images.Media.DESCRIPTION,
                            "Image capture by camera"
                        )
                        val imageUri =
                            getContentResolver().insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                values
                            )!!
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                        launcher.launch(
                            cameraIntent,
                            object :
                                BaseActivityResult.OnActivityResult<ActivityResult> {
                                @SuppressLint("NotifyDataSetChanged")
                                override fun onActivityResult(result: ActivityResult) {
                                    if (result.resultCode == Activity.RESULT_OK) {
                                        try {
                                            listener.onImageSelect(
                                                getFilePath(
                                                    applicationContext,
                                                    imageUri
                                                )
                                            )
                                        } catch (ex: Exception) {
                                        }
                                    }
                                }
                            })
                    } else {
                        Log.e("Failed to create file", "File cration failed")
                    }
                }
            }

            override fun onCancel() {
                listener.onImageCancel()
            }

        })
    dialog.show(fragmentManager, "ImagePickerDialog")
}

fun Activity.createImageFile_(): File? {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val sd_main = File(Environment.getExternalStorageDirectory(), "/Pictures")
    var success = true
    if (!sd_main.exists())
        success = sd_main.mkdir()

    if (success) {
        val sd = File(imageFileName)

        if (!sd.exists())
            success = sd.mkdir()

        if (success) {
            // directory exists or already created
            val dest = File(sd, imageFileName)

        }
        return sd
    } else {
        // directory creation is not successful
        return null
    }
}

fun getFilePath(context: Context, contentUri: Uri): String? {
    try {
        val filePathColumn = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.TITLE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
        )

        val returnCursor =
            contentUri.let { context.contentResolver.query(it, filePathColumn, null, null, null) }

        if (returnCursor != null) {

            returnCursor.moveToFirst()
            val nameIndex = returnCursor.getColumnIndexOrThrow(
                OpenableColumns.DISPLAY_NAME
            )
            val name = returnCursor.getString(nameIndex)
            val file = File(context.cacheDir, name)
            val inputStream = context.contentResolver.openInputStream(contentUri)
            val outputStream = FileOutputStream(file)
            var read: Int
            val maxBufferSize = 1 * 1024 * 1024
            val bytesAvailable = inputStream!!.available()

            val bufferSize = Integer.min(
                bytesAvailable,
                maxBufferSize
            )
            val buffers = ByteArray(bufferSize)

            while (inputStream.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }

            inputStream.close()
            outputStream.close()
            return file.absolutePath
        } else {
            Log.d("", "returnCursor is null")
            return null
        }
    } catch (e: Exception) {
        Log.d("", "exception caught at getFilePath(): $e")
        return null
    }
}

fun isLocationEnabled(context: Context): Boolean? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        // This is a new method provided in API 28
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lm.isLocationEnabled
    } else {
        // This was deprecated in API 28
        val mode = Settings.Secure.getInt(
            context.contentResolver, Settings.Secure.LOCATION_MODE,
            Settings.Secure.LOCATION_MODE_OFF
        )
        mode != Settings.Secure.LOCATION_MODE_OFF
    }
}

fun isGPSEnabled(mContext: Context): Boolean {
    val manager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        manager.isLocationEnabled
    } else {
        manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
    //return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

fun checkGPSEnabled(mContext: Context, mActivity: Activity, listener: PermissionListener) {
    val manager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER).not()) {
        turnOnGPS(mActivity, listener)
    }
}

private fun turnOnGPS(mActivity: Activity, listener: PermissionListener) {
//    val request = LocationRequest.create().apply {
//        interval = 2000
//        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//    }
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
        .setWaitForAccurateLocation(false)
        .setMinUpdateIntervalMillis(2000)
        .setMaxUpdateDelayMillis(2000)
        .build()
    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
    val client: SettingsClient = LocationServices.getSettingsClient(mActivity)
    val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
    task.addOnFailureListener {
        if (it is ResolvableApiException) {
            try {
                it.startResolutionForResult(mActivity, 12345)
            } catch (sendEx: IntentSender.SendIntentException) {
            }
        }
        listener.onPermissionCancel()
    }.addOnSuccessListener {
        //here GPS is On
        listener.onPermissionGranted()
    }
}

fun Activity.checkLocationPermission(
    fragmentManager: FragmentManager,
    listener: PermissionListener
) {
    val context = this
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Dexter.withContext(context)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    p0?.let {
                        if (p0.areAllPermissionsGranted() && isGPSEnabled(this@checkLocationPermission)) {
                            listener.onPermissionGranted()
                        } else {
                            val permissionDialogueDialogFragment =
                                PermissionBottomSheet(resources.getString(R.string.request_for_location),
                                    object : PermissionBottomSheet.OnItemClickListeners {
                                        override fun onSettingsClick() {
                                            //if (isLocationEnabled(this@checkLocationPermission) == true) {
                                            if (isGPSEnabled(this@checkLocationPermission)) {
                                                startActivity(Intent().apply {
                                                    action =
                                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                                    data =
                                                        Uri.fromParts("package", packageName, null)
                                                })
                                                listener.onPermissionCancel()
                                            } else {
                                                checkGPSEnabled(
                                                    this@checkLocationPermission,
                                                    this@checkLocationPermission,
                                                    listener
                                                )
                                                //startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                                            }
                                        }

                                        override fun onCancelClick() {
                                            listener.onPermissionCancel()
                                        }

                                    })
                            permissionDialogueDialogFragment.show(fragmentManager, "")
                        }
                    }

                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }

            })
            .check()
    } else {
        Dexter.withContext(context)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    p0?.let {
                        if (p0.areAllPermissionsGranted() && isGPSEnabled(this@checkLocationPermission)) {
                            listener.onPermissionGranted()
                        } else {
                            listener.onPermissionCancel()
                            val permissionDialogueDialogFragment =
                                PermissionBottomSheet(resources.getString(R.string.request_for_location),
                                    object : PermissionBottomSheet.OnItemClickListeners {
                                        override fun onSettingsClick() {
                                            if (isGPSEnabled(this@checkLocationPermission)) {
                                                startActivity(Intent().apply {
                                                    action =
                                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                                    data =
                                                        Uri.fromParts("package", packageName, null)
                                                })
                                                listener.onPermissionCancel()
                                            } else {
                                                checkGPSEnabled(
                                                    this@checkLocationPermission,
                                                    this@checkLocationPermission,
                                                    listener
                                                )
                                                //startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                                            }
//                                            startActivity(Intent().apply {
//                                                action =
//                                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                                                data = Uri.fromParts("package", packageName, null)
//                                            })
                                        }

                                        override fun onCancelClick() {
                                            listener.onPermissionCancel()
                                        }
                                    })
                            permissionDialogueDialogFragment.show(fragmentManager, "")
                        }
                    }

                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }

            })
            .check()
    }

}

fun Context.getMapDirectory(date: Long): File? {
    val sd_main = File(
        Environment.getExternalStorageDirectory(),
        "/Download/FishTheBreak/map_${date}"
    )
    var success = false
    if (!sd_main.exists()) {
        success = sd_main.mkdir()
        if (!success) {
            success = sd_main.mkdirs()
        }
    } else {
        success = true
    }
    return if (success) {
        sd_main
    } else {
        // directory creation is not successful
        null
    }
}

fun Context.createMapDirectoryPath(): File? {
    val sd_main = File(
        Environment.getExternalStorageDirectory(),
        "/Download/FishTheBreak/map_${Date().time}"
    )
    var success = false
    if (!sd_main.exists()) {
        success = sd_main.mkdir()
        if (!success) {
            success = sd_main.mkdirs()
        }
    } else {
        success = true
    }
    return if (success) {
        sd_main
    } else {
        // directory creation is not successful
        null
    }
}

fun Activity.getPathFromUris(list: List<Uri>): ArrayList<String> {
    val imagesList = ArrayList<String>()
    list.forEach { selectedImage ->
        if ("content" == selectedImage.scheme) {
            val cursor: Cursor? =
                contentResolver.query(
                    selectedImage,
                    arrayOf(MediaStore.Images.ImageColumns.DATA),
                    null,
                    null,
                    null
                )
            cursor?.moveToFirst()
            imagesList.add(
                cursor?.getString(
                    0
                ) ?: ""
            )
            cursor?.close()
        } else {
            imagesList.add(selectedImage.path ?: "")
        }
    }
    return imagesList
}

fun Activity.getPathFromUri(imageUri: Uri): String {
    if ("content" == imageUri.scheme) {
        val cursor: Cursor? =
            contentResolver.query(
                imageUri,
                arrayOf(MediaStore.Images.ImageColumns.DATA),
                null,
                null,
                null
            )
        cursor?.moveToFirst()
        val path = cursor?.getString(
            0
        ) ?: ""
        cursor?.close()
        return path
    } else {
        return imageUri.path ?: ""
    }
}

fun Context.isPhotoPickerAvailable(): Boolean {
    return ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context = this)
}