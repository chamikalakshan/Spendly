package com.spendly.financetracker.ui.util

import org.json.JSONArray
import org.json.JSONObject

data class CategorySettings(
    val customExpenseCategories: List<String> = emptyList(),
    val hiddenExpenseCategories: List<String> = emptyList(),
    val customIncomeSources: List<String> = emptyList(),
    val hiddenIncomeSources: List<String> = emptyList()
) {
    fun visibleExpenses(defaults: List<String>): List<String> =
        (defaults.filterNot { it in hiddenExpenseCategories } + customExpenseCategories)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

    fun visibleIncomeSources(defaults: List<String>): List<String> =
        (defaults.filterNot { it in hiddenIncomeSources } + customIncomeSources)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
}

fun parseCategorySettings(json: String?): CategorySettings {
    if (json.isNullOrBlank()) return CategorySettings()
    return runCatching {
        val root = JSONObject(json)
        CategorySettings(
            customExpenseCategories = root.optStringList("customExpenseCategories"),
            hiddenExpenseCategories = root.optStringList("hiddenExpenseCategories"),
            customIncomeSources = root.optStringList("customIncomeSources"),
            hiddenIncomeSources = root.optStringList("hiddenIncomeSources")
        )
    }.getOrDefault(CategorySettings())
}

fun CategorySettings.toJson(): String = JSONObject()
    .put("customExpenseCategories", customExpenseCategories.toJsonArray())
    .put("hiddenExpenseCategories", hiddenExpenseCategories.toJsonArray())
    .put("customIncomeSources", customIncomeSources.toJsonArray())
    .put("hiddenIncomeSources", hiddenIncomeSources.toJsonArray())
    .toString()

private fun JSONObject.optStringList(key: String): List<String> {
    val array = optJSONArray(key) ?: return emptyList()
    return buildList {
        for (i in 0 until array.length()) {
            array.optString(i).trim().takeIf { it.isNotBlank() }?.let(::add)
        }
    }.distinct()
}

private fun List<String>.toJsonArray(): JSONArray {
    val array = JSONArray()
    map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .forEach { array.put(it) }
    return array
}
