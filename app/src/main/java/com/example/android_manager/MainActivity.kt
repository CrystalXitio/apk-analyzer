package com.example.android_manager

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.android_manager.navigation.AppNavigation
import com.example.android_manager.ui.theme.Android_ManagerTheme
import java.io.File

class MainActivity : ComponentActivity() {

    private var incomingApkUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install modern AndroidX splash screen before super.onCreate
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Phase 3.2: Clean up any stale scan artifacts in cache on startup
        cleanupTempScanCache()

        // Phase 3.3: Handle incoming APK files via VIEW or SEND intents
        handleIncomingApkIntent(intent)

        enableEdgeToEdge()

        setContent {
            Android_ManagerTheme {
                AppNavigation(initialApkUri = incomingApkUri)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingApkIntent(intent)
    }

    private fun handleIncomingApkIntent(intent: Intent?) {
        if (intent == null) return
        val uri = when (intent.action) {
            Intent.ACTION_VIEW -> intent.data
            Intent.ACTION_SEND -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_STREAM)
                }
            }
            else -> null
        }
        if (uri != null) {
            incomingApkUri = uri
        }
    }

    private fun cleanupTempScanCache() {
        try {
            val cacheFiles = cacheDir.listFiles { _, name ->
                name.startsWith("apvm_scan_") && name.endsWith(".apk")
            }
            cacheFiles?.forEach { it.delete() }
        } catch (_: Exception) {
            // Non-blocking cleanup
        }
    }
}