package com.neas.trace.tools.model

data class Dependency(
    val component: String,
    val repoName: String,
) {
    companion object {
        fun fromMap(map: Map<String, Any?>, path: String): List<Dependency> {
            val components = (map["components"] as? Map<*, *>) ?: emptyMap<Any, Any>()
            return components.mapNotNull {
                val component = it.key as? String ?: ""
                if (component.removePrefix("project ") == path) {// remove self
                    null
                } else {
                    Dependency(
                        component = component,
                        repoName = (it.value as? Map<*, *>)?.get("repoName") as? String ?: ""
                    )
                }
            }
        }
    }

    override fun toString(): String {
        return "component: $component${if (repoName.isNotBlank()) "($repoName)" else ""}"
    }
}