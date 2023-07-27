package com.neas.trace.tools.model

import com.neas.trace.tools.BuildOperationRecord

@Suppress("UNCHECKED_CAST")
data class BuildCache(
    val buildPath: String,
    val enabled: Boolean,
    val remote: Map<String, *>?,
    val local: Map<String, *>?,
) {
    companion object {
        fun fromRecord(record: BuildOperationRecord): BuildCache {
            val result = record.result ?: emptyMap()
            return BuildCache(
                buildPath = record.details?.get("buildPath") as? String ?: "",
                result["enabled"] as Boolean,
                result["remote"] as? Map<String, *>,
                result["local"] as? Map<String, *>
            )
        }
    }

    override fun toString(): String {
        val local = if (local != null) {
            val localConfig = local["config"] as? Map<String, *>
            """
            Local cache:
                Type: ${local["type"]}
                Push: ${if (local["push"] == true) "enabled" else "disabled"}
                Configuration
                    Location: ${localConfig?.get("location")}
                    RemoveUnusedEntriesAfter: ${localConfig?.get("removeUnusedEntriesAfter")}
            """.trimIndent()
        } else {
            "Local cache: disabled"
        }
        val remote = if (remote != null) {
            val remoteConfig = remote["config"] as? Map<String, *>
            """
            Remote cache:
                Type: ${remote["type"]}
                Push: ${if (remote["push"] == true) "enabled" else "disabled"}
                Configuration
                    URI: ${remoteConfig?.get("uri")}
                    Authenticated: ${remoteConfig?.get("authenticated")}
                    AllowUntrustedServer: ${remoteConfig?.get("allowUntrustedServer")}
                    AllowInsecureProtocol: ${remoteConfig?.get("allowInsecureProtocol")}
                    UseExpectContinue: ${remoteConfig?.get("useExpectContinue")}
            """.trimIndent()
        } else {
            "Remote cache: disabled"
        }
        return local + "\n" + remote
    }
}