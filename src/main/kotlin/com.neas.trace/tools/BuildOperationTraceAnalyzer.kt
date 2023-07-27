package com.neas.trace.tools

import com.neas.trace.tools.buildops.*
import com.neas.trace.tools.model.*
import java.io.File

class BuildOperationTraceAnalyzer : BuildOperationVisitor {

    private var project: ProjectNode? = null
    private var switches = Switch()
    private var buildCache: BuildCache? = null
    private val plugins: MutableMap<String, MutableList<Plugins>> = mutableMapOf()
    private val dependencies: MutableList<Dependencies> = mutableListOf()
    private val buildDependencies: MutableList<Dependencies> = mutableListOf()
    private val tasks = mutableListOf<Task>()

    override fun visit(record: BuildOperationRecord): PostVisit {
        when {
            record.isLoadProjects() -> {
                val result = record.result ?: return {}
                val rootProject = result["rootProject"] as Map<*, *>
                project = ProjectNode.fromRecord(rootProject, rootProject["projectDir"] as String)
            }

            record.isRootBuildOperation() -> {
                record.progress?.forEach {
                    if (it.detailsClassName == CONFIGURATION_CACHE_SWITCH) {
                        switches.configurationCache = it.details?.get("enabled") == true
                    } else if (it.detailsClassName == FILE_SYSTEM_WATCH) {
                        switches.filesystemWatching = it.details?.get("enabled") == true
                    }
                }
            }

            record.isEvaluateSettings() -> {
                // evaluate settings duration
            }

            record.isBuildCache() -> {
                val buildPath = record.details?.get("buildPath")
                buildCache = BuildCache.fromRecord(record)
                switches.buildCache = buildCache?.enabled ?: false
            }

            record.isApplySettings() -> {
                val details = record.details ?: return {}
                val buildFile = details["buildFile"] as? String ?: ""
                val buildUri = details["uri"] as? String
                val buildPath = details["buildPath"] as? String ?: ""
                collectPlugins(record)
            }

            record.isConfigureProject() -> {
                collectPlugins(record)
            }

            record.isResolveDependency() -> {
                val dep = Dependencies.fromRecord(record)
                if (dep.scriptConfiguration) {
                    buildDependencies.add(dep)
                } else {
                    dependencies.add(dep)
                }
            }

            record.isExecuteTask() -> {
                collectTasks(record)
            }

            record.isSnapshotInputs() -> {
                collectSnapshotInputs(record)
            }
        }
        return {}
    }

    fun convert(slice: BuildOperationTraceSlice, outputFile: File) {
        BuildOperationVisitor.visitRecords(slice, this)

        writeProjectInfo(project, outputFile)

        writeSwitches(outputFile)

        writeBuildCache(outputFile)

        writeApplyScript(outputFile)

        writePlugins(outputFile)

        writeBuildDependencies(outputFile)

        writeDependencies(outputFile)

        writeTaskExecution(outputFile)

        println("ANALYZER: Wrote analyzer result to ${outputFile.absolutePath}")
    }

    private fun collectTasks(record: BuildOperationRecord) {
        tasks.add(Task.frommRecord(record))
    }

    private fun collectPlugins(record: BuildOperationRecord) {
        record.children?.forEach {
            if (it.isApplyPlugin()) {
                it.details?.let { details ->
                    val plugin = Plugins.fromRecord(details)
                    val key = plugin.targetPath ?: plugin.buildPath ?: ""
                    val value = plugins[key]
                    val newValue = if (value == null) {
                        mutableListOf(plugin)
                    } else {
                        value.add(plugin)
                        value
                    }
                    plugins[key] = newValue
                }
            }
        }
    }

    private fun collectSnapshotInputs(record: BuildOperationRecord) {
        val snapshotInputs = SnapshotInputs.fromRecord(record)
        tasks.find { it.id == snapshotInputs.parentId }.let { task ->
            task?.snapshotInputs = snapshotInputs
        }
    }

    private fun writeApplyScript(outputFile: File) {
        // TODO: apply scripts
        outputFile.appendText("\n\nScripts\n")
    }

    private fun writeTaskExecution(outputFile: File) {
        outputFile.appendText("\n\nTaskExecution\n")
        tasks.forEach { task ->
            outputFile.appendText("\n")
            outputFile.appendText("task '${task.taskPath}' ${if (task.taskOutcome.isNullOrEmpty()) "" else task.taskOutcome}\n")
            outputFile.appendText("taskClass: ${task.taskClass}\n")
            outputFile.appendText("duration: ${task.duration / 1000.toDouble()}s\n")
            outputFile.appendText("    snapshot inputs duration: ${(task.snapshotInputs?.duration ?: 0) / 1000.toDouble()}s\n")
            // TODO: snapshot duration, build cache duration
            if (!task.skipReasonMessage.isNullOrEmpty()) {
                outputFile.appendText("skipped reason: ${task.skipReasonMessage}\n")
            }
            if (!task.cachingDisabledReasonMessage.isNullOrEmpty()) {
                outputFile.appendText("cacheable: false, reason: ${task.cachingDisabledReasonMessage}\n")
            } else {
                outputFile.appendText("cacheable: true\n")
            }
            outputFile.appendText("actionable: ${task.actionable}\n")
            outputFile.appendText("incremental: ${task.incremental}\n")
            if (!task.executionReason.isNullOrEmpty()) {
                outputFile.appendText("execution reason: \n${task.executionReason.joinToString(prefix = "    ", separator = "\n")}\n")
            }
            if (task.originExecutionTime != null) {
                outputFile.appendText("originExecutionTime: ${task.originExecutionTime}\n")
            }
            if (task.snapshotInputs?.result?.get("hash") != null) {
                outputFile.appendText("snapshot inputs hash: \n${task.snapshotInputs?.result}\n")
            }
        }
    }

    private fun writeDepCommon(dependencies: Map<String, List<Dependencies>>, outputFile: File) {
        dependencies.forEach { group ->
            outputFile.appendText("path: '${group.key}'\n")
            group.value.forEach { dep ->
                if (dep.components.isNotEmpty()) {
                    outputFile.appendText("    configurationName: ${dep.configurationName}\n")
                    outputFile.appendText(
                        "    components:\n${dep.components.joinToString(separator = "\n") { "        ${it.component}${if (it.repoName.isNotBlank()) "(${it.repoName})" else ""}" }}"
                    )
                    outputFile.appendText("\n")
                }
            }
            outputFile.appendText("\n")
        }
    }

    private fun writeDependencies(outputFile: File) {
        outputFile.appendText("\n\nDependencies\n")
        writeDepCommon(dependencies.groupBy { it.projectPath }, outputFile)
    }

    private fun writeBuildDependencies(outputFile: File) {
        outputFile.appendText("\n\nBuild dependencies\n")
        writeDepCommon(buildDependencies.groupBy { it.buildPath }, outputFile)
    }

    private fun writePlugins(outputFile: File) {
        outputFile.appendText("\n\nPlugins\n")
        if (plugins.isEmpty()) {
            outputFile.appendText("no plugins")
        } else {
            plugins.forEach { entry ->
                outputFile.appendText("path '${entry.key}'\n")
                entry.value.groupBy { it.targetType }.forEach { group ->
                    outputFile.appendText("    targetType: ${group.key}\n")
                    group.value.forEach { plugin ->
                        outputFile.appendText("        id: ${plugin.pluginId ?: "no_id"}  pluginClass: ${plugin.pluginClass}\n")
                    }
                }
            }
        }
    }

    private fun writeBuildCache(outputFile: File) {
        outputFile.appendText("\n\nBuildCache\n")
        outputFile.appendText(buildCache.toString())
    }

    private fun writeSwitches(outputFile: File) {
        outputFile.appendText("\n\nSwitches\n")
        outputFile.appendText("Configuration Cache: ${if (switches.configurationCache) "On" else "Off"}\n")
        outputFile.appendText("File System Watch: ${if (switches.filesystemWatching) "On" else "Off"}\n")
        outputFile.appendText("Build Cache: ${if (switches.buildCache) "On" else "Off"}\n")
    }

    private fun writeProjectInfo(project: ProjectNode?, outputFile: File, indent: String = "") {
        fun writeSingleProject(project: ProjectNode, outputFile: File, indent: String = "") {
            var dir = project.projectDir.removePrefix(project.rootDir)
            if (dir.isBlank()) {
                dir = "root"
            }
            outputFile.appendText("${indent}${project.path}(dir: ${dir})\n")
            project.children.forEach {
                writeSingleProject(it, outputFile, "$indent    ")
            }
        }
        project?.let {
            outputFile.writeText("Project\n")
            writeSingleProject(project, outputFile, indent)
        }
    }

}
