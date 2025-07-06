package com.cp.fishthebreak.models.group

import android.text.SpannableString
import androidx.core.content.ContextCompat
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.base.ListAdapterItem
import com.cp.fishthebreak.di.MyApplication
import com.cp.fishthebreak.utils.kAndMFormatter
import com.cp.fishthebreak.utils.spanTextColor
import com.cp.fishthebreak.utils.timeAgo
import java.io.Serializable

data class ChatListModel(
    val `data`: ArrayList<ChatListData>,
    val last_page: Int?,
    val message: String,
    val status: Boolean,
    val statusCode: Int
)

data class ChatListData(
    val base_url: String?,
    override val id: Int,
    val latest_message: String?,
    var members: Int?,
    var name: String?,
    var profile_pic: String?,
    val sender: String?,
    val sent_at: String?,
    val owner: Boolean,
    var status: Int,// 0 -> pending, 1-> accepted
    var isSelected: Boolean = false
): ListAdapterItem, Serializable{
    fun getLastMessageTime(): String{
        return sent_at?.timeAgo("yyyy-MM-dd'T'HH:mm:ss.SSS")?:""
    }
    fun getLastMessage(): SpannableString {
        val mContext = MyApplication.appContext
        return mContext.spanTextColor(sender?:"", ContextCompat.getColor(mContext, R.color.secondary700),R.font.urbanist_medium," "+(latest_message?:""), ContextCompat.getColor(mContext, R.color.secondary700),R.font.urbanist_regular)
        //return "${sender?:""} ${latest_message?:""}".trim()
    }

    fun getMembersFormat(): String{
        return if((members?:0) < 2){
            (members?.toString()?:"0") + " member"
        }else{
            kAndMFormatter(members?:0) + " members"
        }
    }

    fun isLastMessageExist(): Boolean{
        return !latest_message.isNullOrEmpty()
    }
}