package com.cp.fishthebreak.utils

import android.os.Parcelable
import com.cp.fishthebreak.models.group.ChatListData
import kotlinx.parcelize.Parcelize

sealed class StartDestination : Parcelable {
    @Parcelize
    class ApplyLayers() : StartDestination()

    @Parcelize
    class ReadMoreAboutLayer(val link: String) : StartDestination()

    @Parcelize
    class SearchLocations(val isFromRoute: Boolean) : StartDestination()

    @Parcelize
    class EditPrifile() : StartDestination()

    @Parcelize
    class UpdatePassword() : StartDestination()

    @Parcelize
    class UpdateEmail() : StartDestination()

    @Parcelize
    class UpdatetvPreferences() : StartDestination()

    @Parcelize
    class SaveOfflineMap() : StartDestination()

    @Parcelize
    class ResoursesData() : StartDestination()

    @Parcelize
    class VesselsData() : StartDestination()

    @Parcelize
    class Subscription() : StartDestination()

    @Parcelize
    class ActiveSubscription() : StartDestination()

    @Parcelize
    class OpenGroup(val data: ChatListData) : StartDestination()

    @Parcelize
    class CommonMap(val data: MapUiData) : StartDestination()

    @Parcelize
    class RejectGroupReason() : StartDestination()
}
