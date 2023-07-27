package com.neas.trace.tools.model

import com.neas.trace.tools.BuildOperationRecord

data class Task(
    val id: Long,
    val duration: Long,
    val buildPath: String,
    val taskPath: String,
    val taskId: Long,
    val taskClass: String,
    val cachingDisabledReasonMessage: String?,
    val taskOutcome: String?,// task outcome
    val skipReasonMessage: String?,
    val actionable: Boolean,
    val incremental: Boolean,
    val executionReason: List<String>?,// execute reason
    val originExecutionTime: Long?,
    val originBuildInvocationId: String?,
) {

    var snapshotInputs: SnapshotInputs? = null

    companion object {
        fun frommRecord(record: BuildOperationRecord): Task {
            val details = record.details as Map<String, *>
            val result = record.result as Map<String, *>
            return Task(
                id = record.id,
                duration = record.endTime - record.startTime,
                buildPath = details["buildPath"] as String,
                taskPath = details["taskPath"] as String,
                taskId = details["taskId"].toString().toDouble().toLong(),
                taskClass = details["taskClass"] as String,
                cachingDisabledReasonMessage = result["cachingDisabledReasonMessage"] as String?,
                taskOutcome = result["skipMessage"] as String?,
                skipReasonMessage = result["skipReasonMessage"] as String?,
                actionable = result["actionable"] as Boolean,
                incremental = result["incremental"] as Boolean,
                executionReason = result["upToDateMessages"] as? List<String>,
                originExecutionTime = result["originExecutionTime"].toString().toLongOrNull(),
                originBuildInvocationId = result["originBuildInvocationId"] as String?,
            )
        }
    }
}