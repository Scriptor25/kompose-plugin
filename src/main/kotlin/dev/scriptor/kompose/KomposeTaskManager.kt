package dev.scriptor.kompose

import com.intellij.execution.process.ProcessOutputType
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationEvent
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager
import kotlin.io.path.Path

class KomposeTaskManager : ExternalSystemTaskManager<KomposeExecutionSettings> {

    private fun commandFor(task: KomposeTask): List<String> =
        when (task) {
            KomposeTask.CLEAN ->
                listOf("kompose", "clean")

            KomposeTask.COMPILE ->
                listOf("kompose", "compile")

            KomposeTask.LAUNCH ->
                listOf("kompose", "launch")

            KomposeTask.PACKAGE ->
                listOf("kompose", "package")

            KomposeTask.PACKAGE_FAT_JAR ->
                listOf("kompose", "package", "--fat")
        }

    private fun executeTask(
        projectPath: String,
        id: ExternalSystemTaskId,
        settings: KomposeExecutionSettings,
        listener: ExternalSystemTaskNotificationListener,
        taskName: String,
    ) {
        val task = KomposeTask.valueOf(taskName)

        listener.onStatusChange(
            ExternalSystemTaskNotificationEvent(
                id,
                "Running Kompose $taskName",
            ),
        )

        process(
            commandFor(task),
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
            directory(Path(projectPath).toFile())
        }
    }

    override fun executeTasks(
        projectPath: String,
        id: ExternalSystemTaskId,
        settings: KomposeExecutionSettings,
        listener: ExternalSystemTaskNotificationListener,
    ) {
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

    override fun cancelTask(
        id: ExternalSystemTaskId,
        listener: ExternalSystemTaskNotificationListener,
    ): Boolean {
        return false
    }
}
