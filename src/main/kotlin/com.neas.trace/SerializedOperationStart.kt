package com.neas.trace

import org.gradle.internal.operations.trace.BuildOperationTrace

internal class SerializedOperationStart : SerializedOperation {
    val id: Long
    val parentId: Long?
    val displayName: String?
    val startTime: Long
    val details: Any?
    val detailsClassName: String?

    constructor(id: Long, parentId: Long?, displayName: String?, startTime: Long, details: Any?) {
        this.id = id
        this.parentId = parentId
        this.displayName = displayName
        this.startTime = startTime
        this.details = BuildOperationTrace.toSerializableModel(details)
        detailsClassName = details?.javaClass?.name
    }

    constructor(map: Map<String?, *>) {
        id = (map["id"] as Int?)!!.toLong()
        val parentId = map["parentId"] as Int?
        this.parentId = parentId?.toLong()
        displayName = map["displayName"] as String?
        startTime = (map["startTime"] as Long?)!!
        details = map["details"]
        detailsClassName = map["detailsClassName"] as String?
    }

    override fun toMap(): Map<String, *> {
        val map = HashMap<String, Any?>()

        // Order is optimised for humans looking at the log.
        map["displayName"] = displayName!!
        if (details != null) {
            map["details"] = details
            map["detailsClassName"] = detailsClassName!!
        }
        map["id"] = id
        if (parentId != null) {
            map["parentId"] = parentId
        }
        map["startTime"] = startTime
        return map
    }
}