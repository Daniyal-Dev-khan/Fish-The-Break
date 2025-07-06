package com.cp.fishthebreak.adapters.profile

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.ItemActiveSubscriptionsBinding
import com.cp.fishthebreak.databinding.ItemSubscriptionsBinding
import com.cp.fishthebreak.models.profile.InAppSubscriptionModel
import com.cp.fishthebreak.models.profile.SubscribedModel
import com.cp.fishthebreak.utils.getProductNameFromProductId
import com.cp.fishthebreak.utils.getRemainingTrialDays
import com.cp.fishthebreak.utils.setOnSingleClickListener
import com.cp.fishthebreak.utils.toDate
import com.cp.fishthebreak.utils.toFormat
import org.json.JSONObject

class ActiveSubscriptionAdapter(
    private val list: ArrayList<SubscribedModel>,
    private val mListener: OnItemClickListener
) : RecyclerView.Adapter<ActiveSubscriptionAdapter.ViewHolder>() {
    private lateinit var mContext: Context

    inner class ViewHolder(
        private val binding: ItemActiveSubscriptionsBinding,
        private val listener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun onBind(index: Int) {
            binding.tvName.text = list[index].serverData?.product_name?:""
            if((list[index].serverData?.product_id?:"") == "free_trial"){
                binding.tvDetails.text =
                    "Enjoy Fish the Break free for ${list[index].serverData?.expiry_date?.toDate("yyyy-MM-dd")?.getRemainingTrialDays()?:"0"} days"
            }else {
                binding.tvDetails.text =
                    "Pay ${list[index].serverData?.product_name?.lowercase()}, cancel any time"
            }
//            binding.tvExpiryDate.text = "Expires: ${list[index].purchaseData.purchaseTime.toDate(JSONObject(list[index].purchaseData.originalJson).get("productId").toString()).toFormat("MMM dd, yyyy")}"
            binding.tvExpiryDate.text = "Expires: ${list[index].serverData?.expiry_date?.toDate("yyyy-MM-dd")?.toFormat("MMM dd, yyyy")}"
            binding.tvPrice.text = list[index].serverData?.product_price?:""
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mContext = parent.context
        val binding: ItemActiveSubscriptionsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_active_subscriptions,
            parent,
            false
        )
        return ViewHolder(binding, mListener)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(position)
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}