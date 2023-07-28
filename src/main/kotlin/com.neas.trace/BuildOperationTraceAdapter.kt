package com.neas.trace

import org.gradle.internal.enterprise.core.GradleEnterprisePluginAdapter
import org.gradle.internal.operations.notify.BuildOperationNotificationListenerRegistrar

class BuildOperationTraceAdapter(
    private val registrar: BuildOperationNotificationListenerRegistrar,
    rootDir: String,
) : GradleEnterprisePluginAdapter {

    val listener = BuildOperationTraceListener(rootDir)

    override fun shouldSaveToConfigurationCache(): Boolean {
        return true
    }

    override fun onLoadFromConfigurationCache() {
        createService()
    }

    override fun buildFinished(buildFailure: Throwable?) {
    }


    fun createService() {
        registrar.register(listener)
    }
}