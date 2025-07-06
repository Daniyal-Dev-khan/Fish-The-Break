package com.cp.fishthebreak.utils

import android.os.Parcelable
import com.cp.fishthebreak.models.group.ChatListData
import com.cp.fishthebreak.models.home.SearchData
import com.cp.fishthebreak.models.map.MapLayer
import com.cp.fishthebreak.models.profile.ResourceData
import com.cp.fishthebreak.models.profile.ResourceDetails
import kotlinx.parcelize.Parcelize

sealed class NavigationDirections: Parcelable {
    @Parcelize
    class ResourceList(val data: ResourceData): NavigationDirections()
    @Parcelize
    class ResourceScreen(val data: ResourceDetails): NavigationDirections()
    @Parcelize
    class ReadMoreFilterScreen(val data: MapLayer): NavigationDirections()
    @Parcelize
    class LayerFilterScreen(val data: MapLayer): NavigationDirections()

    @Parcelize
    class OpenGroup(val data: ChatListData): NavigationDirections()

    @Parcelize
    class OpenGroupList(): NavigationDirections()
    @Parcelize
    class RejectReason(val data: ChatListData): NavigationDirections()

    @Parcelize
    class CommonMap(val data: MapUiData): NavigationDirections()
    @Parcelize
    class Share(val data: MapUiData): NavigationDirections()

    @Parcelize
    class ViewPoints(val data: MapUiData): NavigationDirections()

    @Parcelize
    class DeletePoints(val data: MapUiData): NavigationDirections()

    @Parcelize
    class GlobalSearch(val data: SearchData): NavigationDirections()
}

