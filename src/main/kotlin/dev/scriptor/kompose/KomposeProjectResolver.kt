package dev.scriptor.kompose

import com.intellij.openapi.externalSystem.importing.ProjectResolverPolicy
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ExternalSystemException
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.model.settings.ExternalSystemExecutionSettings
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationEvent
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver
import dev.scriptor.kompose.model.KomposeProject
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.Path

class KomposeProjectResolver : ExternalSystemProjectResolver<ExternalSystemExecutionSettings> {

    override fun resolveProjectInfo(
        id: ExternalSystemTaskId,
        projectPath: String,
        isPreviewMode: Boolean,
        settings: ExternalSystemExecutionSettings?,
        resolverPolicy: ProjectResolverPolicy?,
        listener: ExternalSystemTaskNotificationListener,
    ): DataNode<ProjectData> {
        listener.onStatusChange(ExternalSystemTaskNotificationEvent(id, "Loading Kompose project"))

        val model = load(Path(projectPath))

        return model.toNode(id.projectSystemId)
    }

    override fun cancelTask(
        taskId: ExternalSystemTaskId,
        listener: ExternalSystemTaskNotificationListener,
    ): Boolean {
        return false
    }

    private fun load(path: Path): KomposeProject {
        val process = ProcessBuilder("kompose", "model", "resolve")
            .directory(path.toFile())
            .start()

        val output = process.inputStream
            .bufferedReader()
            .readText()
        val error = process.errorStream
            .bufferedReader()
            .readText()

        val exitCode = process.waitFor()

        if (exitCode != 0)
            throw ExternalSystemException("'kompose' failed with exit code $exitCode:\n$error")

        val json = Json {
            ignoreUnknownKeys = true
        }

        return json.decodeFromString(output)
    }
}
