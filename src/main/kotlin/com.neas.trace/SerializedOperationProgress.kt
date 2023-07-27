package com.neas.trace

import org.gradle.internal.operations.trace.BuildOperationTrace

internal class SerializedOperationProgress : SerializedOperation {
    val id: Long
    val time: Long
    val details: Any?
    val detailsClassName: String?

    constructor(id: Long, time: Long, details: Any?) {
        this.id = id
        this.time = time
        this.details = BuildOperationTrace.toSerializableModel(details)
        detailsClassName = details?.javaClass?.name
    }

    constructor(map: Map<String?, *>) {
        id = (map["id"] as Int?)!!.toLong()
        time = (map["time"] as Long?)!!
        details = map["details"]
        detailsClassName = map["detailsClassName"] as String?
    }

    override fun toMap(): Map<String, *> {
        val map = HashMap<String, Any?>()

        // Order is optimised for humans looking at the log.
        if (details != null) {
            map["details"] = details
            map["detailsClassName"] = detailsClassName!!
        }
        map["id"] = id
        map["time"] = time
        return map
    }
}