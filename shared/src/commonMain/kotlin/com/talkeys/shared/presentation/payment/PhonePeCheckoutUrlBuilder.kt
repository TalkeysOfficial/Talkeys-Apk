package com.talkeys.shared.presentation.payment

object PhonePeCheckoutUrlBuilder {
    private const val PRODUCTION_CHECKOUT_BASE_URL = "https://mercury.phonepe.com/transact/pg"
    private const val SANDBOX_CHECKOUT_BASE_URL = "https://mercury-t2.phonepe.com/transact/pg"

    fun buildCheckoutUrl(tokenOrUrl: String, isProduction: Boolean): String {
        val trimmed = tokenOrUrl.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed

        val baseUrl = if (isProduction) PRODUCTION_CHECKOUT_BASE_URL else SANDBOX_CHECKOUT_BASE_URL
        return "$baseUrl?token=${urlEncode(trimmed)}"
    }

    private fun urlEncode(value: String): String = buildString {
        value.encodeToByteArray().forEach { byte ->
            val char = byte.toInt().toChar()
            if (char.isUrlSafe()) {
                append(char)
            } else {
                append('%')
                append(byte.toUByte().toString(16).uppercase().padStart(2, '0'))
            }
        }
    }

    private fun Char.isUrlSafe(): Boolean =
        this in 'A'..'Z' ||
            this in 'a'..'z' ||
            this in '0'..'9' ||
            this == '-' ||
            this == '_' ||
            this == '.' ||
            this == '~'
}
