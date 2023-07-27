package com.neas.trace

import org.gradle.internal.operations.trace.BuildOperationTrace

internal class SerializedOperationFinish : SerializedOperation {
    val id: Long
    val endTime: Long
    val result: Any?
    val resultClassName: String?
    val failureMsg: String?

    constructor(id: Long, endTime: Long, result: Any?, failure: Throwable?) {
        this.id = id
        this.endTime = endTime
        this.result = BuildOperationTrace.toSerializableModel(result)
        resultClassName = result?.javaClass?.name
        failureMsg = failure?.toString()
    }

    constructor(map: Map<String?, *>) {
        id = (map["id"] as Int?)!!.toLong()
        endTime = (map["endTime"] as Long?)!!
        result = map["result"]
        resultClassName = map["resultClassName"] as String?
        failureMsg = map["failure"] as String?
    }

    override fun toMap(): Map<String, *> {
        val map = HashMap<String, Any?>()

        // Order is optimised for humans looking at the log.
        map["id"] = id
        if (result != null) {
            map["result"] = result
            map["resultClassName"] = resultClassName!!
        }
        if (failureMsg != null) {
            map["failure"] = failureMsg
        }
        map["endTime"] = endTime
        return map
    }
}