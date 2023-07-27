package com.neas.trace

class BuildOperationRecord(
    val id: Long,
    val parentId: Long?,
    val displayName: String?,
    val startTime: Long,
    val endTime: Long,
    val details: Any?,
    private val detailsClassName: String?,
    val result: Any?,
    private val resultClassName: String?,
    val failure: String?,
    val progress: List<Progress>,
    val children: List<BuildOperationRecord>
) {
    fun toSerializable(): Map<String, *> {
        val map: MutableMap<String, Any?> = LinkedHashMap()
        map["displayName"] = displayName
        map["id"] = id
        if (parentId != null) {
            map["parentId"] = parentId
        }
        map["startTime"] = startTime
        map["endTime"] = endTime
        map["duration"] = endTime - startTime
        if (details != null) {
            map["details"] = details
            map["detailsClassName"] = detailsClassName
        }
        if (result != null) {
            map["result"] = result
            map["resultClassName"] = resultClassName
        }
        if (failure != null) {
            map["failure"] = failure
        }
        if (progress.isNotEmpty()) {
            map["progress"] = progress.map {
                it.toSerializable()
            }
        }
        if (children.isNotEmpty()) {
            map["children"] = children.map {
                it.toSerializable()
            }
        }
        return map
    }

    @Throws(ClassNotFoundException::class)
    fun hasDetailsOfType(clazz: Class<*>): Boolean {
        val detailsType = detailsType
        return detailsType != null && clazz.isAssignableFrom(detailsType)
    }

    @get:Throws(ClassNotFoundException::class)
    val detailsType: Class<*>?
        get() = if (detailsClassName == null) null else javaClass.classLoader.loadClass(detailsClassName)

    @get:Throws(ClassNotFoundException::class)
    val resultType: Class<*>?
        get() = if (resultClassName == null) null else javaClass.classLoader.loadClass(resultClassName)

    override fun toString(): String {
        return "BuildOperationRecord{$id->$displayName}"
    }

    class Progress(
        val time: Long,
        val details: Any?,
        val detailsClassName: String?
    ) {
        fun toSerializable(): Map<String?, *> {
            val map: MutableMap<String?, Any?> = LinkedHashMap()
            map["time"] = time
            if (details != null) {
                map["details"] = details
                map["detailsClassName"] = detailsClassName
            }
            return map
        }

        @get:Throws(ClassNotFoundException::class)
        val detailsType: Class<*>?
            get() = if (detailsClassName == null) null else javaClass.classLoader.loadClass(detailsClassName)

        @Throws(ClassNotFoundException::class)
        fun hasDetailsOfType(clazz: Class<*>): Boolean {
            val detailsType = detailsType
            return detailsType != null && clazz.isAssignableFrom(detailsType)
        }

        override fun toString(): String {
            return "Progress{details=$details, detailsClassName='$detailsClassName'}"
        }
    }
}