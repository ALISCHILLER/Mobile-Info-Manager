package com.msa.mobileinfomanager

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import java.util.*

class MobileInfoManager(private val context: Context) {

    private val TAG = "MobileInfoManager"

    // وضعیت شبکه (Wi-Fi، Mobile Data)
    fun getNetworkInfo(): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        return when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi"
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Mobile Data"
            else -> "No Network"
        }
    }

    // اطلاعات Wi-Fi (SSID و قدرت سیگنال)
    fun getWifiInfo(): String {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        return if (wifiInfo != null) {
            "SSID: ${wifiInfo.ssid}, Signal Strength: ${wifiInfo.rssi}"
        } else {
            "No Wi-Fi connection"
        }
    }

    // اطلاعات باتری (درصد شارژ و وضعیت شارژ)
    fun getBatteryInfo(): String {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val isCharging = batteryManager.isCharging
        return "Battery Level: $batteryLevel%, Charging: ${if (isCharging) "Yes" else "No"}"
    }

    // اطلاعات دستگاه (مدل، سازنده، نسخه اندروید)
    @SuppressLint("MissingPermission", "HardwareIds")
    fun getDeviceInfo(): String {
        var serial: String? = null

        // درخواست مجوز
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            serial = Build.getSerial()
        } else {
            Log.e(TAG, "Permission not granted for reading serial number.")
            serial = "Not available"
        }

        return """
            Device Model: ${Build.MODEL}
            Manufacturer: ${Build.MANUFACTURER}
            Brand: ${Build.BRAND}
            Device Name: ${Build.DEVICE}
            Product: ${Build.PRODUCT}
            Android Version: ${Build.VERSION.RELEASE}
            SDK Version: ${Build.VERSION.SDK_INT}
            Hardware: ${Build.HARDWARE}
            Serial: $serial
        """.trimIndent()
    }

    // وضعیت حافظه (حافظه آزاد و کل حافظه)
    fun getStorageInfo(): String {
        val externalStorage = Environment.getExternalStorageDirectory()
        val freeSpace = externalStorage.freeSpace / (1024 * 1024) // به مگابایت
        val totalSpace = externalStorage.totalSpace / (1024 * 1024) // به مگابایت
        return "Free Storage: ${freeSpace}MB, Total Storage: ${totalSpace}MB"
    }

    // اطلاعات تلفن همراه (IMEI، شماره تلفن، اپراتور، شبکه موبایل)
    @SuppressLint("MissingPermission", "HardwareIds")
    fun getTelephonyInfo(): String {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        var imei: String? = null
        var phoneNumber: String? = null
        val carrierName = telephonyManager.networkOperatorName

//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
//            imei = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                telephonyManager.imei
//            } else {
//                telephonyManager.deviceId
//            }
//            phoneNumber = telephonyManager.line1Number
//        } else {
//            Log.e(TAG, "Permission not granted for reading phone state.")
//        }

        val simOperator = telephonyManager.simOperator
        val simCountry = telephonyManager.simCountryIso
        val networkType = telephonyManager.networkType

        return """
            IMEI: ${imei ?: "Not available"}
            Phone Number: ${phoneNumber ?: "Not available"}
            Carrier: $carrierName
            SIM Operator: $simOperator
            SIM Country: $simCountry
            Network Type: $networkType
        """.trimIndent()
    }

    // Android ID (یک شناسه منحصر به فرد برای هر دستگاه)
    @SuppressLint("HardwareIds")
    fun getAndroidId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    // اطلاعات زبان و منطقه (Language و Country)
    fun getLocaleInfo(): String {
        val locale = Locale.getDefault()
        return "Language: ${locale.language}, Country: ${locale.country}"
    }

    // وضعیت حالت پرواز (روشن یا خاموش بودن)
    fun isAirplaneModeOn(): Boolean {
        return Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON,
            0
        ) != 0
    }

    // وضعیت GPS (روشن یا خاموش بودن مکان‌یابی)
    fun isLocationEnabled(): Boolean {
        val locationMode = Settings.Secure.getInt(
            context.contentResolver,
            Settings.Secure.LOCATION_MODE,
            Settings.Secure.LOCATION_MODE_OFF
        )
        return locationMode != Settings.Secure.LOCATION_MODE_OFF
    }


    fun deviceBrand(): String = try {
        val manufacturer = android.os.Build.MANUFACTURER
        val model = android.os.Build.MODEL
        model.replace("-", "_")
        if (model.startsWith(manufacturer))
            model.replaceFirstChar { it.uppercase() }
        else
            "Device Brand : ${manufacturer.replaceFirstChar { it.uppercase() }} ${model.replaceFirstChar { it.uppercase() }}"
    } catch (e: Exception) {
        "Device Brand : ${e.message}"
    }


    //---------------------------------------------------------------------------------------------- appVersionCode
    fun appVersionCode(): Long = try {
        context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode
    } catch (e: Exception) {
        0
    }
    //---------------------------------------------------------------------------------------------- appVersionCode


    //---------------------------------------------------------------------------------------------- appVersionName
    fun appVersionName(): String = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (e: Exception) {
        "0.0.0"
    }
    //---------------------------------------------------------------------------------------------- appVersionName


    //---------------------------------------------------------------------------------------------- androidVersion
    fun androidVersion(): String = try {
        val release = android.os.Build.VERSION.RELEASE
        val sdkVersion = android.os.Build.VERSION.SDK_INT
        "Android Version is $release & SDK Version is $sdkVersion"
    } catch (e: Exception) {
        "Android Version : ${e.message}"
    }

//    Serial Number: ${
//        if (ActivityCompat.checkSelfPermission(
//                context,
//                Manifest.permission.READ_PHONE_STATE
//            ) == PackageManager.PERMISSION_GRANTED
//        )
//            Build.getSerial()
//        else "Not available"
//    }
    // اطلاعات امنیتی سطح بالا: کد IMEI، Android ID، Serial و غیره
    fun getSecurityInfo(): String {
        return """
            IMEI: ${getTelephonyInfo()}
            Android ID: ${getAndroidId()}
 
            Hardware: ${Build.HARDWARE}
        """.trimIndent()
    }

    // نمایش تمام اطلاعات امنیتی و دستگاه به صورت جامع
    fun getAllInfo(): String {
        Log.e(TAG, "getNetworkInfo: ${getNetworkInfo()}")
        Log.e(TAG, "getWifiInfo: ${getWifiInfo()}")
        Log.e(TAG, "getBatteryInfo: ${getBatteryInfo()}")
        //   Log.e(TAG, "getDeviceInfo: ${getDeviceInfo()}")
        Log.e(TAG, "getStorageInfo: ${getStorageInfo()}")
        Log.e(TAG, "getTelephonyInfo: ${getTelephonyInfo()}")
        Log.e(TAG, "getAndroidId: ${getAndroidId()}")
        Log.e(TAG, "getLocaleInfo: ${getLocaleInfo()}")
        Log.e(TAG, "isAirplaneModeOn: ${isAirplaneModeOn()}")
        Log.e(TAG, "isLocationEnabled: ${isLocationEnabled()}")
        Log.e(TAG, "getSecurityInfo: ${getSecurityInfo()}")
        Log.e(ContentValues.TAG, "deviceBrand: ${deviceBrand()}", )
        Log.e(ContentValues.TAG, "appVersionCode: ${appVersionCode()}", )
        Log.e(ContentValues.TAG, "appVersionName: ${appVersionName()}", )
        return """
            Network: ${getNetworkInfo()}
            Wi-Fi: ${getWifiInfo()}
            Battery: ${getBatteryInfo()}
            Storage: ${getStorageInfo()}
            Telephony: ${getTelephonyInfo()}
            Android ID: ${getAndroidId()}
            Locale: ${getLocaleInfo()}
            Airplane Mode: ${if (isAirplaneModeOn()) "On" else "Off"}
            GPS: ${if (isLocationEnabled()) "Enabled" else "Disabled"}
            Security Info: ${getSecurityInfo()}
        """.trimIndent()
    }
}
