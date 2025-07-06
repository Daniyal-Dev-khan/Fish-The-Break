package com.cp.fishthebreak.utils

interface SelectImageListener {
    fun onImageSelect(path: String?)
    fun onImageCancel()
}
interface PermissionListener {
    fun onPermissionGranted()
    fun onPermissionCancel()
}