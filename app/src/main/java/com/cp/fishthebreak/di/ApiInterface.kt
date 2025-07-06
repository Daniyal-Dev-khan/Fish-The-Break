package com.cp.fishthebreak.di

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
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {
    @POST("login")
    suspend fun login(
        @Body body: JsonObject
    ): Response<RegisterModel>

    @POST("social-login")
    suspend fun socialLogin(
        @Body body: JsonObject
    ): Response<RegisterModel>

    @POST("register")
    suspend fun signup(
        @Body body: JsonObject
    ): Response<RegisterModel>

    @POST("verify-otp")
    suspend fun verifyOtp(
        @Body body: JsonObject
    ): Response<RegisterModel>

    @POST("send-otp")
    suspend fun sendOtp(
        @Body body: JsonObject
    ): Response<SendCodeModel>

    @POST("verify-otp-reset")
    suspend fun verifyPasswordResetOtp(
        @Body body: JsonObject
    ): Response<VerifyPasswordOtpModel>

    @POST("reset-password")
    suspend fun resetPassword(
        @Body body: JsonObject
    ): Response<ResetPasswordModel>

    @GET("layers/get-all")
    suspend fun getAllLayers(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int,
    ): Response<GetAllLayerModel>

    @POST("get-search")
    suspend fun getSearchHistory(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<GetSearchModel>

    @GET("user/get-self-profile")
    suspend fun getProfile(
        @Header("Authorization") token: String,
    ): Response<RegisterModel>

    @POST("save-search")
    suspend fun saveSearch(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<SaveSearchModel>

    @POST("user/update-self-profile")
    suspend fun editProfile(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<RegisterModel>

    @Multipart
    @Headers("Accept: Application/json")
    @POST("user/update-self-profile")
    suspend fun editProfile(
        @Header("Authorization") token: String?,
        @Part("first_name") firstName: RequestBody?,
        @Part("last_name") lastName: RequestBody?,
        @Part("username") username: RequestBody?,
        @Part("mobile") mobile: RequestBody?,
        @Part profilePic: MultipartBody.Part?
    ): Response<RegisterModel>

    @Multipart
    @Headers("Accept: Application/json")
    @POST("user/update-self-profile")
    suspend fun editProfile(
        @Header("Authorization") token: String?,
        @Part("first_name") firstName: RequestBody?,
        @Part("last_name") lastName: RequestBody?,
        @Part("mobile") mobile: RequestBody?,
        @Part profilePic: MultipartBody.Part?
    ): Response<RegisterModel>

    @Multipart
    @Headers("Accept: Application/json")
    @POST("user/update-self-profile")
    suspend fun editProfile(
        @Header("Authorization") token: String?,
        @Part profilePic: MultipartBody.Part?
    ): Response<RegisterModel>

    @POST("user/change-password")
    suspend fun updatePassword(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<ChangePasswordModel>

    @POST("user/change-email")
    suspend fun updateEmail(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<ChangeEmailModel>

    @POST("user/update-email")
    suspend fun verifyEmailOtp(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<RegisterModel>

    @POST("user/boat-range")
    suspend fun boatRange(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<RegisterModel>

    @POST("user/toggle-vessel")
    suspend fun toggleVessel(
        @Header("Authorization") token: String,
    ): Response<RegisterModel>
    @GET("resources/get-all")
    suspend fun getAllResources(
        @Header("Authorization") token: String,
    ): Response<AllResourcesModel>

    @GET("resources/get-by-id")
    suspend fun getResourceListById(
        @Header("Authorization") token: String,
        @Query("id") id: Int,
    ): Response<ResourceListModel>

    @POST("logout")
    suspend fun logout(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<LogoutModel>

    @POST("user/account-delete")
    suspend fun deleteAccount(
        @Header("Authorization") token: String,
    ): Response<DeleteAccountModel>

    @POST("points/delete")
    suspend fun deletePointsById(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<SavePointsModel>

    @GET("points/get-all")
    suspend fun getSavedPoints(
        @Header("Authorization") token: String,
        @Query("search_text") searchText: String,
        @Query("limit") limit: Int,
        @Query("page") page: Int,
        @Query("trolling_id") trollingId: Int?,
    ): Response<GetSavedPointsModel>

    @GET("points/get-all")
    suspend fun getSavedPoints(
        @Header("Authorization") token: String,
        @Query("search_text") searchText: String,
        @Query("limit") limit: Int,
        @Query("page") page: Int,
        @Query("type") type: Int,
        @Query("trolling_id") trollingId: Int?,
    ): Response<GetSavedPointsModel>

    @Multipart
    @Headers("Accept: Application/json")
    @POST("points/save")
    suspend fun savePoints(
        @Header("Authorization") token: String?,
        @Part("point_name") pointName: RequestBody?,
        @Part("latitude") lat: RequestBody?,
        @Part("longitude") lang: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("date") date: RequestBody?,
        @Part("time") time: RequestBody?,
        @Part("local_db_id") local_db_id: RequestBody?,
        @Part("local_db_unique_id") local_db_unique_id: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Response<SavePointsModel>

    @POST("points/save")
    suspend fun savePoints(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<SavePointsModel>

    @POST("points/update")
    suspend fun updatePoints(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<SavePointsModel>

    @Multipart
    @Headers("Accept: Application/json")
    @POST("points/update")
    suspend fun updatePoints(
        @Header("Authorization") token: String?,
        @Part("id") id: RequestBody?,
        @Part("point_name") pointName: RequestBody?,
        @Part("latitude") lat: RequestBody?,
        @Part("longitude") lang: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Response<SavePointsModel>

    @Headers("Accept: Application/json")
    @GET("points/get-by-id")
    suspend fun getPointById(
        @Header("Authorization") token: String?,
        @Query("id") id: Int
    ): Response<SavePointsModel>

    @POST("fish-log/save")
    suspend fun saveFishLog(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<SaveFishLogModel>

    @Multipart
    @Headers("Accept: Application/json")
    @POST("fish-log/save")
    suspend fun saveFishLog(
        @Header("Authorization") token: String?,
        @Part("fish_name") pointName: RequestBody?,
        @Part("latitude") lat: RequestBody?,
        @Part("longitude") lang: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("length") length: RequestBody,
        @Part("weight") weight: RequestBody,
        @Part("date") date: RequestBody,
        @Part("time") time: RequestBody,
        @Part("local_db_id") local_db_id: RequestBody,
        @Part("local_db_unique_id") local_db_unique_id: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<SaveFishLogModel>

    @POST("fish-log/update")
    suspend fun updateFishLog(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<SaveFishLogModel>


    @Multipart
    @Headers("Accept: Application/json")
    @POST("fish-log/update")
    suspend fun updateFishLog(
        @Header("Authorization") token: String?,
        @Part("id") id: RequestBody?,
        @Part("fish_name") pointName: RequestBody?,
        @Part("latitude") lat: RequestBody?,
        @Part("longitude") lang: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("length") length: RequestBody,
        @Part("weight") weight: RequestBody,
        @Part("date") date: RequestBody,
        @Part("time") time: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<SaveFishLogModel>

    @POST("fish-log/delete")
    suspend fun deleteFishLogById(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<SaveFishLogModel>

    @GET("fish-log/get-all")
    suspend fun getSavedFishLogs(
        @Header("Authorization") token: String,
        @Query("search_text") searchText: String,
        @Query("limit") limit: Int,
        @Query("page") page: Int,
        @Query("trolling_id") trollingId: Int?,
    ): Response<GetFishLogsModel>

    @GET("fish-log/get-all")
    suspend fun getSavedFishLogs(
        @Header("Authorization") token: String,
        @Query("search_text") searchText: String,
        @Query("limit") limit: Int,
        @Query("page") page: Int,
        @Query("type") type: Int,
        @Query("trolling_id") trollingId: Int?,
    ): Response<GetFishLogsModel>

    @Headers("Accept: Application/json")
    @GET("fish-log/get-by-id")
    suspend fun getFishLogById(
        @Header("Authorization") token: String?,
        @Query("id") id: Int
    ): Response<SaveFishLogModel>

    @Headers("Accept: Application/json")
    @POST("trolling/save")
    suspend fun saveTrolling(
        @Header("Authorization") token: String?,
        @Body body: JsonObject
    ): Response<TrollingResponseModel>
    @Headers("Accept: Application/json")
    @POST("trolling/save-points")
    suspend fun saveTrollingPoints(
        @Header("Authorization") token: String?,
        @Body body: JsonObject
    ): Response<TrollingResponseModel>

    @POST("vessel/save")
    suspend fun saveVessel(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<SaveVesselModel>

    @POST("vessel/update")
    suspend fun updateVessel(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<SaveVesselModel>

    @GET("vessel/get-all")
    suspend fun getAllVessel(
        @Header("Authorization") token: String
    ): Response<GetVesselModel>

    @Multipart
    @Headers("Accept: Application/json")
    @POST("route/save")
    suspend fun saveRoute(
        @Header("Authorization") token: String?,
        @Part("name") pointName: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("date") date: RequestBody,
        @Part("time") time: RequestBody,
        @Part("locations") locations: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<SaveRouteModel>

    @POST("route/save")
    suspend fun saveRoute(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<SaveRouteModel>

    @GET("group/list")
    suspend fun getGroupList(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int,
        @Query("page") page: Int,
    ): Response<ChatListModel>

    @GET("chat/list")
    suspend fun getGroupChat(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int,
        @Query("page") page: Int,
        @Query("room_id") roomId: Int,
    ): Response<GroupDetailsModel>

    @POST("group/create")
    suspend fun createGroup(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<CreateGroupModel>

    @POST("group/add-member")
    suspend fun addMembersInGroup(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<AddMembersModel>

    @Multipart
    @Headers("Accept: Application/json")
    @POST("group/create")
    suspend fun createGroup(
        @Header("Authorization") token: String?,
        @Part("name") pointName: RequestBody?,
        @Part("user_id") userIds: RequestBody?,
        @Part profilePic: MultipartBody.Part?
    ): Response<CreateGroupModel>

    @POST("group/edit-detail")
    suspend fun updateGroup(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<UpdateGroupModel>

    @Multipart
    @Headers("Accept: Application/json")
    @POST("group/edit-detail")
    suspend fun updateGroup(
        @Header("Authorization") token: String?,
        @Part("name") pointName: RequestBody?,
        @Part("id") userIds: RequestBody?,
        @Part profilePic: MultipartBody.Part?
    ): Response<UpdateGroupModel>

    @GET("user/find")
    suspend fun findUsers(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int,
        @Query("page") page: Int,
        @Query("search_text") searchText: String,
    ): Response<FindUsersModel>

    @GET("user/find")
    suspend fun findUsers(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int,
        @Query("page") page: Int,
        @Query("search_text") searchText: String,
        @Query("room_id") roomId: Int,
    ): Response<FindUsersModel>

    @GET("group/member")
    suspend fun getGroupUsers(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int,
        @Query("page") page: Int,
        @Query("room_id") roomId: Int,
    ): Response<GroupMemberModel>

    @POST("chat/bookmark")
    suspend fun bookmarkChat(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<BookMarkModel>

    @POST("group/leave")
    suspend fun leaveGroup(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<LeaveGroupModel>

    @GET("trolling/get-all")
    suspend fun getAllTrolling(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int,
        @Query("page") page: Int,
        @Query("search_text") searchText: String,
    ): Response<GetAllTrollingModel>

    @GET("trolling/get-all")
    suspend fun getAllTrolling(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int,
        @Query("page") page: Int,
        @Query("search_text") searchText: String,
        @Query("type") type: Int,
    ): Response<GetAllTrollingModel>

    @GET("trolling/get-by-id")
    suspend fun getTrollingById(
        @Header("Authorization") token: String,
        @Query("id") id: Int,
    ): Response<TrollingResponseModel>

    @GET("route/get-all")
    suspend fun getAllRoute(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int,
        @Query("page") page: Int,
        @Query("search_text") searchText: String,
    ): Response<GetAllRouteModel>

    @GET("route/get-all")
    suspend fun getAllRoute(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int,
        @Query("page") page: Int,
        @Query("search_text") searchText: String,
        @Query("type") type: Int,
    ): Response<GetAllRouteModel>

    @POST("chat/send")
    suspend fun sharePointInGroup(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<ShareInGroupModel>

    @POST("route/delete")
    suspend fun deleteRouteById(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<SaveRouteModel>

    @POST("trolling/delete")
    suspend fun deleteTrollingById(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<TrollingResponseModel>

    @POST("trolling/update")
    suspend fun updateTrolling(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<TrollingResponseModel>

    @POST("route/update")
    suspend fun updateRoute(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<SaveRouteModel>

    @Multipart
    @Headers("Accept: Application/json")
    @POST("route/update")
    suspend fun updateRoute(
        @Header("Authorization") token: String?,
        @Part("id") id: RequestBody?,
        @Part("name") pointName: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Response<SaveRouteModel>

    @GET("route/get-by-id")
    suspend fun getSingleRouteById(
        @Header("Authorization") token: String,
        @Query("id") id: Int,
    ): Response<SaveRouteModel>

    @POST("package/android/subscribe")
    suspend fun subscribePackage(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<AddSubscriptionModel>

    @GET("package/android/subscribe")
    suspend fun getSubscribedPackage(
        @Header("Authorization") token: String
    ): Response<GetSubscribedPackageModel>

    @POST("user/start-trial")
    suspend fun startFreeTrial(
        @Header("Authorization") token: String
    ): Response<StartTrialModel>

    @POST("group/accept-request")
    suspend fun acceptRequest(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<RequestAcceptModel>

    @POST("group/reject-request")
    suspend fun rejectRequest(
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Response<RequestRejectModel>

    @GET("group/room-leaving-reason")
    suspend fun getRoomLeavingReason(
        @Header("Authorization") token: String,
    ): Response<LeaveReasonModel>
}