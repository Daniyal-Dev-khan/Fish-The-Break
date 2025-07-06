package com.cp.fishthebreak.viewModels.profile.trolling

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.cp.fishthebreak.models.trolling.Trolling
import com.cp.fishthebreak.models.trolling.TrollingPoint
import com.cp.fishthebreak.worker.LocalRepository
import com.cp.fishthebreak.worker.Repository
import com.cp.fishthebreak.worker.WorkerStarter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class TrollingViewModel @Inject constructor
    (
    private val repository: Repository,
    private val localRepository: LocalRepository,
    private val workerStarter: WorkerStarter,
    private val applicationContext: Application
) : AndroidViewModel(applicationContext) {

    private val _trollingResponse: MutableStateFlow<Long?> =
        MutableStateFlow(null)
    val trollingResponse: StateFlow<Long?> = _trollingResponse.asStateFlow()

    private val _trollingId: MutableStateFlow<Long> =
        MutableStateFlow(Date().time)
    val trollingId: StateFlow<Long> = _trollingId.asStateFlow()

    init {
        //getTrolling()
        //workerStarter.invoke()
        syncTrolling()
    }

    fun syncTrolling(){
        viewModelScope.launch {
            localRepository.updateTrollingStatus().collect { _ ->
                workerStarter.invoke()
            }
        }
    }

    fun saveTrolling(trollingId: Long) = viewModelScope.launch {
        _trollingId.value = trollingId
        val id = _trollingId.value
        localRepository.saveTrolling(Trolling(id,null, id.toString(), 0F, 0F, 0F, null,false, isSaved = false)).collect { values ->
            Log.i("saveTrolling", values.toString())
        }
    }

    fun updateTrollingName(name: String, time: String) = viewModelScope.launch {
        val id = _trollingId.value
        localRepository.getMinMaxSpeed(id).collect { speed ->
            localRepository.updateTrollingName(Trolling(id,null, name,speed.minValue,speed.maxValue,speed.averageValue, time,false, isSaved = true)).collect { values ->
                _trollingResponse.value = values.toLong()
                Log.i("saveTrollingName", values.toString())
            }
        }
    }

    fun deleteTrolling() = viewModelScope.launch {
        val id = _trollingId.value
        localRepository.deleteTrollingById(id).collect { values ->
            localRepository.deletePointByTrollingId(id).collect{
                localRepository.deleteFishLogByTrollingId(id).collect{

                }
            }
            Log.i("trollingRemoved", values.toString())
        }
    }

    fun saveTrollingPoint(location: Location) = viewModelScope.launch {
        localRepository.saveTrollingPoints(
            TrollingPoint(
                0,
                _trollingId.value,
                location.latitude,
                location.longitude,
                location.speed,
                Date().time
            )
        ).collect { values ->
            Log.i("saveTrollingPoints", values.toString())
        }
    }

    fun getTrolling() = viewModelScope.launch {
        localRepository.getAllTrollingWithPoints().collect { values ->
            Log.i("getTrolling", "$values")
        }
    }

    fun resetResponse() {
        _trollingResponse.value = null
    }

}