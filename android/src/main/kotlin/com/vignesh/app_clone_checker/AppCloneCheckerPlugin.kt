package com.vignesh.app_clone_checker

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result


/** AppCloneCheckerPlugin */
class AppCloneCheckerPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private val dualAppId999 = "999"
    private val dot = '.'
    private var myActivity: Activity? = null


    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "app_clone_checker")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {

            AppConstants.getPlatformVersion -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }

            AppConstants.checkDeviceCloned -> {

                val resultMap = mutableMapOf<String, String>()

                var isValidApp = true
                val applicationID = call.argument<String>(AppConstants.applicationID) ?: ""

                val workProfileAllowedFlag: Boolean =
                    call.argument<Boolean>(AppConstants.workProfileAllowedFlag) ?: true

                if (applicationID.isBlank() || applicationID.isEmpty()) {
                    resultMap[AppConstants.responseResultKey] = AppConstants.failureID
                    resultMap[AppConstants.responseCodeKey] = AppConstants.failureCodeAppIdMissing
                    resultMap[AppConstants.responseMessageKey] = AppConstants.failureMessageAppIdMissing
                    result.success(resultMap.toMap())
                    return
                }

                myActivity?.let {

                    val path: String = it.filesDir.path
                    //This will detect if app is accessed through Work Profile
                    val devicePolicyManager =
                        myActivity?.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                    val activeAdmins: List<ComponentName>? = devicePolicyManager.activeAdmins
                    val appPackageDotCount = applicationID.count { it == '.' }

                    // Detect system managers
                    val detectedManagers = mutableListOf<String>()
                    activeAdmins?.forEach { admin ->
                        detectedManagers.add(admin.packageName)
                    }
                    resultMap[AppConstants.responseSystemManagerKey] = detectedManagers.joinToString(", ")

                    if (getDotCount(path, appPackageDotCount)>appPackageDotCount) {
                        ///"Package Mismatch"
                        ///"Cloned App"
                        isValidApp = false
                        resultMap[AppConstants.responseCodeKey] = AppConstants.failureCodePackageMismatch
                        resultMap[AppConstants.responseMessageKey] = AppConstants.failureMessagePackageMismatch
                        Log.d("AppCloneCheckerPlugin","Package ID Mismatch")
                    } else if (path.contains(dualAppId999)) {
                        ///"Package Directory Mismatch"
                        ///"Cloned App"
                        isValidApp = false
                        resultMap[AppConstants.responseCodeKey] = AppConstants.failureCodeDualApp
                        resultMap[AppConstants.responseMessageKey] = AppConstants.failureMessageDualApp
                        Log.d("AppCloneCheckerPlugin","Dual App 999 Detected")
                    } else if (!workProfileAllowedFlag && activeAdmins != null && activeAdmins.isNotEmpty()) {
                        ///"Check for Work Profile or unauthorized MDM"

                        // Check if device has Samsung components
                        val samsungDevice = activeAdmins.any { admin ->
                            admin.packageName.contains("com.samsung")
                        }

                        if(samsungDevice){
                            // Samsung-specific check using Profile Owner
                            activeAdmins.forEach { admin ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    if (devicePolicyManager.isProfileOwnerApp(admin.packageName)) {
                                        isValidApp = false
                                        resultMap[AppConstants.responseCodeKey] = AppConstants.failureCodeWorkProfileSamsung
                                        resultMap[AppConstants.responseMessageKey] = AppConstants.failureMessageWorkProfileSamsung
                                        Log.d("AppCloneCheckerPlugin", "Samsung Work Profile/Secure Folder Detected: ${admin.packageName}")
                                    }
                                }
                            }
                        } else {
                            // Generic check: verify all admins are legitimate system managers
                            val hasNonLegitimateAdmin = activeAdmins.any { admin ->
                                val isLegitimate = AppConstants.legitimateSystemManagers.any { legitimate ->
                                    admin.packageName.startsWith(legitimate) || admin.packageName == legitimate
                                }
                                !isLegitimate
                            }

                            if (hasNonLegitimateAdmin) {
                                // Found admin that is NOT in our whitelist = enterprise MDM or unauthorized work profile
                                isValidApp = false
                                resultMap[AppConstants.responseCodeKey] = AppConstants.failureCodeWorkProfileGeneric
                                resultMap[AppConstants.responseMessageKey] = AppConstants.failureMessageWorkProfileGeneric
                                Log.d("AppCloneCheckerPlugin", "Non-Legitimate Work Profile Detected")
                            }
                        }
                    }

                }


                if (myActivity != null && isValidApp) {
                    resultMap[AppConstants.responseResultKey] = AppConstants.successID
                    resultMap[AppConstants.responseCodeKey] = AppConstants.successID
                    resultMap[AppConstants.responseMessageKey] = AppConstants.successMessage
                    // systemManager already set above
                    result.success(resultMap.toMap())
                } else if (myActivity == null) {
                    resultMap[AppConstants.responseResultKey] = AppConstants.failureID
                    resultMap[AppConstants.responseCodeKey] = AppConstants.failureCodeNoActivity
                    resultMap[AppConstants.responseMessageKey] = AppConstants.failureMessageNoActivity
                    resultMap[AppConstants.responseSystemManagerKey] = "N/A"
                    result.success(resultMap.toMap())
                } else {
                    resultMap[AppConstants.responseResultKey] = AppConstants.failureID
                    // code, message, and systemManager already set in specific checks above
                    // if not set (shouldn't happen), use generic failure
                    if (!resultMap.containsKey(AppConstants.responseCodeKey)) {
                        resultMap[AppConstants.responseCodeKey] = AppConstants.failureID
                        resultMap[AppConstants.responseMessageKey] = AppConstants.failureMessage
                    }
                    result.success(resultMap.toMap())
                }

            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private fun getDotCount(path: String, appPackageDotCount: Int): Int {
        var count = 0
        for (element in path) {
            if (count > appPackageDotCount) {
                break
            }
            if (element == dot) {
                count++
            }
        }
        return count
    }


    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        this.myActivity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        this.myActivity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        this.myActivity = binding.activity
    }

    override fun onDetachedFromActivity() {
        this.myActivity = null
    }
}
