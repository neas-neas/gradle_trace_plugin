package com.neas.trace.tools.buildops

import com.neas.trace.tools.BuildOperationRecord

fun BuildOperationRecord.isExecuteTask() =
    detailsClassName == "org.gradle.api.internal.tasks.execution.ExecuteTaskBuildOperationDetails"

fun BuildOperationRecord.isExecuteScheduledTransformationStep() =
    detailsClassName == "org.gradle.api.internal.artifacts.transform.ExecuteScheduledTransformationStepBuildOperationDetails"

fun BuildOperationRecord.isDependency() =
    detailsClassName == "org.gradle.api.internal.tasks.execution.ExecuteTaskBuildOperationDetails"

fun BuildOperationRecord.isLoadProjects() =
    detailsClassName == "org.gradle.initialization.NotifyingBuildLoader$2$1"

fun BuildOperationRecord.isRootBuildOperation() =
    detailsClassName == "org.gradle.launcher.exec.RunAsBuildOperationBuildActionExecutor$1"

fun BuildOperationRecord.isLoadBuild() =
    detailsClassName == "org.gradle.initialization.BuildOperationFiringSettingsPreparer\$LoadBuild$1"

fun BuildOperationRecord.isEvaluateSettings() =
    detailsClassName == "org.gradle.initialization.BuildOperationSettingsProcessor$2$1"

fun BuildOperationRecord.isApplySettings() =
    detailsClassName == "org.gradle.configuration.BuildOperationScriptPlugin\$OperationDetails"

fun BuildOperationRecord.isApplyPlugin() =
    detailsClassName == "org.gradle.api.internal.plugins.DefaultPluginManager\$OperationDetails"

fun BuildOperationRecord.isConfigureProject() =
    detailsClassName == "org.gradle.configuration.project.LifecycleProjectEvaluator\$ConfigureProjectDetails"

fun BuildOperationRecord.isResolveDependency() =
    detailsClassName == "org.gradle.api.internal.artifacts.configurations.ResolveConfigurationResolutionBuildOperationDetails"

fun BuildOperationRecord.isBuildCache() =
    detailsClassName == "org.gradle.caching.internal.services.BuildCacheControllerFactory\$DetailsImpl"

fun BuildOperationRecord.isSnapshotInputs() =
    detailsClassName == "org.gradle.api.internal.tasks.execution.TaskExecution$1"

const val CONFIGURATION_CACHE_SWITCH = "org.gradle.initialization.BuildOptionBuildOperationProgressEventsEmitter$1"
const val FILE_SYSTEM_WATCH = "org.gradle.tooling.internal.provider.FileSystemWatchingBuildActionRunner$1"

