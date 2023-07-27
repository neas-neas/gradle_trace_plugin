package com.neas.trace

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.GradleInternal
import org.gradle.internal.enterprise.core.GradleEnterprisePluginManager
import org.gradle.internal.operations.notify.*
import javax.inject.Inject

class BuildTracePlugin @Inject constructor(
    val registrar: BuildOperationNotificationListenerRegistrar,
) : Plugin<Settings> {

    override fun apply(target: Settings) {
        val manager = (target.gradle as GradleInternal).services.get(GradleEnterprisePluginManager::class.java)
        if (manager.adapter == null) {
            val adapter = BuildOperationTraceAdapter(registrar)
            adapter.createService()
            manager.registerAdapter(adapter)
        }
    }

}