package dev.scriptor.kompose

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.externalSystem.model.ExternalSystemException
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.externalSystem.util.task.TaskExecutionSpec
import com.intellij.openapi.project.Project
import com.intellij.platform.eel.EelApi
import com.intellij.platform.eel.EelExecApiHelpers
import com.intellij.platform.eel.ReadResult
import com.intellij.platform.eel.channels.EelReceiveChannel
import com.intellij.platform.eel.provider.getEelDescriptor
import com.intellij.platform.eel.provider.toEelApi
import com.intellij.platform.eel.provider.utils.EelProcessExecutionResult
import com.intellij.platform.eel.spawnProcess
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.file.Path
import kotlin.io.path.Path

val KomposeSystemId = ProjectSystemId(
    "kompose",
    "Kompose",
)

object KomposeDataKey {
    const val KEY = "KEY"
    const val TARGET_PLATFORM = "TARGET_PLATFORM"
    const val LANGUAGE_LEVEL = "LANGUAGE_LEVEL"
    const val API_LEVEL = "API_LEVEL"
    const val ADDITIONAL_ARGUMENTS = "ADDITIONAL_ARGUMENTS"
}

enum class KomposeTask {
    Clean,
    Compile,
    Launch,
    Package,
    PackageFatJar,
}

fun runKomposeTask(
    project: Project?,
    task: KomposeTask,
) {
    if (project == null)
        return

    val settings = ExternalSystemTaskExecutionSettings().apply {
        externalProjectPath = project.basePath
        externalSystemIdString = KomposeSystemId.id

        taskNames = listOf(task.name)
    }

    ExternalSystemUtil.runTask(
        TaskExecutionSpec
            .create()
            .withProject(project)
            .withSettings(settings)
            .withExecutorId(DefaultRunExecutor.EXECUTOR_ID)
            .withSystemId(KomposeSystemId)
            .build()
    )
}

private suspend fun readLines(
    channel: EelReceiveChannel,
    callback: (String) -> Unit,
): ByteArray {
    val buffer = ByteBuffer.allocate(8192)
    val output = ByteArrayOutputStream()
    val line = ByteArrayOutputStream()

    while (true) {
        buffer.clear()
        val result = channel.receive(buffer)
        buffer.flip()

        while (buffer.hasRemaining()) {
            val byte = buffer.get()
            output.write(byte.toInt())

            if (byte == '\n'.code.toByte()) {
                val bytes = line.toByteArray()
                val length = if (
                    bytes.isNotEmpty() &&
                    bytes.last() == '\r'.code.toByte()
                ) {
                    bytes.size - 1
                } else {
                    bytes.size
                }

                callback(
                    bytes.decodeToString(
                        endIndex = length
                    )
                )
                line.reset()
            } else {
                line.write(byte.toInt())
            }
        }

        if (result == ReadResult.EOF) {
            if (line.size() > 0) {
                callback(line.toByteArray().decodeToString())
            }
            return output.toByteArray()
        }
    }
}

suspend fun process(
    project: Project,
    exe: String,
    vararg args: String,
    stdout: (String) -> Unit = {},
    stderr: (String) -> Unit = {},
    success: () -> Unit = {},
    failure: (Exception) -> Unit = {},
    block: EelExecApiHelpers.SpawnProcess.() -> Unit = {},
): EelProcessExecutionResult {
    val eel = project.getEelDescriptor().toEelApi()

    return process(
        eel,
        exe,
        *args,
        stdout = stdout,
        stderr = stderr,
        success = success,
        failure = failure,
        block = block,
    )
}

suspend fun process(
    path: Path,
    exe: String,
    vararg args: String,
    stdout: (String) -> Unit = {},
    stderr: (String) -> Unit = {},
    success: () -> Unit = {},
    failure: (Exception) -> Unit = {},
    block: EelExecApiHelpers.SpawnProcess.() -> Unit = {},
): EelProcessExecutionResult {
    val eel = path.getEelDescriptor().toEelApi()

    return process(
        eel,
        exe,
        *args,
        stdout = stdout,
        stderr = stderr,
        success = success,
        failure = failure,
        block = block,
    )
}

suspend fun process(
    eel: EelApi,
    exe: String,
    vararg args: String,
    stdout: (String) -> Unit = {},
    stderr: (String) -> Unit = {},
    success: () -> Unit = {},
    failure: (Exception) -> Unit = {},
    block: EelExecApiHelpers.SpawnProcess.() -> Unit = {},
): EelProcessExecutionResult {
    val process = eel.exec
        .spawnProcess(exe, *args)
        .apply(block)
        .eelIt()

    val result = coroutineScope {
        val stdoutReader = async {
            readLines(process.stdout, stdout)
        }
        val stderrReader = async {
            readLines(process.stderr, stderr)
        }

        val exitCode = process.exitCode.await()
        val stdoutBytes = stdoutReader.await()
        val stderrBytes = stderrReader.await()

        EelProcessExecutionResult(
            exitCode,
            stdoutBytes,
            stderrBytes,
        )
    }

    if (result.exitCode == 0)
        success()
    else
        failure(ExternalSystemException("$exe [${args.joinToString(", ") { "'$it'" }}] failed with exit code ${result.exitCode}"))

    return result
}

private fun readLinesBlocking(
    stream: InputStream,
    callback: (String) -> Unit,
): ByteArray {
    val channel = Channels.newChannel(stream)

    val buffer = ByteBuffer.allocate(8192)
    val output = ByteArrayOutputStream()
    val line = ByteArrayOutputStream()

    while (true) {
        buffer.clear()
        val count = channel.read(buffer)
        buffer.flip()

        while (buffer.hasRemaining()) {
            val byte = buffer.get()
            output.write(byte.toInt())

            if (byte == '\n'.code.toByte()) {
                val bytes = line.toByteArray()
                val length = if (
                    bytes.isNotEmpty() &&
                    bytes.last() == '\r'.code.toByte()
                ) {
                    bytes.size - 1
                } else {
                    bytes.size
                }

                callback(
                    bytes.decodeToString(
                        endIndex = length
                    )
                )
                line.reset()
            } else {
                line.write(byte.toInt())
            }
        }

        if (count <= 0) {
            if (line.size() > 0) {
                callback(line.toByteArray().decodeToString())
            }
            return output.toByteArray()
        }
    }
}

private val wslUncPattern =
    Regex("""^[/\\]{2}wsl\.localhost[/\\]([^/\\]+)(?:[/\\](.*))?$""", RegexOption.IGNORE_CASE)

fun <T> maybeWslPath(path: String, t: (distribution: String, path: String) -> T, e: (path: String) -> T): T =
    when (val match = wslUncPattern.matchEntire(path)) {
        null -> e(path)
        else -> {
            val distribution = match.groupValues[1]
            val path = "/" + match.groupValues[2]
                .replace('\\', '/')

            t(distribution, path)
        }
    }

fun processBlocking(
    exe: String,
    vararg args: String,
    stdout: (String) -> Unit = {},
    stderr: (String) -> Unit = {},
    success: () -> Unit = {},
    failure: (Exception) -> Unit = {},
    workingDirectory: String,
): EelProcessExecutionResult {
    val commandLine = maybeWslPath(
        workingDirectory,
        { distribution, path ->
            System.err.println(
                "wsl command line: wsl.exe --distribution '$distribution' --cd '$path' --exec '$exe' ${
                    args.joinToString(
                        "' '",
                        "'",
                        "'",
                    )
                }"
            )

            GeneralCommandLine(
                "wsl.exe",
                "--distribution", distribution,
                "--cd", path,
                "--exec", exe,
                *args,
            )
        },
        { path ->
            GeneralCommandLine(exe, *args)
                .withWorkingDirectory(Path(path))
        },
    )

    val process = try {
        commandLine.createProcess()
    } catch (e: Exception) {
        e.printStackTrace()
        throw ExternalSystemException("failed to create process $exe [${args.joinToString(", ") { "'$it'" }}]: ${e.message}")
    }

    val result = run {
        lateinit var stdoutBytes: ByteArray
        lateinit var stderrBytes: ByteArray

        val stdoutReader = Thread {
            stdoutBytes = readLinesBlocking(process.inputStream, stdout)
        }.apply { start() }
        val stderrReader = Thread {
            stderrBytes = readLinesBlocking(process.errorStream, stderr)
        }.apply { start() }

        val exitCode = process.waitFor()

        stdoutReader.join()
        stderrReader.join()

        EelProcessExecutionResult(
            exitCode,
            stdoutBytes,
            stderrBytes,
        )
    }

    if (result.exitCode == 0)
        success()
    else
        failure(ExternalSystemException("$exe [${args.joinToString(", ") { "'$it'" }}] failed with exit code ${result.exitCode}"))

    return result
}
