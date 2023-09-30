package com.abhitues.peerwavee.utils

import android.net.wifi.p2p.WifiP2pDevice

object WifiP2pUtils {

    fun getDeviceStatus(deviceStatus: Int): String {
            return when (deviceStatus) {
                WifiP2pDevice.AVAILABLE -> "available"
                WifiP2pDevice.INVITED -> "Inviting"
                WifiP2pDevice.CONNECTED -> "connected"
                WifiP2pDevice.FAILED -> "failed"
                WifiP2pDevice.UNAVAILABLE -> "unavailable"
                else -> "unknown"
            }
        }

    }
