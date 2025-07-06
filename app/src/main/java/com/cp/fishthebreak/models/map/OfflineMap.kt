package com.cp.fishthebreak.models.map

import androidx.annotation.DrawableRes
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.cp.fishthebreak.adapters.base.ListAdapterItem
import com.cp.fishthebreak.utils.toFormat
import java.io.Serializable
import java.util.Date

@Entity
data class OfflineMap(
    @PrimaryKey(autoGenerate = true) override var id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "image") var image: String?,
    @ColumnInfo(name = "map_path") var mapPath: String?,
    @ColumnInfo(name = "date") val date: Long?
): ListAdapterItem, Serializable {
    @Ignore
    var isSelected: Boolean = false
    @Ignore
    @DrawableRes
    var placeholder: Int? = null
    constructor(name: String, description: String, image: String, mapPath: String, date: Long) : this(
        id = 0,
        name,
        description,
        image,
        mapPath,
        date
    )

    fun getDateFormat(): String{
        if(date == null){
            return ""
        }
        return Date(date).toFormat("MMM dd, yyyy kk:mm")
    }
}
