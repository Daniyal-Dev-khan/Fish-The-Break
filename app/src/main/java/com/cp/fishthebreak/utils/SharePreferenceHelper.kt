package com.cp.fishthebreak.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.cp.fishthebreak.models.auth.User
import com.cp.fishthebreak.models.map.LayersStatusModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SharePreferenceHelper private constructor(private val sharedPreferences: SharedPreferences) {

    private val dispatcher = Dispatchers.Default

    companion object {

        private const val USER_SHARED_PREFERENCE = "ftb__user_preferences"
        private const val IS_LOGGED_IN = "ftb__user_is_logged_in"
        private const val Is_First_Launch = "ftb__user_is_first_launch_"
        private const val Is_NEW_INSTALL = "ftb__user_is_new_install"
        private const val PUSH_TOKEN = "ftb__user_pushToken"
        private const val TOKEN = "ftb__user_token"
        private const val DEVICE_ID = "ftb__user_device_id"
        private const val EMAIL_ID = "ftb__user_email_id"
        private const val USER_MODEL = "ftb__user_user_model"
        private const val LAYERS_MODEL = "ftb__layers_model"
        private const val ARCGIS_MAP = "ftb__arcgis_map"
        private const val SOCIAL_LOGIN = "ftb__user_social_login"
        private const val NOT_SHOW_ROUTE_DIALOG = "ftb__user_NOT_SHOW_ROUTE_DIALOG"
        private const val NOT_SHOW_LOCATION_DIALOG = "ftb__user_NOT_SHOW_LOCATION_DIALOG"
        private const val NOT_SHOW_TROLLING_DIALOG = "ftb__user_NOT_SHOW_TROLLING_DIALOG"

        private var INSTANCE: SharePreferenceHelper? = null

        fun getInstance(context: Context): SharePreferenceHelper {
            val sharedPreference =
                context.getSharedPreferences(USER_SHARED_PREFERENCE, Context.MODE_PRIVATE)
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SharePreferenceHelper(sharedPreference).also { INSTANCE = it }
            }
        }
    }


    private suspend fun put(key: String, value: String) = withContext(dispatcher) {
        sharedPreferences.edit {
            putString(key, value)
        }
    }

    private suspend fun put(key: String, value: Boolean) = withContext(dispatcher) {
        sharedPreferences.edit {
            putBoolean(key, value)
        }
    }

    private suspend fun put(key: String, value: Int) = withContext(dispatcher) {
        sharedPreferences.edit {
            putInt(key, value)
        }
    }

    suspend fun saveToken(token: String) {
        put(TOKEN, token)
    }

    fun getToken(): String? {
        return sharedPreferences.getString(TOKEN, null)
    }

    suspend fun saveDevicePushToken(token: String) {
        put(PUSH_TOKEN, token)
    }

    fun getDevicePushToken(): String? {
        return sharedPreferences.getString(PUSH_TOKEN, null)
    }

    suspend fun saveUserLogIn() {
        put(IS_LOGGED_IN, true)
    }

    suspend fun saveSocialLogin() {
        put(SOCIAL_LOGIN, true)
    }

    fun isSocialLogin(): Boolean {
        return sharedPreferences.getBoolean(SOCIAL_LOGIN, false)
    }

    suspend fun isFirstLaunch(isFirstLaunch: Boolean) {
        put(Is_First_Launch, isFirstLaunch)
    }

    suspend fun isNewInstall(isNewInstall: Boolean) {
        put(Is_NEW_INSTALL, isNewInstall)
    }

    fun isUserLoggedIn(): Boolean {
        if(!sharedPreferences.contains(IS_LOGGED_IN)){
            return false
        }
        return sharedPreferences.getBoolean(IS_LOGGED_IN, false)
    }


    fun getIsFirstLaunch(): Boolean {
        return !sharedPreferences.contains(Is_First_Launch)
//        return sharedPreferences.getBoolean(Is_First_Launch, false)
    }

    fun getIsNewInstall(): Boolean {
        if(!sharedPreferences.contains(Is_NEW_INSTALL)){
            return false
        }
        return sharedPreferences.getBoolean(Is_NEW_INSTALL, false)
    }

    suspend fun saveDevice(device_id: String) {
        put(DEVICE_ID, device_id)
    }
    suspend fun saveEmail(email: String?) {
        put(EMAIL_ID, email?:"")
    }

    fun getEmail(): String? {
        return sharedPreferences.getString(EMAIL_ID, null)
    }

    fun getUser(): User? {
        return try {
            Gson().fromJson(sharedPreferences.getString(USER_MODEL, ""), User::class.java)
        } catch (ex: Exception) {
            Log.e("getUser error", ex.message.toString())
            null
        }
    }

    suspend fun saveUser(user: User?) {
        if (user == null) {
            return
        }
        try {
            put(USER_MODEL, Gson().toJson(user))
        } catch (ex: Exception) {
            Log.e("saveUser error", ex.message.toString())
        }
    }

    fun getSavedLayers(): LayersStatusModel? {
        return try {
            Gson().fromJson(sharedPreferences.getString(LAYERS_MODEL, ""), LayersStatusModel::class.java)
        } catch (ex: Exception) {
            Log.e("getLayers error", ex.message.toString())
            null
        }
    }

    suspend fun saveLayers(layers: LayersStatusModel?) {
        if (layers == null) {
            return
        }
        try {
            put(LAYERS_MODEL, Gson().toJson(layers))
        } catch (ex: Exception) {
            Log.e("saveLayers error", ex.message.toString())
        }
    }

    fun getSavedMap(): String? {
        return try {
            sharedPreferences.getString(ARCGIS_MAP, "ARCGIS_OCEANS")
        } catch (ex: Exception) {
            Log.e("getMap error", ex.message.toString())
            "ARCGIS_OCEANS"
        }
    }

    suspend fun saveMap(map: String?) {
        if (map == null) {
            return
        }
        try {
            put(ARCGIS_MAP, map)
        } catch (ex: Exception) {
            Log.e("saveMap error", ex.message.toString())
        }
    }

    suspend fun doNotAskAgainLocationDialog() {
        put(NOT_SHOW_LOCATION_DIALOG, true)
    }
    suspend fun doNotAskAgainRouteDialog() {
        put(NOT_SHOW_ROUTE_DIALOG, true)
    }
    suspend fun doNotAskAgainTrollingDialog() {
        put(NOT_SHOW_TROLLING_DIALOG, true)
    }

    fun isDoNotAskAgainLocationDialog(): Boolean {
        if(!sharedPreferences.contains(NOT_SHOW_LOCATION_DIALOG)){
            return false
        }
        return sharedPreferences.getBoolean(NOT_SHOW_LOCATION_DIALOG, false)
    }

    fun isDoNotAskAgainRouteDialog(): Boolean {
        if(!sharedPreferences.contains(NOT_SHOW_ROUTE_DIALOG)){
            return false
        }
        return sharedPreferences.getBoolean(NOT_SHOW_ROUTE_DIALOG, false)
    }

    fun isDoNotAskAgainTrollingDialog(): Boolean {
        if(!sharedPreferences.contains(NOT_SHOW_TROLLING_DIALOG)){
            return false
        }
        return sharedPreferences.getBoolean(NOT_SHOW_TROLLING_DIALOG, false)
    }

    suspend fun clearAllPreferenceData() {
        val email = getEmail()
        sharedPreferences.edit().clear().apply()
        saveEmail(email)
    }
}
