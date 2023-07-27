package com.neas.trace.tools

import com.google.gson.Gson
import java.io.File

enum class OutputFormat(val filePostfix: String) {
    ANALYZER("-analyzer.txt");
}

class Converter(val buildOperationTrace: File) {

    fun run() {
        val slice = readTraceSlice(buildOperationTrace)
        val outputFile = outputFile(buildOperationTrace, OutputFormat.ANALYZER)
        convertToAnalyzeResult(outputFile, slice)
    }

    private fun outputFile(traceFile: File, format: OutputFormat): File {
        return File(traceFile.parentFile, traceFile.nameWithoutExtension + format.filePostfix)
    }

    private fun convertToAnalyzeResult(outputFile: File, slice: BuildOperationTraceSlice) {
        BuildOperationTraceAnalyzer().convert(slice, outputFile)
    }

    private fun readTraceSlice(traceFile: File): BuildOperationTraceSlice {
        val records = readBuildOperationTrace(traceFile)
        println("Read ${records.size} build operation tree roots from ${traceFile.name}")
        return BuildOperationTraceSlice(records.toList())
    }

    private fun readBuildOperationTrace(traceJsonFile: File): Array<BuildOperationRecord> {
        val inputTraceJsonText = traceJsonFile.readText()
        try {
            return Gson().fromJson(inputTraceJsonText, Array<BuildOperationRecord>::class.java)
        } catch (e: Exception) {
            throw RuntimeException("Failed to read build operation trace file: ${traceJsonFile.absolutePath}", e)
        }
    }

}
