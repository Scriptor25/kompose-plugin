package dev.scriptor.kompose

import com.intellij.openapi.Disposable
import com.intellij.openapi.externalSystem.autolink.ExternalSystemProjectLinkListener
import com.intellij.openapi.externalSystem.autolink.ExternalSystemUnlinkedProjectAware
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class KomposeUnlinkedProjectAware : ExternalSystemUnlinkedProjectAware {

    override val systemId: ProjectSystemId
        get() = KomposeSystemId

    override fun isBuildFile(
        project: Project,
        buildFile: VirtualFile,
    ): Boolean {
        return buildFile.name == "project.toml"
    }

    override fun isLinkedProject(
        project: Project,
        externalProjectPath: String,
    ): Boolean {
        val settings = ExternalSystemApiUtil.getSettings(project, systemId)

        return settings.getLinkedProjectSettings(externalProjectPath) != null
    }

    override fun linkAndLoadProject(
        project: Project,
        externalProjectPath: String,
    ) {
        val settings = KomposeProjectSettings().apply {
            this.externalProjectPath = externalProjectPath
        }

        ExternalSystemUtil.linkExternalProject(
            settings,
            ImportSpecBuilder(
                project,
                systemId,
            ).build(),
        )
    }

    override suspend fun unlinkProject(
        project: Project,
        externalProjectPath: String,
    ) {
        val settings = ExternalSystemApiUtil.getSettings(project, systemId)

        settings.unlinkExternalProject(externalProjectPath)
    }

    override fun subscribe(
        project: Project,
        listener: ExternalSystemProjectLinkListener,
        parentDisposable: Disposable,
    ) {
        val connection = project.messageBus.connect(parentDisposable)

        connection.subscribe(
            KomposeSettingsListener.TOPIC,
            object : KomposeSettingsListener {

                override fun onProjectsLinked(settings: Collection<KomposeProjectSettings>) {
                    settings.forEach { listener.onProjectLinked(it.externalProjectPath) }
                }

                override fun onProjectsUnlinked(linkedProjectPaths: Set<String>) {
                    linkedProjectPaths.forEach { listener.onProjectUnlinked(it) }
                }
            },
        )
    }
}
