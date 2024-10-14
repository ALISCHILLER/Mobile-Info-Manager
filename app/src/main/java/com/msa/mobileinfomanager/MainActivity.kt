package com.msa.mobileinfomanager

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.ContentValues.TAG
import android.util.Log
import com.msa.mobileinfomanager.ui.theme.MobileInfoManagerTheme

class MainActivity : ComponentActivity() {

    private lateinit var mobileInfoManager: MobileInfoManager
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mobileInfoManager = MobileInfoManager(this)

        // تنظیم launcher برای درخواست مجوزها
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var allGranted = true
            permissions.forEach { (permission, isGranted) ->
                if (!isGranted) {
                    allGranted = false
                    // نمایش پیام خطا در صورت رد مجوز
                    Toast.makeText(this, "Permission denied: $permission", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "permission: $permission")
                }
            }
            if (allGranted) {
                // نمایش اطلاعات دستگاه
                displayDeviceInfo()
            }
        }

        // درخواست مجوزها
        requestPermissions()
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.READ_PHONE_STATE
        )

        // برای اندروید 13 و بالاتر، مجوزهای رسانه‌ای را اضافه کنید
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
        }

        // بررسی اینکه آیا مجوزها داده شده‌اند
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            Log.i(TAG, "Requesting permissions: ${missingPermissions.joinToString(", ")}")
            permissionLauncher.launch(missingPermissions.toTypedArray())
        } else {
            // اگر هیچ مجوزی کمبود نداشته باشد، نمایش اطلاعات دستگاه
            displayDeviceInfo()
        }
    }

    private fun displayDeviceInfo() {
        setContent {
            MobileInfoManagerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DeviceInfoScreen(
                        deviceInfo = mobileInfoManager.getAllInfo(),
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceInfoScreen(deviceInfo: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = deviceInfo, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun DeviceInfoPreview() {
    MobileInfoManagerTheme {
        DeviceInfoScreen("Sample Device Info")
    }
}
