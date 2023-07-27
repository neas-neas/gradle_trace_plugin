package com.neas.trace.tools.model

data class ProjectNode(
    val rootDir: String,
    val projectDir: String,
    val identityPath: String,
    val path: String,
    val name: String,
    val buildFile: String,
    val children: List<ProjectNode> = emptyList(),
) {
    companion object {
        fun fromRecord(project: Map<*, *>, rootDir: String): ProjectNode {
            return ProjectNode(
                rootDir,
                project["projectDir"] as String,
                project["identityPath"] as String,
                project["path"] as String,
                project["name"] as String,
                project["buildFile"] as String,
                (project["children"] as? List<*> ?: emptyList<Any>()).map {
                    fromRecord(it as Map<*, *>, rootDir)
                }
            )
        }
    }
}