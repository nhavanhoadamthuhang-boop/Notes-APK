package com.example.ui.util

import java.text.Normalizer

object SearchUtils {
    fun normalize(input: String): String {
        val nfd = Normalizer.normalize(input, Normalizer.Form.NFD)
        val pattern = "\\p{InCombiningDiacriticalMarks}+".toRegex()
        return pattern.replace(nfd, "")
            .replace("đ", "d", ignoreCase = true)
            .replace("Đ", "D", ignoreCase = true)
            .lowercase()
            .trim()
    }

    fun matches(text: String, query: String): Boolean {
        if (query.isBlank()) return true
        if (text.contains(query, ignoreCase = true)) return true
        val normText = normalize(text)
        val normQuery = normalize(query)
        return normText.contains(normQuery)
    }
}
