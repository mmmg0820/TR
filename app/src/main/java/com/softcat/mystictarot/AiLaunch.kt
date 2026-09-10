package com.softcat.mystictarot

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import com.softcat.mystictarot.ui.components.HarmonyIcon

@Composable
fun AiAppLaunchRow(
    title: String,
    subtitle: String,
    packageName: String,
    webUrl: String,
    prompt: String
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    SystemMenuRow(title, subtitle, HarmonyIcon.Ai) {
        runCatching {
            clipboard.setText(AnnotatedString(prompt))
        }
        val opened = runCatching {
            openAiAppOrWeb(context, packageName, webUrl)
        }.getOrDefault(false)
        Toast.makeText(
            context,
            if (opened) {
                "요청문을 복사했습니다. AI 앱에서 붙여넣어 주세요."
            } else {
                "앱을 열 수 없어요. 요청문은 복사해두었습니다."
            },
            Toast.LENGTH_SHORT
        ).show()
    }
}

private fun openAiAppOrWeb(context: Context, packageName: String, webUrl: String): Boolean {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (launchIntent != null && context.tryStartAiActivity(launchIntent)) return true

    val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        .setPackage("com.android.vending")
    if (context.tryStartAiActivity(marketIntent)) return true

    val storeWebIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
    )
    if (context.tryStartAiActivity(storeWebIntent)) return true

    return context.tryStartAiActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)))
}

private fun Context.tryStartAiActivity(intent: Intent): Boolean {
    val safeIntent = Intent(intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return runCatching {
        startActivity(safeIntent)
        true
    }.getOrDefault(false)
}
