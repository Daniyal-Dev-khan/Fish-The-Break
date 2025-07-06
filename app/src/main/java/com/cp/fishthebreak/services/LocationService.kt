package com.cp.fishthebreak.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cp.fishthebreak.di.MyApplication
import com.cp.fishthebreak.utils.getNotificationIcon
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import com.cp.fishthebreak.R
import com.cp.fishthebreak.models.trolling.TrollingPoint
import com.cp.fishthebreak.utils.Constants
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.cp.fishthebreak.viewModels.ViewModelFactory
import com.cp.fishthebreak.viewModels.profile.trolling.TrollingViewModel
import com.cp.fishthebreak.worker.LocalRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject


@AndroidEntryPoint

class LocationService : Service() {
    var counter = 0
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var title = "${MyApplication.appContext.getString(R.string.app_name)} is using your location"
    val NOTIFICATION_CHANNEL_ID = "com.cp.dating.match.partner.hukd"
    val NOTIF_ID = 1
    private var client: FusedLocationProviderClient? = null
    private var locationListener: LocationCallback? = null
    private var trollingId: Long? = null
    private val TAG = "LocationService"
    private var wakeLock: PowerManager.WakeLock? = null

    @Inject
    lateinit var localRepository: LocalRepository

    //    val viewModel: TrollingViewModel = ViewModelProvider(this, ViewModelFactory<TrollingViewModel>{
//        TrollingViewModel(first, second)
//    })[TrollingViewModel::class.java]
    override fun onCreate() {
        super.onCreate()
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) createNotificationChanel() else startForeground(
//            1,
//            Notification()
//        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChanel() else {
            startForeground(
                NOTIF_ID,
                NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).setOngoing(true)
                    .setContentTitle(
                        if (title.isNullOrEmpty()) "App is running count::"
                        else title
                    ).setPriority(NotificationManager.IMPORTANCE_HIGH)
                    .setCategory(Notification.CATEGORY_SERVICE).build()
            )
        }
        //requestLocationUpdates()
    }

    private fun createNotificationChanel(title: String = "") {
        val channelName = "Background_Service"
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        chan.canBypassDnd()
        val manager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val builder = notificationBuilder.setOngoing(true).setContentTitle(
            if (title.isNullOrEmpty()) "App is running count::"
            else title
        ).setPriority(NotificationManager.IMPORTANCE_HIGH)
        //.setCategory(Notification.CATEGORY_SERVICE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
        }
        val notification = builder.build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIF_ID, notification, FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIF_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        trollingId = intent?.getLongExtra("trollingId", -1)
        Log.i("serviceTrollingId", trollingId.toString())
        requestLocationUpdates()
        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                    acquire()
//                    acquire(10*60*1000L /*10 minutes*/)
                }
            }
        getLocation()
        //startTimer()
        return START_STICKY
        //return START_NOT_STICKY // https://stackoverflow.com/questions/51458421/service-is-killed-after-a-short-period-of-time-1-minute
    }

    override fun onDestroy() {
        Log.i("onDestroy service", "onDestroy")
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
        } catch (e: Exception) {
        }
        stoptimertask()
        locationListener?.let { client?.removeLocationUpdates(it) }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        super.onDestroy()
//        val broadcastIntent = Intent()
//        broadcastIntent.action = "restartservice"
//        broadcastIntent.setClass(this, RestartBackgroundService::class.java)
//        this.sendBroadcast(broadcastIntent)
    }

    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    fun startTimer() {
        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() {
                var count = counter++
                if (latitude != 0.0 && longitude != 0.0) {
                    Log.i("timerTask", "timerTaskRun")
                    Log.d(
                        "Location::",
                        latitude.toString() + ":::" + longitude.toString() + "Count" + count.toString()
                    )
                    getLocation()
                }
            }
        }
        timer!!.schedule(
            timerTask, 0, 1800000
        ) //1 * 60 * 1000 1 minute
    }

    fun stoptimertask() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun requestLocationUpdates() {
//        val request = LocationRequest()
//        request.interval = 1000
////        request.fastestInterval = 60000
//        request.fastestInterval = 1000
//        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(1000)
            .setMaxUpdateDelayMillis(1000)
            .build()
        client =
            LocationServices.getFusedLocationProviderClient(this)

        val permission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permission == PackageManager.PERMISSION_GRANTED) { // Request location updates and when an update is
            // received, store the location in Firebase
            locationListener = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location: Location? = locationResult.lastLocation
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                        val thread = Thread {
                            try {
                                Log.i(
                                    "onLocationResult",
                                    "requestLocationUpdates ${location.hasSpeed()}, ${location.speed}, ${location.speedAccuracyMetersPerSecond}"
                                )
                                EventBus.getDefault().post(location)
//                                getLatLng.getCurrentLocation(latitude, longitude)
                                //Your code goes here
                                saveTrollingPoint(location, trollingId)
                                //getLocation()

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        thread.start()
                    }
                }
            }
            locationListener?.let { listener ->
                client?.requestLocationUpdates(
                    request,
                    listener,
                    Looper.getMainLooper()
                )
            }
        }
    }

    fun getLocation() {
        val builder =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).setOngoing(true)
                .setContentTitle(
                    title
                )
//                .setContentText(
//                    title
//                )
                .setSmallIcon(this.getNotificationIcon())
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setColor(ContextCompat.getColor(this, R.color.secondary800))
        //.setCategory(Notification.CATEGORY_SERVICE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
        }
        val notification = builder.build()
        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        var notificationChannel: NotificationChannel? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Background_Service"
            notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = ""
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            mNotificationManager.createNotificationChannel(notificationChannel)
        }
        mNotificationManager.notify(NOTIF_ID, notification)
    }

    fun saveTrollingPoint(location: Location, id: Long?) {
        if (id == null) {
            Log.i("onDestroy service", "onDestroy")
            try {
                wakeLock?.let {
                    if (it.isHeld) {
                        it.release()
                    }
                }
            } catch (e: Exception) {
            }
            stoptimertask()
            locationListener?.let { client?.removeLocationUpdates(it) }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            Log.i("saveTrollingPointId", id.toString())
            localRepository.saveTrollingPoints(
                TrollingPoint(
                    0,
                    id,
                    location.latitude,
                    location.longitude,
                    location.speed * 3.6F,
                    Date().time
                )
            ).collect { values ->
                Log.i("saveTrollingPoints", values.toString())
            }
        }
    }

}