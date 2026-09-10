package com.softcat.mystictarot

import java.security.MessageDigest
import java.util.Locale

internal data class PurchaseCode(
    val label: String,
    val normalizedSha256: String,
    val disablesAds: Boolean = true
)

private val knownPurchaseCodes = listOf(
    PurchaseCode(
        label = "Founder's No Ads",
        normalizedSha256 = "ca3cbcc246c7aaa6e2e2d57464b36445d56b0781e928c5347b7af9ca963d8b03"
    ),
    PurchaseCode(
        label = "Hoscat No Ads",
        normalizedSha256 = "cd9f3fb8ba7d3c83d0bfb5ce6a6d40294f5f9fecd9caf7abad344fde7b462a25"
    ),
    PurchaseCode(
        label = "Myang Support",
        normalizedSha256 = "22599188464af079adec8770d28dd69c5b99eb196c108c6f57aa2744ed068390"
    )
)

internal fun redeemPurchaseCode(input: String): PurchaseCode? {
    val normalized = normalizePurchaseCode(input)
    if (normalized.isBlank()) return null
    val digest = sha256Hex(normalized)
    return knownPurchaseCodes.firstOrNull { it.normalizedSha256 == digest }
}

private fun normalizePurchaseCode(input: String): String {
    return input.filterNot { it.isWhitespace() }.lowercase(Locale.ROOT)
}

private fun sha256Hex(value: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it.toInt() and 0xff) }
}
