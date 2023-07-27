package com.neas.trace.tools.model

import com.neas.trace.tools.BuildOperationRecord

data class Dependencies(
    val scriptConfiguration: Boolean,
    val configurationDescription: String,
    val projectPath: String,
    val configurationTransitive: Boolean,
    val configurationName: String,
    val buildPath: String,
    val components: List<Dependency>,
) {
    companion object {
        fun fromRecord(record: BuildOperationRecord): Dependencies {
            val details = record.details ?: emptyMap()
            val scriptConfiguration = details["scriptConfiguration"] as Boolean
            val projectPath = details["projectPath"] as? String ?: ""
            val buildPath = details["buildPath"] as? String ?: ""
            return Dependencies(
                scriptConfiguration = scriptConfiguration,
                configurationDescription = details["configurationDescription"] as? String ?: "",
                projectPath = projectPath,
                configurationTransitive = details["configurationTransitive"] as Boolean,
                configurationName = details["configurationName"] as? String ?: "",
                buildPath = buildPath,
                components = Dependency.fromMap(
                    record.result ?: emptyMap(),
                    if (scriptConfiguration) buildPath else projectPath
                )
            )
        }
    }

    override fun toString(): String {
        return "path: '${if (scriptConfiguration) buildPath else projectPath}'\n" +
            "configurationName: $configurationName\n" +
            "components:\n${components.joinToString(separator = "\n") { "    $it" }}" +
            "\n"
    }
}