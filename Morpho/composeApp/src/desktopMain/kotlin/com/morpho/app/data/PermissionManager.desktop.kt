package com.morpho.app.data

import androidx.compose.runtime.Composable

// PermissionsManager.kt
actual class PermissionsManager actual constructor(callback: PermissionCallback) :
    PermissionHandler {



    @Composable
    override fun askPermission(permission: PermissionType) {
        println("Permission asked, not needed on desktop")
    }

    @Composable
    override fun isPermissionGranted(permission: PermissionType): Boolean {
        println("Permission granted, not needed on desktop")
        return true
    }

    @Composable
    override fun launchSettings() {
        println("Settings launched, not needed on desktop")
    }
}

@Composable
actual fun createPermissionsManager(callback: PermissionCallback): PermissionsManager {
    return PermissionsManager(callback)
}