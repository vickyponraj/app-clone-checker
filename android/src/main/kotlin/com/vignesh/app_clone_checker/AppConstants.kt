package com.vignesh.app_clone_checker

object AppConstants {

    const val getPlatformVersion = "getPlatformVersion"
    const val checkDeviceCloned = "checkDeviceCloned"
    const val applicationID = "applicationID"
    const val workProfileAllowedFlag = "isWorkProfileAllowed"
    const val successID = "Success"
    const val successMessage = "Valid App"
    const val failureID = "Failure"
    const val failureMessage = "Cloned / In-Valid App"
    const val failureAppIdMessage = "Application ID Not Passed"

    // Failure codes and messages
    const val failureCodeAppIdMissing = "ERROR_NO_APP_ID"
    const val failureCodePackageMismatch = "ERROR_PACKAGE_MISMATCH"
    const val failureCodeDualApp = "ERROR_DUAL_APP_999"
    const val failureCodeWorkProfileSamsung = "ERROR_WORK_PROFILE_SAMSUNG"
    const val failureCodeWorkProfileGeneric = "ERROR_WORK_PROFILE_DETECTED"
    const val failureCodeNoActivity = "ERROR_NO_ACTIVITY_CONTEXT"

    const val failureMessageAppIdMissing = "Application ID Not Passed"
    const val failureMessagePackageMismatch = "Package Path Mismatch - Possible Clone Detected"
    const val failureMessageDualApp = "Dual App Environment Detected (ID 999)"
    const val failureMessageWorkProfileSamsung = "Samsung Work Profile / Secure Folder Detected"
    const val failureMessageWorkProfileGeneric = "Work Profile with Non-GMS Policy Detected"
    const val failureMessageNoActivity = "No Activity Context Available"

    const val responseResultKey = "result"
    const val responseMessageKey = "message"
    const val responseCodeKey = "code"
    const val responseSystemManagerKey = "systemManager"

    // Legitimate system managers from OEMs
    val legitimateSystemManagers = listOf(
        "com.google.android.gms",           // Google Mobile Services
        "com.samsung.android.knox",         // Samsung Knox
        "com.samsung.klmsagent",            // Samsung KLM Agent
        "com.zte.mdm",                      // ZTE Mobile Device Management
        "com.zte.systemmanager",            // ZTE System Manager
        "com.zte.secureguard",              // ZTE Security
        "com.xiaomi.miui.securitycenter",   // Xiaomi MIUI Security
        "com.miui.securitycenter",          // Xiaomi MIUI Security (alt)
        "com.xiaomi.finddevice",            // Xiaomi Find Device
        "com.oppo.safe",                    // Oppo ColorOS Security
        "com.coloros.safecenter",           // Oppo ColorOS Safe Center
        "com.vivo.securitycore",            // Vivo Security
        "com.vivo.pure",                    // Vivo Pure Mode
        "com.huawei.systemmanager",         // Huawei System Manager
        "com.huawei.android.hwouc",         // Huawei OUC
        "com.oneplus.security",             // OnePlus Security
        "com.realme.securitycheck",         // Realme Security
        "com.motorola.devicemanagement"     // Motorola Device Management
    )


}