package com.callumrodgers.opendosecontroller

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import androidx.core.content.PermissionChecker
import java.security.Permissions

class AppPermissionManager(val context: Context) {

    fun hasRequiredPermissions(): Boolean {
        val hasWifi = context.checkSelfPermission(android.Manifest.permission.ACCESS_WIFI_STATE)
        val hasNetwork = context.checkSelfPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)

        return (hasWifi + hasNetwork) == PackageManager.PERMISSION_GRANTED;
    }
}