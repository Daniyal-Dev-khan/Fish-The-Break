package com.cp.fishthebreak.notifications

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import com.cp.fishthebreak.models.group.ChatListData
import com.cp.fishthebreak.utils.NavigationDirections

data class NotificationModel(
    var message: String = "",
    var title: String = "",
    var navigation: NavigationDirections? = null,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            parcel.readParcelable(
                NavigationDirections::class.java.classLoader,
                NavigationDirections::class.java
            )
        } else {
            parcel.readParcelable(NavigationDirections::class.java.classLoader)
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(message)
        parcel.writeString(title)
        parcel.writeParcelable(navigation, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NotificationModel> {
        override fun createFromParcel(parcel: Parcel): NotificationModel {
            return NotificationModel(parcel)
        }

        override fun newArray(size: Int): Array<NotificationModel?> {
            return arrayOfNulls(size)
        }
    }
}

data class ChatNotificationDataModel(
    val my_data: ChatListData?,
    val notification_type: String?
)

