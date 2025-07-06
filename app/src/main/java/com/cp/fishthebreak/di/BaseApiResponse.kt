package com.cp.fishthebreak.di

import android.content.Intent
import androidx.room.Room
import com.bumptech.glide.Glide
import com.cp.fishthebreak.di.local.AppDatabase
import com.cp.fishthebreak.screens.activities.AuthActivity
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.cp.fishthebreak.utils.deleteAppCache
import com.cp.fishthebreak.utils.deleteAppDownloadCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

abstract class BaseApiResponse {
    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
        try {
            val response = apiCall()
            if(response.code() == 401){
                val preference_ = SharePreferenceHelper.getInstance(MyApplication.appContext)
                preference_.clearAllPreferenceData()
                preference_.isFirstLaunch(false)
                MyApplication.appContext.deleteAppCache()
                MyApplication.appContext.deleteAppDownloadCache()
                CoroutineScope(Dispatchers.IO).launch {
                    Room.databaseBuilder(
                        MyApplication.appContext,
                        AppDatabase::class.java,
                        "TrollingPoints"
                    ).build().clearAllTables()
                }
                val sIntent = Intent(MyApplication.appContext, AuthActivity::class.java)
                sIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                MyApplication.appContext.startActivity(sIntent)
                return error("${response.code()} ${response.message()}")
            }
            if (response.isSuccessful) {
                val body = response.body()
                body?.let {
                    return NetworkResult.Success(body)
                }
            }
            return try {
                val jObjError =
                    JSONObject((response as Response<*>).errorBody()!!.string())
                return error(jObjError.toString())
            } catch (e: Exception) {
                error("${response.code()} ${response.message()}")
            }
            //return error("${response.code()} ${response.message()}")
        } catch (e: Exception) {
            return error(e.message ?: e.toString())
        }
    }

    private fun <T> error(errorMessage: String,data: T? = null): NetworkResult<T> =
        NetworkResult.Error(errorMessage, data)
//        NetworkResult.Error("Api call failed $errorMessage",data)
}