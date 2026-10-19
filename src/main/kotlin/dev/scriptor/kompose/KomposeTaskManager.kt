package dev.scriptor.kompose

import com.intellij.execution.process.ProcessOutputType
import com.intellij.openapi.externalSystem.model.settings.ExternalSystemExecutionSettings
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationEvent
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager
import com.intellij.platform.eel.provider.asEelPath
import kotlinx.coroutines.runBlocking
import kotlin.io.path.Path

class KomposeTaskManager : ExternalSystemTaskManager<ExternalSystemExecutionSettings> {

    private fun argsFor(task: KomposeTask): Array<String> =
        when (task) {
            KomposeTask.Clean ->
                arrayOf("clean")

            KomposeTask.Compile ->
                arrayOf("compile")

            KomposeTask.Launch ->
                arrayOf("launch")

            KomposeTask.Package ->
                arrayOf("package")

            KomposeTask.PackageFatJar ->
                arrayOf("package", "--fat")
        }

    private suspend fun executeTask(
        projectPath: String,
        id: ExternalSystemTaskId,
        settings: ExternalSystemExecutionSettings,
        listener: ExternalSystemTaskNotificationListener,
        taskName: String,
    ) {
        val task = KomposeTask.valueOf(taskName)

        listener.onStart(projectPath, id)

        listener.onStatusChange(
            ExternalSystemTaskNotificationEvent(
                id,
                "Running Kompose $taskName",
            ),
        )

        val path = Path(projectPath)

        process(
            path,
            "kompose",
            *argsFor(task),
            stdout = {
                listener.onTaskOutput(
                    id,
                    "$it\n",
                    ProcessOutputType.STDOUT,
                )
            },
            stderr = {
                listener.onTaskOutput(
                    id,
                    "$it\n",
                    ProcessOutputType.STDERR,
                )
            },
            success = {
                listener.onSuccess(projectPath, id)
            },
            failure = { exception ->
                listener.onFailure(projectPath, id, exception)
            },
        ) {
            workingDirectory(path.asEelPath())
        }
    }

    override fun executeTasks(
        projectPath: String,
        id: ExternalSystemTaskId,
        settings: ExternalSystemExecutionSettings,
        listener: ExternalSystemTaskNotificationListener,
    ) {
        runBlocking {
            for (taskName in settings.tasks) {
                executeTask(
                    projectPath,
                    id,
                    settings,
                    listener,
                    taskName,
                )
            }
        }
    }

    override fun cancelTask(
        id: ExternalSystemTaskId,
        listener: ExternalSystemTaskNotificationListener,
    ): Boolean {
        return false
    }
}
