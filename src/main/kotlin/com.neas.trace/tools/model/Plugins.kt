package com.neas.trace.tools.model

data class Plugins(
    val pluginId: String?,
    val targetPath: String?,
    val pluginClass: String?,
    val targetType: String?,
    val applicationId: Long,
    val buildPath: String?,
) {
    companion object {
        fun fromRecord(details: Map<*, *>): Plugins {
            return Plugins(
                pluginId = details["pluginId"] as String?,
                targetPath = details["targetPath"] as String?,
                pluginClass = details["pluginClass"] as String?,
                targetType = details["targetType"] as String?,
                applicationId = details["applicationId"].toString().toLongOrNull() ?: 0,
                buildPath = details["buildPath"] as String?,
            )
        }
    }
}