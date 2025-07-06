package com.cp.fishthebreak.adapters.group

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.ItemMessageLeftBinding
import com.cp.fishthebreak.databinding.ItemMessageLeftTrollingBinding
import com.cp.fishthebreak.databinding.ItemMessageRightBinding
import com.cp.fishthebreak.databinding.ItemMessageRightTrollingBinding
import com.cp.fishthebreak.models.group.Message
import com.cp.fishthebreak.utils.updateCapitalizedTextByRemovingUnderscore
import com.cp.fishthebreak.utils.loadImage
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.cp.fishthebreak.utils.timeToAmPm
import com.cp.fishthebreak.utils.viewGone
import com.cp.fishthebreak.utils.viewVisible

const val ITEM_RIGHT = 0
const val ITEM_LEFT = 1
const val ITEM_RIGHT_TROLLING = 2
const val ITEM_LEFT_TROLLING = 3
class MessageAdapter(private val list: ArrayList<Message>, private val authId: Int, private val mListener: OnItemClickListener): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var mContext: Context
    inner class ViewHolderRight(private val binding: ItemMessageRightBinding, private val listener: OnItemClickListener):RecyclerView.ViewHolder(binding.root) {
        fun onBind(index: Int){
            val item = list[index]
            if(index == 0){
                binding.dateLayout.viewVisible()
                binding.tvDate.text = item.sent_date
            }else {
                if(list[index-1].sent_date != item.sent_date){
                    binding.dateLayout.viewVisible()
                    binding.tvDate.text = item.sent_date
                }else{
                    binding.dateLayout.viewGone()
                }
            }
            binding.tvType.text = item.shareable_type.updateCapitalizedTextByRemovingUnderscore()
            binding.iv.loadImage(item.base_url+item.image, R.drawable.place_holder_square, R.drawable.place_holder_square)
            binding.ivProfile.loadImage(item.user_base_url+item.sender_photo, R.drawable.place_holder_square, R.drawable.place_holder_square)
            binding.tvName.text = item.label
            binding.tvDescription.text = item.description?:""
            binding.tvUserName.text = item.sender_name
            binding.tvTime.text = item.sent_at.timeToAmPm("yyyy-MM-dd'T'HH:mm:ss.SSS")
            if(item.bookmarked) {
                binding.ivBookMark.setImageDrawable(
                    ContextCompat.getDrawable(
                        mContext,
                        R.drawable.ic_book_mark_filled
                    )
                )
            }else{
                binding.ivBookMark.setImageDrawable(
                    ContextCompat.getDrawable(
                        mContext,
                        R.drawable.ic_book_mark_gray
                    )
                )
            }
            binding.ivBookMark.setOnSingleClickListener {
                listener.onBookMark(item)
            }
            binding.layout.setOnSingleClickListener {
                listener.onChatClick(item)
            }
        }
    }

    inner class ViewHolderLeft(private val binding: ItemMessageLeftBinding, private val listener: OnItemClickListener):RecyclerView.ViewHolder(binding.root) {
        fun onBind(index: Int){
            val item = list[index]
            if(index == 0){
                binding.dateLayout.viewVisible()
                binding.tvDate.text = item.sent_date
            }else {
                if(list[index-1].sent_date != item.sent_date){
                    binding.dateLayout.viewVisible()
                    binding.tvDate.text = item.sent_date
                }else{
                    binding.dateLayout.viewGone()
                }
            }
            binding.tvType.text = item.shareable_type.updateCapitalizedTextByRemovingUnderscore()
            binding.iv.loadImage(item.base_url+item.image, R.drawable.place_holder_square, R.drawable.place_holder_square)
            binding.ivProfile.loadImage(item.user_base_url+item.sender_photo, R.drawable.place_holder_square, R.drawable.place_holder_square)
            binding.tvName.text = item.label
            binding.tvDescription.text = item.description?:""
            binding.tvUserName.text = item.sender_name
            binding.tvTime.text = item.sent_at.timeToAmPm("yyyy-MM-dd'T'HH:mm:ss.SSS")
            if(item.bookmarked) {
                binding.ivBookMark.setImageDrawable(
                    ContextCompat.getDrawable(
                        mContext,
                        R.drawable.ic_book_mark_filled
                    )
                )
            }else{
                binding.ivBookMark.setImageDrawable(
                    ContextCompat.getDrawable(
                        mContext,
                        R.drawable.ic_book_mark_gray
                    )
                )
            }
            binding.ivBookMark.setOnSingleClickListener {
                listener.onBookMark(item)
            }
            binding.layout.setOnSingleClickListener {
                listener.onChatClick(item)
            }
        }
    }

    inner class ViewHolderRightTrolling(private val binding: ItemMessageRightTrollingBinding, private val listener: OnItemClickListener):RecyclerView.ViewHolder(binding.root) {
        fun onBind(index: Int){
            val item = list[index]
            if(index == 0){
                binding.dateLayout.viewVisible()
                binding.tvDate.text = item.sent_date
            }else {
                if(list[index-1].sent_date != item.sent_date){
                    binding.dateLayout.viewVisible()
                    binding.tvDate.text = item.sent_date
                }else{
                    binding.dateLayout.viewGone()
                }
            }
            binding.tvType.text = item.shareable_type.updateCapitalizedTextByRemovingUnderscore()
            binding.ivProfile.loadImage(item.user_base_url+item.sender_photo, R.drawable.place_holder_square, R.drawable.place_holder_square)
            binding.tvName.text = item.label
            binding.tvUserName.text = item.sender_name
            binding.tvTime.text = item.sent_at.timeToAmPm("yyyy-MM-dd'T'HH:mm:ss.SSS")
            if(item.bookmarked) {
                binding.ivBookMark.setImageDrawable(
                    ContextCompat.getDrawable(
                        mContext,
                        R.drawable.ic_book_mark_filled
                    )
                )
            }else{
                binding.ivBookMark.setImageDrawable(
                    ContextCompat.getDrawable(
                        mContext,
                        R.drawable.ic_book_mark_gray
                    )
                )
            }
            binding.ivBookMark.setOnSingleClickListener {
                listener.onBookMark(item)
            }
            binding.layout.setOnSingleClickListener {
                listener.onChatClick(item)
            }
        }
    }

    inner class ViewHolderLeftTrolling(private val binding: ItemMessageLeftTrollingBinding, private val listener: OnItemClickListener):RecyclerView.ViewHolder(binding.root) {
        fun onBind(index: Int){
            val item = list[index]
            if(index == 0){
                binding.dateLayout.viewVisible()
                binding.tvDate.text = item.sent_date
            }else {
                if(list[index-1].sent_date != item.sent_date){
                    binding.dateLayout.viewVisible()
                    binding.tvDate.text = item.sent_date
                }else{
                    binding.dateLayout.viewGone()
                }
            }
            binding.tvType.text = item.shareable_type.updateCapitalizedTextByRemovingUnderscore()
            binding.ivProfile.loadImage(item.user_base_url+item.sender_photo, R.drawable.place_holder_square, R.drawable.place_holder_square)
            binding.tvName.text = item.label
            binding.tvUserName.text = item.sender_name
            binding.tvTime.text = item.sent_at.timeToAmPm("yyyy-MM-dd'T'HH:mm:ss.SSS")
            if(item.bookmarked) {
                binding.ivBookMark.setImageDrawable(
                    ContextCompat.getDrawable(
                        mContext,
                        R.drawable.ic_book_mark_filled
                    )
                )
            }else{
                binding.ivBookMark.setImageDrawable(
                    ContextCompat.getDrawable(
                        mContext,
                        R.drawable.ic_book_mark_gray
                    )
                )
            }
            binding.ivBookMark.setOnSingleClickListener {
                listener.onBookMark(item)
            }
            binding.layout.setOnSingleClickListener {
                listener.onChatClick(item)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mContext = parent.context
        when (viewType) {
            ITEM_RIGHT_TROLLING -> {
                val binding: ItemMessageRightTrollingBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_message_right_trolling,
                    parent,
                    false
                )
                return ViewHolderRightTrolling(binding,mListener)
            }
            ITEM_LEFT_TROLLING -> {
                val binding: ItemMessageLeftTrollingBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_message_left_trolling,
                    parent,
                    false
                )
                return ViewHolderLeftTrolling(binding,mListener)
            }
            ITEM_RIGHT -> {
                val binding: ItemMessageRightBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_message_right,
                    parent,
                    false
                )
                return ViewHolderRight(binding,mListener)
            }
            else -> {
                val binding: ItemMessageLeftBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_message_left,
                    parent,
                    false
                )
                return ViewHolderLeft(binding,mListener)
            }
        }
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is ViewHolderRight->{ holder.onBind(position) }
            is ViewHolderLeft->{ holder.onBind(position) }
            is ViewHolderRightTrolling->{ holder.onBind(position) }
            is ViewHolderLeftTrolling->{ holder.onBind(position) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(list[position].sender_id == authId && list[position].shareable_type == "trolling" ){
            ITEM_RIGHT_TROLLING
        }else if(list[position].sender_id != authId && list[position].shareable_type == "trolling" ){
            ITEM_LEFT_TROLLING
        }else if(list[position].sender_id == authId){
            ITEM_RIGHT
        }else{
            ITEM_LEFT
        }
    }

    interface OnItemClickListener{
        fun onBookMark(data: Message)
        fun onChatClick(data: Message)
    }
}