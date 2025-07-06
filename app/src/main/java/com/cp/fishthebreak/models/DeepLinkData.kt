package com.cp.fishthebreak.models

import android.os.Parcel
import android.os.Parcelable

data class DeepLinkData(val id: Int, val type: String): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()?:""
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DeepLinkData> {
        override fun createFromParcel(parcel: Parcel): DeepLinkData {
            return DeepLinkData(parcel)
        }

        override fun newArray(size: Int): Array<DeepLinkData?> {
            return arrayOfNulls(size)
        }
    }
}
