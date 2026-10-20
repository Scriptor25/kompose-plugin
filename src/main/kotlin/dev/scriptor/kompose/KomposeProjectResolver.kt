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
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.platform.eel.provider.asEelPath
import com.intellij.platform.eel.provider.utils.stderrString
import com.intellij.platform.eel.provider.utils.stdoutString
import dev.scriptor.kompose.model.KomposeProject
import kotlinx.coroutines.runBlocking
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
        listener.onStatusChange(ExternalSystemTaskNotificationEvent(id, "Loading Kompose Project"))

        val model = ExternalSystemApiUtil.executeOnEdt {
            runBlocking { load(Path(projectPath)) }
        }

        return model.toNode(id.projectSystemId)
    }

    override fun cancelTask(
        taskId: ExternalSystemTaskId,
        listener: ExternalSystemTaskNotificationListener,
    ): Boolean {
        return false
    }

    private suspend fun load(path: Path): KomposeProject {
        val result = process(path, "kompose", "model", "resolve") {
            workingDirectory(path.asEelPath())
        }

        if (result.exitCode != 0)
            throw ExternalSystemException("'kompose' failed with exit code ${result.exitCode}:\n${result.stderrString}")

        val json = Json {
            ignoreUnknownKeys = true
        }

        return json.decodeFromString(result.stdoutString)
    }
}
