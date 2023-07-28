package com.neas.trace

import com.neas.trace.tools.Converter
import groovy.json.JsonOutput
import org.gradle.internal.UncheckedException
import org.gradle.internal.operations.*
import org.gradle.internal.operations.notify.BuildOperationFinishedNotification
import org.gradle.internal.operations.notify.BuildOperationNotificationListener
import org.gradle.internal.operations.notify.BuildOperationProgressNotification
import org.gradle.internal.operations.notify.BuildOperationStartedNotification
import org.gradle.launcher.exec.RunBuildBuildOperationType
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.ArrayList

class BuildOperationTraceListener(private val rootDir: String) : BuildOperationNotificationListener {

    private val logOutputStream by lazy {
        BufferedOutputStream(FileOutputStream(jsonFile()))
    }

    private fun jsonFile(): File {
        val f = File(rootDir, "build/trace/buildOpTrace.json")
        if (!f.exists()) {
            f.parentFile.mkdirs()
            f.createNewFile()
        }
        return f
    }

    private val operationList = ArrayList<SerializedOperation>()

    @Synchronized
    override fun started(startedNotification: BuildOperationStartedNotification) {
        operationList.add(
            SerializedOperationStart(
                (startedNotification.notificationOperationId as OperationIdentifier).id,
                (startedNotification.notificationOperationParentId as? OperationIdentifier)?.id,
                "",
                startedNotification.notificationOperationStartedTimestamp,
                startedNotification.notificationOperationDetails
            )
        )
    }

    @Synchronized
    override fun progress(progressNotification: BuildOperationProgressNotification) {
        operationList.add(
            SerializedOperationProgress(
                (progressNotification.notificationOperationId as OperationIdentifier).id,
                progressNotification.notificationOperationProgressTimestamp,
                progressNotification.notificationOperationProgressDetails
            )
        )
    }

    @Synchronized
    override fun finished(finishedNotification: BuildOperationFinishedNotification) {
        operationList.add(
            SerializedOperationFinish(
                (finishedNotification.notificationOperationId as OperationIdentifier).id,
                finishedNotification.notificationOperationFinishedTimestamp,
                finishedNotification.notificationOperationResult,
                finishedNotification.notificationOperationFailure
            )
        )
        if (finishedNotification.notificationOperationDetails is RunBuildBuildOperationType.Details) {
            val roots = toTreeRoot(operationList)
            writeDetailTree(roots)
            Converter(jsonFile()).run()
        }
    }

    private fun toTreeRoot(list: List<SerializedOperation>): List<BuildOperationRecord> {
        val roots: ArrayList<BuildOperationRecord> = ArrayList()
        val pendings: HashMap<Any, PendingOperation> = HashMap()
        val childrens: HashMap<Any, LinkedList<BuildOperationRecord>> = HashMap()

        for (serialized in list) {
            if (serialized is SerializedOperationStart) {
                pendings[serialized.id] = PendingOperation(serialized)
                childrens[serialized.id] = LinkedList<BuildOperationRecord>()
            } else if (serialized is SerializedOperationProgress) {
                val pending =
                    pendings[serialized.id] ?: error("did not find owner of progress event with ID " + serialized.id)
                pending.progress.add(serialized)
            } else {
                val finish = serialized as SerializedOperationFinish
                val pending = pendings.remove(finish.id)!!
                val children = childrens.remove(finish.id)!!
                val start = pending.start

                val details = start.details
                val result = finish.result

                val progresses = ArrayList<BuildOperationRecord.Progress>()
                for (progress in pending.progress) {
                    val progressDetailsMap = progress.details
                    progresses.add(
                        BuildOperationRecord.Progress(
                            progress.time,
                            progressDetailsMap,
                            progress.detailsClassName
                        )
                    )
                }
                val record = BuildOperationRecord(
                    start.id,
                    start.parentId,
                    start.displayName,
                    start.startTime,
                    finish.endTime,
                    details,
                    start.detailsClassName,
                    result,
                    finish.resultClassName,
                    finish.failureMsg,
                    progresses,
                    children.sortedWith(kotlin.Comparator { t1, t2 ->
                        if (t1.id == t2.id) {
                            if (t1.startTime >= t2.startTime) {
                                return@Comparator 1
                            } else {
                                return@Comparator -1
                            }
                        }
                        if (t1.id > t2.id) {
                            return@Comparator 1
                        } else {
                            return@Comparator -1
                        }
                    })
                )
                if (start.parentId == null) {
                    roots.add(record)
                } else {
                    val parentChildren =
                        childrens[start.parentId] ?: error("parentChildren != null from ${jsonFile()}")
                    parentChildren.add(record)
                }
            }
        }
        return roots
    }

    internal class PendingOperation(val start: SerializedOperationStart) {
        val progress: ArrayList<SerializedOperationProgress> = ArrayList()
    }

    private fun writeDetailTree(roots: List<BuildOperationRecord>) {
        val currentThread = Thread.currentThread()
        val previousClassLoader = currentThread.contextClassLoader
        currentThread.contextClassLoader = JsonOutput::class.java.classLoader
        try {
            val list = LinkedList<Map<*, *>>()
            roots.forEach {
                list.add(it.toSerializable())
            }
            val json = JsonOutput.toJson(list)
            try {
                synchronized(logOutputStream) {
                    logOutputStream.write(json.toByteArray(StandardCharsets.UTF_8))
                    logOutputStream.flush()
                }
            } catch (e: IOException) {
                throw UncheckedException.throwAsUncheckedException(e)
            }
        } catch (e: OutOfMemoryError) {
            System.err.println("Failed to write build operation trace JSON due to out of memory.")
        } finally {
            currentThread.contextClassLoader = previousClassLoader
        }
    }

}