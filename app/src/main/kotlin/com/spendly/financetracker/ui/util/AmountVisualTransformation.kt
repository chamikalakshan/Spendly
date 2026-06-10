package com.spendly.financetracker.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

object AmountVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val dotIndex = raw.indexOf('.')
        val whole = raw.substringBefore('.')
        val decimal = if (dotIndex >= 0) raw.substring(dotIndex) else ""
        val formattedWhole = whole.reversed().chunked(3).joinToString(",").reversed()
        val formatted = formattedWhole + decimal
        val commaPositions = formatted.mapIndexedNotNull { index, char -> if (char == ',') index else null }

        return TransformedText(
            AnnotatedString(formatted),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    val prefix = raw.take(offset)
                    val prefixDotIndex = prefix.indexOf('.')
                    val prefixWhole = prefix.substringBefore('.')
                    val prefixDecimal = if (prefixDotIndex >= 0) prefix.substring(prefixDotIndex) else ""
                    val formattedPrefix = prefixWhole.reversed().chunked(3).joinToString(",").reversed() + prefixDecimal
                    return formattedPrefix.length.coerceIn(0, formatted.length)
                }

                override fun transformedToOriginal(offset: Int): Int {
                    val commasBefore = commaPositions.count { it < offset }
                    return (offset - commasBefore).coerceIn(0, raw.length)
                }
            }
        )
    }
}
