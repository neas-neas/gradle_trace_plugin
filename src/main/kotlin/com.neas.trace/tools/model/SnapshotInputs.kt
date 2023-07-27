package com.neas.trace.tools.model

import com.neas.trace.tools.BuildOperationRecord

data class SnapshotInputs(
    val parentId: Long? = null,
    val duration: Long,
    val result: Map<String, *>,
) {
    companion object {
        fun fromRecord(record: BuildOperationRecord): SnapshotInputs {
            val result = record.result ?: emptyMap()
            return SnapshotInputs(
                parentId = record.parentId,
                duration = record.endTime - record.startTime,
                result = result
            )
        }
    }

}