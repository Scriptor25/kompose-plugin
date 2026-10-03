package dev.scriptor.kompose

import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.externalSystem.model.ExternalSystemException
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.externalSystem.util.task.TaskExecutionSpec
import com.intellij.openapi.project.Project
import java.io.InputStream

val KomposeSystemId = ProjectSystemId(
    "dev.scriptor.kompose",
    "Kompose",
)

enum class KomposeTask {
    CLEAN,
    COMPILE,
    LAUNCH,
    PACKAGE,
    PACKAGE_FAT_JAR,
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

fun pipe(stream: InputStream, action: (String) -> Unit): Thread {
    return Thread { stream.bufferedReader().forEachLine(action) }
}

fun process(
    command: List<String>,
    stdout: (String) -> Unit = {},
    stderr: (String) -> Unit = {},
    success: () -> Unit = {},
    failure: (Exception) -> Unit = {},
    block: ProcessBuilder.() -> Unit,
) {
    val process = ProcessBuilder(command)
        .apply(block)
        .start()

    val output = pipe(process.inputStream, stdout)

    val error = pipe(process.errorStream, stderr)

    output.start()
    error.start()

    val exitCode = process.waitFor()

    output.join()
    error.join()

    if (exitCode == 0)
        success()
    else
        failure(ExternalSystemException("'${command.joinToString(" ")}' failed with exit code $exitCode"))
}
