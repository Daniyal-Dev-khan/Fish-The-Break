package com.cp.fishthebreak.utils

class Constants {
    companion object {
        const val MAP_PADDING = 100.0
        const val WMS_TYPE = "WMS"
        const val Self_Hosted_Type = "Self Hosted"
        const val Feature_Type = "Feature"
        const val TILE = "Tile"
        const val MESSAGE_DISPLAY_TIME = 2000L
        const val MESSAGE_PAGINATION_LOAD = 10
        const val DEFAULT_LAYER_OPACITY = 50F
        const val SPECIAL_REFERENCE_4326 = 4326 //4326 is for Geographic coordinate systems (GCSs)
        const val SPECIAL_REFERENCE_3857 = 3857 //Web Mercator Auxiliary Sphere (WKID=3857).
//https://developers.arcgis.com/kotlin/spatial-and-data-analysis/spatial-references/
        //        const val MAP_SCALE = 99999999.0
//        const val MAP_SCALE = 3e9
        const val MAP_SCALE = 3333333.0
        const val CURRENT_LOCATION_SCALE = 10000.0
        const val MAP_SCALE_Zoom = 1000.0
        const val OFFLINE_MAP_SCALE_Zoom = 1000.0
        const val SEARCH_MAP_SCALE_Zoom = 500.0

        //Live Urls
        const val DEEP_LINK_URL = ""
        const val SERVER_URL = ""
        const val BASE_URL = "${SERVER_URL}api/"
        const val SOCKET_URL = ""
        const val IMG_URL = "${SERVER_URL}assets/images/users/"
        const val WEB_URL = SERVER_URL
        const val CONTACT_US = "${SERVER_URL}contact-us"
        const val PrivacyPolicyUrl = "${SERVER_URL}api/get-terms/1"
        const val TermsAndConditionsUrl = "${SERVER_URL}api/get-terms/0"

        const val START_DESTINATION = "startDestination"

        const val channelName = "app_notifications"

        const val FISH_LOG_TYPE = "fish_log"
        const val LOCATION_TYPE = "location"
        const val TROLLING_TYPE = "trolling"
        const val ROUTE_TYPE = "route"
        const val FREE_TRAIL = "free_trail"
        const val SUBSCRIPTION = "subscription"

        const val ARCGIS_CLIENT_ID = ""
        const val ARCGIS_CLIENT_SECRET = ""
        const val ARCGIS_REDIRECT_URI = ""
        const val ARCGIS_OAUTH_URL = ""
    }

}