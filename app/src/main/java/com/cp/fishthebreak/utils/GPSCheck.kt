package com.cp.fishthebreak.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.location.LocationManager
import android.os.Build

class GPSCheck(private val locationCallBack: LocationCallBack) :
    BroadcastReceiver() {
    interface LocationCallBack {
        fun turnedOn()
        fun turnedOff()
    }

    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                manager.isLocationEnabled
            } else {
                manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            }
        ) locationCallBack.turnedOn() else locationCallBack.turnedOff()
    }

}