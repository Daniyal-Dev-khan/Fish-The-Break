package com.cp.fishthebreak.utils

import android.view.View
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.cp.fishthebreak.R
import com.cp.fishthebreak.adapters.base.BaseAdapter
import com.cp.fishthebreak.adapters.base.ListAdapterItem
import com.google.android.material.imageview.ShapeableImageView

@BindingAdapter("android:visibility")
fun View.bindVisibility(visible: Boolean?) {
    isVisible = visible == true
    //visibility = if (visible == true) View.VISIBLE else View.GONE
}
@BindingAdapter("android:background")
fun View.bindBackground(visible: Boolean?) {
    background = if (visible == true) ContextCompat.getDrawable(context,
        R.drawable.bg_alice_blue_rounded_error) else ContextCompat.getDrawable(context, R.drawable.bg_alice_blue_rounded)
    //visibility = if (visible == true) View.VISIBLE else View.GONE
}

@BindingAdapter("setAdapter")
fun setAdapter(
    recyclerView: RecyclerView,
    adapter: BaseAdapter<ViewDataBinding, ListAdapterItem>?
) {
    adapter?.let {
        recyclerView.adapter = it
    }
}

@Suppress("UNCHECKED_CAST")
@BindingAdapter("submitList")
fun submitList(recyclerView: RecyclerView, list: List<ListAdapterItem>?) {
    val adapter = recyclerView.adapter as BaseAdapter<ViewDataBinding, ListAdapterItem>?
    adapter?.updateData(list ?: listOf())
}

@BindingAdapter("setImage")
fun setImage(imageView: ShapeableImageView, url: String?) {
    imageView.loadImage(url, R.drawable.place_holder_square,R.drawable.place_holder_square)
}

@BindingAdapter("backgroundSelectionLayer")
fun backgroundSelectionLayer(view: ConstraintLayout, selected: Boolean?) {
    view.background = if (selected == true) ContextCompat.getDrawable(view.context,
        R.drawable.bg_layers_selected) else ContextCompat.getDrawable(view.context, R.drawable.bg_layers)
}

@BindingAdapter("setVisibility")
fun setVisibility(view: ConstraintLayout, data: Any?) {
    view.visibility = if (data != null) View.VISIBLE else View.GONE
}