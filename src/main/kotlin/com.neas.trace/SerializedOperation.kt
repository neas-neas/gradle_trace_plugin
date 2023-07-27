package com.neas.trace

internal interface SerializedOperation {
    fun toMap(): Map<String, *>
}