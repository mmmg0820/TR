package com.softcat.mystictarot.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.softcat.mystictarot.TarotCard
import com.softcat.mystictarot.ui.theme.LocalHarmonyColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

private object TarotBitmapCache {
    private val cache = object : LruCache<String, Bitmap>(12 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.allocationByteCount / 1024
        }
    }

    @Synchronized
    fun get(key: String): Bitmap? = cache.get(key)

    @Synchronized
    fun put(key: String, bitmap: Bitmap) {
        cache.put(key, bitmap)
    }

    @Synchronized
    fun clear() {
        cache.evictAll()
    }
}

private object TarotBitmapDecodeLocks {
    private val locks = Array(16) { Any() }

    fun lockFor(key: String): Any {
        val index = (key.hashCode() and Int.MAX_VALUE) % locks.size
        return locks[index]
    }
}

fun clearTarotBitmapCache() = TarotBitmapCache.clear()

@Composable
fun TarotImage(
    card: TarotCard,
    modifier: Modifier = Modifier,
    isReversed: Boolean = false,
    maxTextureSize: Int = 768,
    accessibilityDescription: String = card.nameKr
) {
    val context = LocalContext.current
    val rotation = if (isReversed) 180f else 0f
    val imageModifier = modifier
        .fillMaxSize()
        .graphicsLayer { rotationZ = rotation }

    card.imageUri?.let { uri ->
        val customBitmap by produceState<ImageBitmap?>(initialValue = null, uri, maxTextureSize) {
            withContext(Dispatchers.IO) {
                sampledImageBitmapFromUri(
                    context = context,
                    uriString = uri,
                    maxTextureSize = maxTextureSize
                )
            }
        }
        if (customBitmap != null) {
            Image(
                bitmap = customBitmap!!,
                contentDescription = accessibilityDescription,
                modifier = imageModifier,
                contentScale = ContentScale.Fit
            )
        } else {
            TarotImagePlaceholder(accessibilityDescription, modifier)
        }
        return
    }

    val resId = tarotDrawableResId(card.id)
    if (resId != null) {
        val resourceBitmap by produceState<ImageBitmap?>(initialValue = null, resId, maxTextureSize) {
            value = withContext(Dispatchers.IO) {
                sampledImageBitmapFromResource(
                    context = context,
                    resId = resId,
                    maxTextureSize = maxTextureSize
                )
            }
        }
        resourceBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap,
                contentDescription = accessibilityDescription,
                modifier = imageModifier,
                contentScale = ContentScale.Fit
            )
            return
        }
        TarotImagePlaceholder(accessibilityDescription, modifier)
    } else {
        TarotImagePlaceholder(accessibilityDescription, modifier)
    }
}

@Composable
private fun TarotImagePlaceholder(accessibilityDescription: String, modifier: Modifier) {
    val colors = LocalHarmonyColors.current
    Box(
        modifier
            .fillMaxSize()
            .semantics { contentDescription = "$accessibilityDescription, 이미지 없음" }
            .background(colors.surfaceSecondary),
        contentAlignment = Alignment.Center
    ) {
        Text("✦", color = colors.textSecondary, fontSize = 42.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun sampledImageBitmapFromResource(
    context: Context,
    resId: Int,
    maxTextureSize: Int
): ImageBitmap? {
    val cacheKey = "res:$resId#$maxTextureSize"
    TarotBitmapCache.get(cacheKey)?.let { return it.asImageBitmap() }
    return synchronized(TarotBitmapDecodeLocks.lockFor(cacheKey)) {
        TarotBitmapCache.get(cacheKey)?.let { return@synchronized it.asImageBitmap() }
        runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeResource(context.resources, resId, bounds)

            val largestSide = max(bounds.outWidth, bounds.outHeight)
            val sampleSize = if (largestSide <= 0) {
                1
            } else {
                var sample = 1
                while (largestSide / sample > maxTextureSize) {
                    sample *= 2
                }
                sample
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = android.graphics.Bitmap.Config.RGB_565
                inScaled = false
            }
            BitmapFactory.decodeResource(context.resources, resId, decodeOptions)?.also { bitmap ->
                TarotBitmapCache.put(cacheKey, bitmap)
            }?.asImageBitmap()
        }.getOrNull()
    }
}

fun sampledImageBitmapFromUri(
    context: Context,
    uriString: String,
    maxTextureSize: Int
): ImageBitmap? {
    val cacheKey = "$uriString#$maxTextureSize"
    TarotBitmapCache.get(cacheKey)?.let { return it.asImageBitmap() }
    return synchronized(TarotBitmapDecodeLocks.lockFor(cacheKey)) {
        TarotBitmapCache.get(cacheKey)?.let { return@synchronized it.asImageBitmap() }
        runCatching {
            val uri = Uri.parse(uriString)
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, bounds)
            }

            val largestSide = max(bounds.outWidth, bounds.outHeight)
            val sampleSize = if (largestSide <= 0) {
                1
            } else {
                var sample = 1
                while (largestSide / sample > maxTextureSize) {
                    sample *= 2
                }
                sample
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = android.graphics.Bitmap.Config.RGB_565
                inScaled = false
            }
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)?.also { bitmap ->
                    TarotBitmapCache.put(cacheKey, bitmap)
                }?.asImageBitmap()
            }
        }.getOrNull()
    }
}
