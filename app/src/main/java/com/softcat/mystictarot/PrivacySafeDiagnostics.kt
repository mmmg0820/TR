package com.softcat.mystictarot

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import org.json.JSONObject

internal val PRIVACY_SAFE_DIAGNOSTIC_KEYS = setOf(
    "schemaVersion",
    "generatedAt",
    "packageName",
    "versionName",
    "versionCode",
    "debuggable",
    "sdkInt",
    "manufacturer",
    "model",
    "densityDpi",
    "screenWidthDp",
    "screenHeightDp",
    "fontScale",
    "languageTag",
    "screen",
    "theme",
    "useReversed",
    "hapticsEnabled",
    "adsDisabled",
    "cardBackStyle",
    "hasCustomCardBack",
    "customDeckCount",
    "enabledDeckCount",
    "savedReadingCount",
    "savedSpreadCount",
    "hasReadingDraft"
)

data class PrivacySafeDiagnostics(
    val generatedAt: Long,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val debuggable: Boolean,
    val sdkInt: Int,
    val manufacturer: String,
    val model: String,
    val densityDpi: Int,
    val screenWidthDp: Int,
    val screenHeightDp: Int,
    val fontScale: Float,
    val languageTag: String,
    val screen: String,
    val theme: String,
    val useReversed: Boolean,
    val hapticsEnabled: Boolean,
    val adsDisabled: Boolean,
    val cardBackStyle: String,
    val hasCustomCardBack: Boolean,
    val customDeckCount: Int,
    val enabledDeckCount: Int,
    val savedReadingCount: Int,
    val savedSpreadCount: Int,
    val hasReadingDraft: Boolean
) {
    internal fun toSafeFieldMap(): Map<String, Any> = linkedMapOf(
        "schemaVersion" to 1,
        "generatedAt" to generatedAt,
        "packageName" to packageName,
        "versionName" to versionName,
        "versionCode" to versionCode,
        "debuggable" to debuggable,
        "sdkInt" to sdkInt,
        "manufacturer" to manufacturer,
        "model" to model,
        "densityDpi" to densityDpi,
        "screenWidthDp" to screenWidthDp,
        "screenHeightDp" to screenHeightDp,
        "fontScale" to fontScale.toDouble(),
        "languageTag" to languageTag,
        "screen" to screen,
        "theme" to theme,
        "useReversed" to useReversed,
        "hapticsEnabled" to hapticsEnabled,
        "adsDisabled" to adsDisabled,
        "cardBackStyle" to cardBackStyle,
        "hasCustomCardBack" to hasCustomCardBack,
        "customDeckCount" to customDeckCount.coerceAtLeast(0),
        "enabledDeckCount" to enabledDeckCount.coerceAtLeast(0),
        "savedReadingCount" to savedReadingCount.coerceAtLeast(0),
        "savedSpreadCount" to savedSpreadCount.coerceAtLeast(0),
        "hasReadingDraft" to hasReadingDraft
    )

    fun toJsonString(): String = JSONObject(toSafeFieldMap()).toString(2)
}

fun collectPrivacySafeDiagnostics(
    context: Context,
    screen: AppScreen,
    settings: AppSettings,
    customDeckCount: Int,
    enabledDeckCount: Int,
    savedReadingCount: Int,
    savedSpreadCount: Int,
    hasReadingDraft: Boolean,
    now: Long = System.currentTimeMillis()
): PrivacySafeDiagnostics {
    val packageInfo = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
    }.getOrNull()
    val configuration = context.resources.configuration
    val metrics = context.resources.displayMetrics
    return PrivacySafeDiagnostics(
        generatedAt = now,
        packageName = context.packageName,
        versionName = packageInfo?.versionName.orEmpty(),
        versionCode = packageInfo?.longVersionCode ?: 0L,
        debuggable = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0,
        sdkInt = Build.VERSION.SDK_INT,
        manufacturer = Build.MANUFACTURER.orEmpty(),
        model = Build.MODEL.orEmpty(),
        densityDpi = metrics.densityDpi,
        screenWidthDp = configuration.screenWidthDp,
        screenHeightDp = configuration.screenHeightDp,
        fontScale = configuration.fontScale,
        languageTag = configuration.locales.get(0)?.toLanguageTag().orEmpty(),
        screen = screen.name,
        theme = settings.themeChoice.name,
        useReversed = settings.useReversed,
        hapticsEnabled = settings.hapticsEnabled,
        adsDisabled = settings.adsDisabled,
        cardBackStyle = settings.cardBackStyle.name,
        hasCustomCardBack = settings.customCardBackUri != null,
        customDeckCount = customDeckCount,
        enabledDeckCount = enabledDeckCount,
        savedReadingCount = savedReadingCount,
        savedSpreadCount = savedSpreadCount,
        hasReadingDraft = hasReadingDraft
    )
}

fun sharePrivacySafeDiagnostics(context: Context, diagnostics: PrivacySafeDiagnostics): Boolean =
    runCatching {
        val sendIntent = Intent(Intent.ACTION_SEND)
            .setType("application/json")
            .putExtra(Intent.EXTRA_SUBJECT, "먕타로 진단 정보")
            .putExtra(Intent.EXTRA_TEXT, diagnostics.toJsonString())
        val chooser = Intent.createChooser(sendIntent, "진단 정보 공유")
        if (context !is Activity) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
        true
    }.getOrDefault(false)
