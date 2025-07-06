package com.cp.fishthebreak.utils

import android.os.Parcelable
import com.cp.fishthebreak.models.home.SearchData
import com.cp.fishthebreak.models.points.SaveFishLogData
import com.cp.fishthebreak.models.points.SavePointsData
import com.cp.fishthebreak.models.routes.SaveRouteData
import com.cp.fishthebreak.models.trolling.TrollingResponseData
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

sealed class MapUiData: Parcelable {
    @Parcelize
    class LocationData(val data: SavePointsData): MapUiData()
    @Parcelize
    class FishLogData(val data: SaveFishLogData): MapUiData()
    @Parcelize
    class TrollingData(val data: TrollingResponseData): MapUiData()
    @Parcelize
    class RouteData(val data: SaveRouteData): MapUiData()
    @Parcelize
    class FromMessage(val pointId: Int, val type: String, val lat:Double?, val lang:Double?): MapUiData()
    @Parcelize
    class OfflineMap(val mapPath: String?): MapUiData()

    @Parcelize
    class BoatRange(val showBoatRangeControls: Boolean, val saveBoatRangeToServer: Boolean, val latitude:Double?, val longitude: Double?): MapUiData()
    @Parcelize
    class GetLocationFromMap(val latitude:Double?, val longitude: Double?, val pointsList: ArrayList<SearchData>): MapUiData()
}
