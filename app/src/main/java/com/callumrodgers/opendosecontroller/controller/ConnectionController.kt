package com.callumrodgers.opendosecontroller.controller

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SignalStrengthUpdateRequest
import android.telephony.TelephonyManager
import com.callumrodgers.opendosecontroller.AppPermissionManager
import com.callumrodgers.opendosecontroller.API

class ConnectionController(val context: Context) {

    /**
     * Whether the user's device supports telephony services. If not, app behaviour regarding
     * mobile data will be disabled.
     */
    val isTelephonySupported: Boolean

    /**
     * Android API's manager for telephony services. It is null if, and only if, telephony
     * services are not supported in this device.
     */
    val telephonyManager: TelephonyManager?

    /**
     * This is set to true if the device is ongoing an active mobile data connection. Mobile data
     * being *enabled* does **not** necessarily mean it is *active*.
     */
    var isMobileDataActive: Boolean = false

    private val permissionManager: AppPermissionManager

    init {
        this.isTelephonySupported =
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)

        this.permissionManager = AppPermissionManager(context)

        if (isTelephonySupported) {
            telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            // Performing initial connection check.
            checkActiveConnections()
        } else {
            telephonyManager = null
        }

    }

    /**
     * Checks for the current active connections on the device.
     */
    @SuppressLint("MissingPermission") // We're not actually missing any permissions,
    // but Lint can't see that check since it's in another file.
    fun checkActiveConnections() {
        if (!isTelephonySupported) return

        if (permissionManager.hasRequiredPermissions()) {
            if (API.isAtLeast(31)) {
                this.isMobileDataActive = telephonyManager!!.isDataConnectionAllowed
            } else {
                this.isMobileDataActive = telephonyManager!!.isDataCapable && telephonyManager.isDataEnabled
                telephonyManager!!.set
            }
        }
    }


    fun setupSignalStrengthChecker() {
        if (!isTelephonySupported) return

        if (permissionManager.hasRequiredPermissions()) {
            val updateRequest = SignalStrengthUpdateRequest()
            telephonyManager!!.setSignalStrengthUpdateRequest()
        }
    }


}