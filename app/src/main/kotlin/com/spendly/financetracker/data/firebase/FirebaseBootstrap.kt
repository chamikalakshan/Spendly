package com.spendly.financetracker.data.firebase

import android.content.Context
import com.google.firebase.FirebaseApp

object FirebaseBootstrap {
    const val MISSING_CONFIG_MESSAGE =
        "Firebase is not configured. Add your Firebase google-services.json file to the app module."

    fun isConfigured(context: Context): Boolean {
        val appId = context.resources.getIdentifier(
            "google_app_id",
            "string",
            context.packageName
        )
        return appId != 0
    }

    fun ensureInitialized(context: Context): Boolean {
        if (!isConfigured(context)) return false

        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }

        return FirebaseApp.getApps(context).isNotEmpty()
    }
}
