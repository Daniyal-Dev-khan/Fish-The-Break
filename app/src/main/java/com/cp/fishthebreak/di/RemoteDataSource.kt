package com.cp.fishthebreak.di

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class RemoteDataSource @Inject constructor(private val apiService: ApiInterface) {
    suspend fun login(body: JsonObject) =
        apiService.login(body)

    suspend fun socialLogin(body: JsonObject) =
        apiService.socialLogin(body)

    suspend fun signup(body: JsonObject) =
        apiService.signup(body)

    suspend fun verifyOtp(body: JsonObject) =
        apiService.verifyOtp(body)

    suspend fun sendOtp(body: JsonObject) =
        apiService.sendOtp(body)

    suspend fun verifyPasswordResetOtp(body: JsonObject) =
        apiService.verifyPasswordResetOtp(body)

    suspend fun resetPassword(body: JsonObject) =
        apiService.resetPassword(body)

    suspend fun getAllLayers(authToken: String) =
        apiService.getAllLayers(authToken, 100)

    suspend fun saveSearch(authToken: String, body: JsonObject) =
        apiService.saveSearch(authToken, body)

    suspend fun editProfile(authToken: String, body: JsonObject) =
        apiService.editProfile(authToken, body)

    suspend fun editProfile(
        authToken: String,
        firstName: RequestBody?,
        lastName: RequestBody,
        userName: RequestBody,
        mobile: RequestBody,
        profilePic: MultipartBody.Part?,
    ) =
        apiService.editProfile(
            authToken,
            firstName,
            lastName,
            userName,
            mobile,
            profilePic
        )

    suspend fun editProfile(
        authToken: String,
        firstName: RequestBody?,
        lastName: RequestBody,
        mobile: RequestBody,
        profilePic: MultipartBody.Part?,
    ) =
        apiService.editProfile(
            authToken,
            firstName,
            lastName,
            mobile,
            profilePic
        )

    suspend fun editProfile(
        authToken: String,
        profilePic: MultipartBody.Part?,
    ) =
        apiService.editProfile(
            authToken,
            profilePic
        )


    suspend fun getSearchHistory(authToken: String, body: JsonObject) =
        apiService.getSearchHistory(authToken, body)

    suspend fun getProfile(authToken: String) =
        apiService.getProfile(authToken)

    suspend fun updatePassword(authToken: String, body: JsonObject) =
        apiService.updatePassword(authToken, body)

    suspend fun updateEmail(authToken: String, body: JsonObject) =
        apiService.updateEmail(authToken, body)

    suspend fun verifyEmailOtp(authToken: String, body: JsonObject) =
        apiService.verifyEmailOtp(authToken, body)

    suspend fun boatRange(authToken: String, body: JsonObject) =
        apiService.boatRange(authToken, body)

    suspend fun toggleVessel(authToken: String) =
        apiService.toggleVessel(authToken)

    suspend fun getAllResources(authToken: String) =
        apiService.getAllResources(authToken)

    suspend fun getResourceListById(authToken: String, id: Int) =
        apiService.getResourceListById(authToken, id)

    suspend fun logout(authToken: String, body: JsonObject) =
        apiService.logout(authToken, body)

    suspend fun deleteAccount(authToken: String) =
        apiService.deleteAccount(authToken)

    suspend fun deletePointsById(authToken: String, body: JsonObject) =
        apiService.deletePointsById(authToken, body)

    suspend fun getSavedPoints(
        authToken: String,
        searchText: String,
        limit: Int,
        page: Int,
        trollingId: Int?
    ) =
        apiService.getSavedPoints(authToken, searchText, limit, page, trollingId)

    suspend fun getSavedPoints(
        authToken: String,
        searchText: String,
        limit: Int,
        page: Int,
        type: Int,
        trollingId: Int?
    ) =
        apiService.getSavedPoints(authToken, searchText, limit, page, type, trollingId)

    suspend fun savePoints(
        authToken: String,
        pointName: RequestBody?,
        lat: RequestBody,
        lang: RequestBody,
        description: RequestBody,
        date: RequestBody,
        time: RequestBody,
        local_db_id: RequestBody,
        local_db_unique_id: RequestBody,
        profilePic: MultipartBody.Part?,
    ) =
        apiService.savePoints(
            authToken,
            pointName,
            lat,
            lang,
            description,
            date,
            time,
            local_db_id,
            local_db_unique_id,
            profilePic
        )

    suspend fun savePoints(authToken: String, body: JsonObject) =
        apiService.savePoints(authToken, body)

    suspend fun updatePoints(authToken: String, body: JsonObject) =
        apiService.updatePoints(authToken, body)

    suspend fun updatePoints(
        authToken: String,
        id: RequestBody?,
        pointName: RequestBody?,
        lat: RequestBody,
        lang: RequestBody,
        description: RequestBody,
        profilePic: MultipartBody.Part?,
    ) =
        apiService.updatePoints(
            authToken,
            id,
            pointName,
            lat,
            lang,
            description,
            profilePic
        )

    suspend fun getPointById(
        authToken: String,
        id: Int
    ) =
        apiService.getPointById(
            authToken,
            id
        )

    suspend fun saveFishLog(authToken: String, body: JsonObject) =
        apiService.saveFishLog(authToken, body)

    suspend fun saveFishLog(
        authToken: String,
        pointName: RequestBody?,
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
    ) =
        apiService.saveFishLog(
            authToken,
            pointName,
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

    suspend fun updateFishLog(authToken: String, body: JsonObject) =
        apiService.updateFishLog(authToken, body)

    suspend fun updateFishLog(
        authToken: String,
        id: RequestBody?,
        pointName: RequestBody?,
        lat: RequestBody,
        lang: RequestBody,
        description: RequestBody,
        length: RequestBody,
        weight: RequestBody,
        date: RequestBody,
        time: RequestBody,
        profilePic: MultipartBody.Part?,
    ) =
        apiService.updateFishLog(
            authToken,
            id,
            pointName,
            lat,
            lang,
            description,
            length,
            weight,
            date,
            time,
            profilePic
        )

    suspend fun deleteFishLogById(authToken: String, body: JsonObject) =
        apiService.deleteFishLogById(authToken, body)

    suspend fun getSavedFishLogs(
        authToken: String,
        searchText: String,
        limit: Int,
        page: Int,
        trollingId: Int?
    ) =
        apiService.getSavedFishLogs(authToken, searchText, limit, page, trollingId)

    suspend fun getSavedFishLogs(
        authToken: String,
        searchText: String,
        limit: Int,
        page: Int,
        type: Int,
        trollingId: Int?
    ) =
        apiService.getSavedFishLogs(authToken, searchText, limit, page, type, trollingId)

    suspend fun getFishLogById(
        authToken: String,
        id: Int
    ) =
        apiService.getFishLogById(
            authToken,
            id
        )

    suspend fun saveTrolling(
        authToken: String,
        body: JsonObject
    ) =
        apiService.saveTrolling(
            authToken,
            body
        )

    suspend fun saveTrollingPoints(
        authToken: String,
        body: JsonObject
    ) =
        apiService.saveTrollingPoints(
            authToken,
            body
        )

    suspend fun saveVessel(authToken: String, body: JsonObject) =
        apiService.saveVessel(authToken, body)

    suspend fun updateVessel(authToken: String, body: JsonObject) =
        apiService.updateVessel(authToken, body)

    suspend fun getAllVessel(authToken: String) =
        apiService.getAllVessel(authToken)

    suspend fun saveRoute(
        authToken: String,
        name: RequestBody?,
        description: RequestBody,
        date: RequestBody,
        time: RequestBody,
        locations: RequestBody,
        profilePic: MultipartBody.Part?,
    ) =
        apiService.saveRoute(
            authToken,
            name,
            description,
            date,
            time,
            locations,
            profilePic
        )

    suspend fun saveRoute(authToken: String, body: JsonObject) =
        apiService.saveRoute(authToken, body)

    suspend fun getGroupList(authToken: String, limit: Int, page: Int) =
        apiService.getGroupList(authToken, limit, page)

    suspend fun getGroupChat(authToken: String, limit: Int, page: Int, roomId: Int) =
        apiService.getGroupChat(authToken, limit, page, roomId)

    suspend fun createGroup(authToken: String, body: JsonObject) =
        apiService.createGroup(authToken, body)

    suspend fun addMembersInGroup(authToken: String, body: JsonObject) =
        apiService.addMembersInGroup(authToken, body)

    suspend fun createGroup(
        authToken: String,
        name: RequestBody?,
        userIds: RequestBody,
        profilePic: MultipartBody.Part?,
    ) =
        apiService.createGroup(
            authToken,
            name,
            userIds,
            profilePic
        )

    suspend fun updateGroup(authToken: String, body: JsonObject) =
        apiService.updateGroup(authToken, body)

    suspend fun updateGroup(
        authToken: String,
        name: RequestBody?,
        userId: RequestBody,
        profilePic: MultipartBody.Part?,
    ) =
        apiService.updateGroup(
            authToken,
            name,
            userId,
            profilePic
        )

    suspend fun findUsers(
        authToken: String,
        limit: Int,
        page: Int,
        searchText: String,
    ) =
        apiService.findUsers(
            authToken,
            limit,
            page,
            searchText
        )

    suspend fun findUsers(
        authToken: String,
        limit: Int,
        page: Int,
        searchText: String,
        roomId: Int,
    ) =
        apiService.findUsers(
            authToken,
            limit,
            page,
            searchText,
            roomId
        )

    suspend fun getGroupUsers(
        authToken: String,
        limit: Int,
        page: Int,
        roomId: Int,
    ) =
        apiService.getGroupUsers(
            authToken,
            limit,
            page,
            roomId
        )

    suspend fun bookmarkChat(authToken: String, body: JsonObject) =
        apiService.bookmarkChat(authToken, body)

    suspend fun leaveGroup(authToken: String, body: JsonObject) =
        apiService.leaveGroup(authToken, body)

    suspend fun getAllTrolling(
        authToken: String,
        limit: Int,
        page: Int,
        searchText: String,
    ) =
        apiService.getAllTrolling(
            authToken,
            limit,
            page,
            searchText
        )

    suspend fun getAllTrolling(
        authToken: String,
        limit: Int,
        page: Int,
        searchText: String,
        type: Int
    ) =
        apiService.getAllTrolling(
            authToken,
            limit,
            page,
            searchText,
            type
        )

    suspend fun getTrollingById(
        authToken: String,
        id: Int
    ) =
        apiService.getTrollingById(
            authToken,
            id
        )

    suspend fun getAllRoute(
        authToken: String,
        limit: Int,
        page: Int,
        searchText: String,
    ) =
        apiService.getAllRoute(
            authToken,
            limit,
            page,
            searchText
        )

    suspend fun getAllRoute(
        authToken: String,
        limit: Int,
        page: Int,
        searchText: String,
        type: Int
    ) =
        apiService.getAllRoute(
            authToken,
            limit,
            page,
            searchText,
            type
        )

    suspend fun sharePointInGroup(
        authToken: String,
        body: JsonObject
    ) =
        apiService.sharePointInGroup(
            authToken,
            body
        )

    suspend fun deleteRouteById(
        authToken: String,
        body: JsonObject
    ) =
        apiService.deleteRouteById(
            authToken,
            body
        )

    suspend fun deleteTrollingById(
        authToken: String,
        body: JsonObject
    ) =
        apiService.deleteTrollingById(
            authToken,
            body
        )

    suspend fun updateTrolling(
        authToken: String,
        body: JsonObject
    ) =
        apiService.updateTrolling(
            authToken,
            body
        )

    suspend fun updateRoute(
        authToken: String,
        body: JsonObject
    ) =
        apiService.updateRoute(
            authToken,
            body
        )

    suspend fun updateRoute(
        authToken: String,
        id: RequestBody?,
        name: RequestBody?,
        description: RequestBody,
        profilePic: MultipartBody.Part?,
    ) =
        apiService.updateRoute(
            authToken,
            id,
            name,
            description,
            profilePic
        )

    suspend fun getSingleRouteById(
        authToken: String,
        id: Int
    ) =
        apiService.getSingleRouteById(
            authToken,
            id
        )

    suspend fun subscribePackage(
        authToken: String,
        body: JsonObject
    ) =
        apiService.subscribePackage(
            authToken,
            body
        )

    suspend fun getSubscribedPackage(
        authToken: String
    ) =
        apiService.getSubscribedPackage(
            authToken
        )

    suspend fun startFreeTrial(
        authToken: String
    ) =
        apiService.startFreeTrial(
            authToken
        )

    suspend fun rejectRequest(
        authToken: String,
        body: JsonObject
    ) =
        apiService.rejectRequest(
            authToken,
            body
        )

    suspend fun acceptRequest(
        authToken: String,
        body: JsonObject
    ) =
        apiService.acceptRequest(
            authToken,
            body
        )

    suspend fun getRoomLeavingReason(
        authToken: String
    ) =
        apiService.getRoomLeavingReason(
            authToken
        )
}