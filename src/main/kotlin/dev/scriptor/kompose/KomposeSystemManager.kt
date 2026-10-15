package dev.scriptor.kompose

import com.intellij.execution.configurations.SimpleJavaParameters
import com.intellij.openapi.externalSystem.ExternalSystemManager
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.model.settings.ExternalSystemExecutionSettings
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver
import com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.util.Function

class KomposeSystemManager :
    ExternalSystemManager<KomposeProjectSettings, KomposeSettingsListener, KomposeSettings, KomposeLocalSettings, ExternalSystemExecutionSettings> {

    override fun getSystemId(): ProjectSystemId = KomposeSystemId

    override fun getSettingsProvider(): Function<Project, KomposeSettings> =
        { project -> KomposeSettings.getInstance(project) }

    override fun getLocalSettingsProvider(): Function<Project, KomposeLocalSettings> =
        { project ->
            KomposeLocalSettings(
                systemId,
                project,
                KomposeLocalSettingsState(),
            )
        }

    override fun getExecutionSettingsProvider(): Function<Pair<Project, String>, ExternalSystemExecutionSettings> =
        { ExternalSystemExecutionSettings() }

    override fun getProjectResolverClass(): Class<out ExternalSystemProjectResolver<ExternalSystemExecutionSettings>> =
        KomposeProjectResolver::class.java

    override fun getTaskManagerClass(): Class<out ExternalSystemTaskManager<ExternalSystemExecutionSettings>> =
        KomposeTaskManager::class.java

    override fun getExternalProjectDescriptor(): FileChooserDescriptor =
        FileChooserDescriptorFactory.createSingleFolderDescriptor()

    override fun enhanceRemoteProcessing(parameters: SimpleJavaParameters) {
        // noop
    }
}
