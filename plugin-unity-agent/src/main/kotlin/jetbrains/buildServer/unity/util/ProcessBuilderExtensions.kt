package jetbrains.buildServer.unity.util

import java.io.BufferedReader
import java.util.concurrent.TimeUnit

sealed interface ProcessExecutionResult
object Timeout : ProcessExecutionResult
class Error(val exception: Throwable) : ProcessExecutionResult
class Completed(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
) : ProcessExecutionResult

fun ProcessBuilder.execute(timeoutSeconds: Long = 3): ProcessExecutionResult {
    try {
        val process = this.start()
        return if (process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
            val stdout = process.inputStream
                .bufferedReader(Charsets.UTF_8)
                .use(BufferedReader::readText)

            val stderr = process.errorStream
                .bufferedReader(Charsets.UTF_8)
                .use(BufferedReader::readText)

            Completed(process.exitValue(), stdout, stderr)
        } else {
            process.destroyForcibly()
            Timeout
        }
    } catch (e: Throwable) {
        return Error(e)
    }
}
