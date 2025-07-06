package com.cp.fishthebreak.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import org.greenrobot.eventbus.EventBus
import java.util.UUID
import javax.inject.Singleton

@Singleton
class WorkerStarter(context : Context) {

    private val workManager = WorkManager.getInstance(context)

    companion object {
        const val WORK_MANAGER_TROLLING_TAG = "WORK_MANAGER_TROLLING_TAG"
        var workRequestId: UUID? = null
    }

    operator fun invoke(){
        if(isWorkerAlreadyRunning())
            return
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<SyncTrollingWorker>()
            .setConstraints(constraints)
            .addTag(WORK_MANAGER_TROLLING_TAG)
            .build()
        workRequestId = request.id
        EventBus.getDefault().post(request.id)
        workManager.enqueue(request)
    }

    private fun isWorkerAlreadyRunning(): Boolean {
        val workInfo = workManager.getWorkInfosByTag(WORK_MANAGER_TROLLING_TAG).get()
        workInfo.forEach { work ->
            if(work.state == WorkInfo.State.ENQUEUED || work.state == WorkInfo.State.RUNNING)
                return true
        }
        return false
    }
}