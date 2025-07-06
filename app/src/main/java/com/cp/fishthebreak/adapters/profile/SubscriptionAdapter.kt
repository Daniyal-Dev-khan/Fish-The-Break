package com.cp.fishthebreak.adapters.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.ProductDetails
import com.cp.fishthebreak.R
import com.cp.fishthebreak.databinding.ItemSubscriptionsBinding
import com.cp.fishthebreak.models.profile.InAppSubscriptionModel
import com.cp.fishthebreak.utils.setOnSingleClickListener

class SubscriptionAdapter(
    private val list: ArrayList<InAppSubscriptionModel>,
    private val mListener: OnItemClickListener
) : RecyclerView.Adapter<SubscriptionAdapter.ViewHolder>() {
    private lateinit var mContext: Context

    inner class ViewHolder(
        private val binding: ItemSubscriptionsBinding,
        private val listener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(index: Int) {
            binding.tvName.text = if(list[index].product.name.lowercase() == "yearly"){"${list[index].product.name} (Save 42%)"}else{list[index].product.name}
            binding.tvDetails.text = "Pay ${list[index].product.name.lowercase()}, cancel any time"
            binding.tvPrice.text =
                list[index].product.subscriptionOfferDetails?.first()?.pricingPhases?.pricingPhaseList?.first()?.formattedPrice
            if (list[index].isSelected) {
                binding.layout.background =
                    ContextCompat.getDrawable(mContext, R.drawable.bg_subscription_select)
                binding.ivSelect.setImageDrawable(
                    ContextCompat.getDrawable(
                        mContext,
                        R.drawable.ic_select_subscription
                    )
                )
            } else {
                binding.layout.background =
                    ContextCompat.getDrawable(mContext, R.drawable.bg_subscription_unselect)
                binding.ivSelect.setImageDrawable(
                    ContextCompat.getDrawable(
                        mContext,
                        R.drawable.ic_unselect_subscription
                    )
                )
            }
            binding.root.setOnSingleClickListener {
                listener.onItemClick(index)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mContext = parent.context
        val binding: ItemSubscriptionsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_subscriptions,
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