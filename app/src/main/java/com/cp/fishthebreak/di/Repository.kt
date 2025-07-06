package com.cp.fishthebreak.di

import android.content.Context
import com.cp.fishthebreak.models.auth.DeleteAccountModel
import com.cp.fishthebreak.models.vessel.SaveVesselModel
import com.cp.fishthebreak.models.auth.LogoutModel
import com.cp.fishthebreak.models.auth.RegisterModel
import com.cp.fishthebreak.models.auth.ResetPasswordModel
import com.cp.fishthebreak.models.auth.SendCodeModel
import com.cp.fishthebreak.models.auth.VerifyPasswordOtpModel
import com.cp.fishthebreak.models.group.AddMembersModel
import com.cp.fishthebreak.models.group.BookMarkModel
import com.cp.fishthebreak.models.group.ChatListModel
import com.cp.fishthebreak.models.group.CreateGroupModel
import com.cp.fishthebreak.models.group.FindUsersModel
import com.cp.fishthebreak.models.group.GroupDetailsModel
import com.cp.fishthebreak.models.group.GroupMemberModel
import com.cp.fishthebreak.models.group.LeaveGroupModel
import com.cp.fishthebreak.models.group.LeaveReasonModel
import com.cp.fishthebreak.models.group.RequestAcceptModel
import com.cp.fishthebreak.models.group.RequestRejectModel
import com.cp.fishthebreak.models.group.ShareInGroupModel
import com.cp.fishthebreak.models.group.UpdateGroupModel
import com.cp.fishthebreak.models.home.GetSearchModel
import com.cp.fishthebreak.models.home.SaveSearchModel
import com.cp.fishthebreak.models.map.GetAllLayerModel
import com.cp.fishthebreak.models.points.GetFishLogsModel
import com.cp.fishthebreak.models.points.GetSavedPointsModel
import com.cp.fishthebreak.models.points.SaveFishLogModel
import com.cp.fishthebreak.models.points.SavePointsModel
import com.cp.fishthebreak.models.profile.AddSubscriptionModel
import com.cp.fishthebreak.models.profile.AllResourcesModel
import com.cp.fishthebreak.models.profile.ChangeEmailModel
import com.cp.fishthebreak.models.profile.ChangePasswordModel
import com.cp.fishthebreak.models.profile.GetSubscribedPackageModel
import com.cp.fishthebreak.models.profile.ResourceListModel
import com.cp.fishthebreak.models.profile.StartTrialModel
import com.cp.fishthebreak.models.routes.GetAllRouteModel
import com.cp.fishthebreak.models.routes.SaveRouteModel
import com.cp.fishthebreak.models.trolling.GetAllTrollingModel
import com.cp.fishthebreak.models.trolling.TrollingResponseModel
import com.cp.fishthebreak.models.vessel.GetVesselModel
import com.cp.fishthebreak.utils.SharePreferenceHelper
import com.google.gson.JsonObject
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@ActivityRetainedScoped
class Repository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val preferenceHelper: SharePreferenceHelper,
    @ApplicationContext private val mContext: Context
) : BaseApiResponse() {

    suspend fun login(
        body: JsonObject
    ): Flow<NetworkResult<RegisterModel>> {
        return flow {
            emit(safeApiCall { remoteDataSource.login(body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun socialLogin(
        body: JsonObject
    ): Flow<NetworkResult<RegisterModel>> {
        return flow {
            emit(safeApiCall { remoteDataSource.socialLogin(body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun signup(
        body: JsonObject
    ): Flow<NetworkResult<RegisterModel>> {
        return flow {
            emit(safeApiCall { remoteDataSource.signup(body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun verifyOtp(
        body: JsonObject
    ): Flow<NetworkResult<RegisterModel>> {
        return flow {
            emit(safeApiCall { remoteDataSource.verifyOtp(body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun sendOtp(
        body: JsonObject
    ): Flow<NetworkResult<SendCodeModel>> {
        return flow {
            emit(safeApiCall { remoteDataSource.sendOtp(body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun verifyPasswordResetOtp(
        body: JsonObject
    ): Flow<NetworkResult<VerifyPasswordOtpModel>> {
        return flow {
            emit(safeApiCall { remoteDataSource.verifyPasswordResetOtp(body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun resetPassword(
        body: JsonObject
    ): Flow<NetworkResult<ResetPasswordModel>> {
        return flow {
            emit(safeApiCall { remoteDataSource.resetPassword(body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getAllLayers(
    ): Flow<NetworkResult<GetAllLayerModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall { remoteDataSource.getAllLayers("Bearer $token") })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun saveSearch(
        body: JsonObject
    ): Flow<NetworkResult<SaveSearchModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall { remoteDataSource.saveSearch("Bearer $token", body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getSearchHistory(
        body: JsonObject
    ): Flow<NetworkResult<GetSearchModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall { remoteDataSource.getSearchHistory("Bearer $token", body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getProfile(
    ): Flow<NetworkResult<RegisterModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall { remoteDataSource.getProfile("Bearer $token") })
        }.flowOn(Dispatchers.IO)
    }


    suspend fun editProfile(
        body: JsonObject
    ): Flow<NetworkResult<RegisterModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall { remoteDataSource.editProfile("Bearer $token", body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun editProfile(
        firstName: RequestBody?,
        lastName: RequestBody,
        userName: RequestBody,
        mobile: RequestBody,
        profilePic: MultipartBody.Part?,
    ): Flow<NetworkResult<RegisterModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.editProfile(
                    "Bearer $token",
                    firstName,
                    lastName,
                    userName,
                    mobile,
                    profilePic
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun editProfile(
        firstName: RequestBody?,
        lastName: RequestBody,
        mobile: RequestBody,
        profilePic: MultipartBody.Part?,
    ): Flow<NetworkResult<RegisterModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.editProfile(
                    "Bearer $token",
                    firstName,
                    lastName,
                    mobile,
                    profilePic
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun editProfile(
        profilePic: MultipartBody.Part?,
    ): Flow<NetworkResult<RegisterModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall { remoteDataSource.editProfile("Bearer $token", profilePic) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updatePassword(
        body: JsonObject
    ): Flow<NetworkResult<ChangePasswordModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall { remoteDataSource.updatePassword("Bearer $token", body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updateEmail(
        body: JsonObject
    ): Flow<NetworkResult<ChangeEmailModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall { remoteDataSource.updateEmail("Bearer $token", body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun verifyEmailOtp(
        body: JsonObject
    ): Flow<NetworkResult<RegisterModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall { remoteDataSource.verifyEmailOtp("Bearer $token", body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun boatRange(
        body: JsonObject
    ): Flow<NetworkResult<RegisterModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall { remoteDataSource.boatRange("Bearer $token", body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun toggleVessel(
    ): Flow<NetworkResult<RegisterModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall { remoteDataSource.toggleVessel("Bearer $token") })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getAllResources(
    ): Flow<NetworkResult<AllResourcesModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall { remoteDataSource.getAllResources("Bearer $token") })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getResourceListById(
        id: Int
    ): Flow<NetworkResult<ResourceListModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall { remoteDataSource.getResourceListById("Bearer $token", id) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun logout(
        body: JsonObject
    ): Flow<NetworkResult<LogoutModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall { remoteDataSource.logout("Bearer $token", body) })
        }.flowOn(Dispatchers.IO)
    }
    suspend fun deleteAccount(
    ): Flow<NetworkResult<DeleteAccountModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall { remoteDataSource.deleteAccount("Bearer $token") })
        }.flowOn(Dispatchers.IO)
    }
    suspend fun deletePointsById(
        body: JsonObject
    ): Flow<NetworkResult<SavePointsModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall { remoteDataSource.deletePointsById("Bearer $token", body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getSavedPoints(
        searchText: String,
        limit: Int,
        page: Int,
        type: Int?,
        trollingId: Int?,
    ): Flow<NetworkResult<GetSavedPointsModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
               if(type == null){
                   remoteDataSource.getSavedPoints(
                       "Bearer $token",
                       searchText,
                       limit,
                       page,
                       trollingId
                   )
               }else{
                   remoteDataSource.getSavedPoints(
                       "Bearer $token",
                       searchText,
                       limit,
                       page,
                       type,
                       trollingId
                   )
               }
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun savePoints(
        pointName: RequestBody?,
        lat: RequestBody,
        lang: RequestBody,
        description: RequestBody,
        date: RequestBody,
        time: RequestBody,
        local_db_id: RequestBody,
        local_db_unique_id: RequestBody,
        profilePic: MultipartBody.Part?,
    ): Flow<NetworkResult<SavePointsModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.savePoints(
                    "Bearer $token",
                    pointName,
                    lat,
                    lang,
                    description,
                    date,
                    time,
                    local_db_id,
                    local_db_unique_id,
                    profilePic,
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun savePoints(
        body: JsonObject
    ): Flow<NetworkResult<SavePointsModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.savePoints(
                    "Bearer $token",
                    body
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updatePoints(
        body: JsonObject
    ): Flow<NetworkResult<SavePointsModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.updatePoints(
                    "Bearer $token",
                    body
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updatePoints(
        id: RequestBody?,
        pointName: RequestBody?,
        lat: RequestBody,
        lang: RequestBody,
        description: RequestBody,
        profilePic: MultipartBody.Part?,
    ): Flow<NetworkResult<SavePointsModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.updatePoints(
                    "Bearer $token",
                    id,
                    pointName,
                    lat,
                    lang,
                    description,
                    profilePic
                )
            })
        }.flowOn(Dispatchers.IO)
    }
    suspend fun getPointById(
        id: Int
    ): Flow<NetworkResult<SavePointsModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.getPointById(
                    "Bearer $token",
                    id
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun saveFishLog(
        body: JsonObject
    ): Flow<NetworkResult<SaveFishLogModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.saveFishLog(
                    "Bearer $token",
                    body
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun saveFishLog(
        fishName: RequestBody?,
        lat: RequestBody,
        lang: RequestBody,
        description: RequestBody,
        length: RequestBody,
        weight: RequestBody,
        date: RequestBody,
        time: RequestBody,
        local_db_id: RequestBody,
        local_db_unique_id: RequestBody,
        profilePic: MultipartBody.Part?,
    ): Flow<NetworkResult<SaveFishLogModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.saveFishLog(
                    "Bearer $token",
                    fishName,
                    lat,
                    lang,
                    description,
                    length,
                    weight,
                    date,
                    time,
                    local_db_id,
                    local_db_unique_id,
                    profilePic
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updateFishLog(
        body: JsonObject
    ): Flow<NetworkResult<SaveFishLogModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.updateFishLog(
                    "Bearer $token",
                    body
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updateFishLog(
        id: RequestBody?,
        fishName: RequestBody?,
        lat: RequestBody,
        lang: RequestBody,
        description: RequestBody,
        length: RequestBody,
        weight: RequestBody,
        date: RequestBody,
        time: RequestBody,
        profilePic: MultipartBody.Part?,
    ): Flow<NetworkResult<SaveFishLogModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.updateFishLog(
                    "Bearer $token",
                    id,
                    fishName,
                    lat,
                    lang,
                    description,
                    length,
                    weight,
                    date,
                    time,
                    profilePic
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deleteFishLogById(
        body: JsonObject
    ): Flow<NetworkResult<SaveFishLogModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall { remoteDataSource.deleteFishLogById("Bearer $token", body) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getSavedFishLogs(
        searchText: String,
        limit: Int,
        page: Int,
        type: Int?,
        trollingId: Int?
    ): Flow<NetworkResult<GetFishLogsModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                if(type == null){
                    remoteDataSource.getSavedFishLogs(
                        "Bearer $token",
                        searchText,
                        limit,
                        page,
                        trollingId
                    )
                }else{
                    remoteDataSource.getSavedFishLogs(
                        "Bearer $token",
                        searchText,
                        limit,
                        page,
                        type,
                        trollingId
                    )
                }
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getFishLogById(
        id: Int
    ): Flow<NetworkResult<SaveFishLogModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.getFishLogById(
                    "Bearer $token",
                    id
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun saveVessel(
        body: JsonObject
    ): Flow<NetworkResult<SaveVesselModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.saveVessel(
                    "Bearer $token",
                    body
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updateVessel(
        body: JsonObject
    ): Flow<NetworkResult<SaveVesselModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.updateVessel(
                    "Bearer $token",
                    body
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getAllVessel(
    ): Flow<NetworkResult<GetVesselModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.getAllVessel(
                    "Bearer $token"
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun saveRoute(
        name: RequestBody?,
        description: RequestBody,
        date: RequestBody,
        time: RequestBody,
        locations: RequestBody,
        profilePic: MultipartBody.Part?,
    ): Flow<NetworkResult<SaveRouteModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.saveRoute(
                    "Bearer $token",
                    name,
                    description,
                    date,
                    time,
                    locations,
                    profilePic
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun saveRoute(
        body: JsonObject
    ): Flow<NetworkResult<SaveRouteModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.saveRoute(
                    "Bearer $token",
                    body
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getGroupList(
        limit: Int,
        page: Int
    ): Flow<NetworkResult<ChatListModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.getGroupList(
                    "Bearer $token",
                    limit,
                    page
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getGroupChat(
        limit: Int,
        page: Int,
        roomId: Int
    ): Flow<NetworkResult<GroupDetailsModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.getGroupChat(
                    "Bearer $token",
                    limit,
                    page,
                    roomId
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun createGroup(
        body: JsonObject
    ): Flow<NetworkResult<CreateGroupModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.createGroup(
                    "Bearer $token",
                    body
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun addMembersInGroup(
        body: JsonObject
    ): Flow<NetworkResult<AddMembersModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.addMembersInGroup(
                    "Bearer $token",
                    body
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun createGroup(
        name: RequestBody?,
        userIds: RequestBody,
        profilePic: MultipartBody.Part?,
    ): Flow<NetworkResult<CreateGroupModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.createGroup(
                    "Bearer $token",
                    name,
                    userIds,
                    profilePic
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updateGroup(
        body: JsonObject
    ): Flow<NetworkResult<UpdateGroupModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.updateGroup(
                    "Bearer $token",
                    body
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updateGroup(
        name: RequestBody?,
        userId: RequestBody,
        profilePic: MultipartBody.Part?,
    ): Flow<NetworkResult<UpdateGroupModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.updateGroup(
                    "Bearer $token",
                    name,
                    userId,
                    profilePic
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun findUsers(
        limit: Int,
        page: Int,
        searchText: String,
        roomId: Int?,
    ): Flow<NetworkResult<FindUsersModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                if(roomId == null){
                    remoteDataSource.findUsers(
                        "Bearer $token",
                        limit,
                        page,
                        searchText
                    )
                }else{
                    remoteDataSource.findUsers(
                        "Bearer $token",
                        limit,
                        page,
                        searchText,
                        roomId
                    )
                }
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getGroupUsers(
        limit: Int,
        page: Int,
        roomId: Int,
    ): Flow<NetworkResult<GroupMemberModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.getGroupUsers(
                    "Bearer $token",
                    limit,
                    page,
                    roomId
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun bookmarkChat(
        body: JsonObject
    ): Flow<NetworkResult<BookMarkModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.bookmarkChat(
                    "Bearer $token",
                    body
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun leaveGroup(
        body: JsonObject
    ): Flow<NetworkResult<LeaveGroupModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.leaveGroup(
                    "Bearer $token",
                    body
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getAllTrolling(
        limit: Int,
        page: Int,
        searchText: String,
        type: Int?
    ): Flow<NetworkResult<GetAllTrollingModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                if(type == null) {
                    remoteDataSource.getAllTrolling(
                        "Bearer $token",
                        limit,
                        page,
                        searchText
                    )
                }
                else{
                    remoteDataSource.getAllTrolling(
                        "Bearer $token",
                        limit,
                        page,
                        searchText,
                        type
                    )
                }
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getTrollingById(
        id: Int
    ): Flow<NetworkResult<TrollingResponseModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.getTrollingById(
                    "Bearer $token",
                    id
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getAllRoute(
        limit: Int,
        page: Int,
        searchText: String,
        type: Int?,
    ): Flow<NetworkResult<GetAllRouteModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                if(type == null){
                    remoteDataSource.getAllRoute(
                        "Bearer $token",
                        limit,
                        page,
                        searchText
                    )
                } else {
                    remoteDataSource.getAllRoute(
                        "Bearer $token",
                        limit,
                        page,
                        searchText,
                        type
                    )
                }
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun sharePointInGroup(
        body: JsonObject
    ): Flow<NetworkResult<ShareInGroupModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.sharePointInGroup(
                    "Bearer $token",
                    body
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deleteRouteById(
        body: JsonObject
    ): Flow<NetworkResult<SaveRouteModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.deleteRouteById(
                    "Bearer $token",
                    body
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deleteTrollingById(
        body: JsonObject
    ): Flow<NetworkResult<TrollingResponseModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.deleteTrollingById(
                    "Bearer $token",
                    body
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updateTrolling(
        body: JsonObject
    ): Flow<NetworkResult<TrollingResponseModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.updateTrolling(
                    "Bearer $token",
                    body
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updateRoute(
        body: JsonObject
    ): Flow<NetworkResult<SaveRouteModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.updateRoute(
                    "Bearer $token",
                    body
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updateRoute(
        id: RequestBody?,
        name: RequestBody?,
        description: RequestBody,
        profilePic: MultipartBody.Part?,
    ): Flow<NetworkResult<SaveRouteModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.updateRoute(
                    "Bearer $token",
                    id,
                    name,
                    description,
                    profilePic
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getSingleRouteById(
        id: Int
    ): Flow<NetworkResult<SaveRouteModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.getSingleRouteById(
                    "Bearer $token",
                    id
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun subscribePackage(
        body: JsonObject
    ): Flow<NetworkResult<AddSubscriptionModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.subscribePackage(
                    "Bearer $token",
                    body
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getSubscribedPackage(
    ): Flow<NetworkResult<GetSubscribedPackageModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.getSubscribedPackage(
                    "Bearer $token"
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun startFreeTrial(
    ): Flow<NetworkResult<StartTrialModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.startFreeTrial(
                    "Bearer $token"
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun acceptRequest(
        body: JsonObject
    ): Flow<NetworkResult<RequestAcceptModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.acceptRequest(
                    "Bearer $token",
                    body
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun rejectRequest(
        body: JsonObject
    ): Flow<NetworkResult<RequestRejectModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.rejectRequest(
                    "Bearer $token",
                    body
                )
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getRoomLeavingReason(
    ): Flow<NetworkResult<LeaveReasonModel>> {
        return flow {
            val token = preferenceHelper.getToken()
            emit(safeApiCall {
                remoteDataSource.getRoomLeavingReason(
                    "Bearer $token"
                )
            })
        }.flowOn(Dispatchers.IO)
    }
}