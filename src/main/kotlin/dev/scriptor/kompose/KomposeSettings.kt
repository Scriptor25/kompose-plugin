package dev.scriptor.kompose

import com.intellij.openapi.Disposable
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemSettings
import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic

class KomposeSettings(
    topic: Topic<KomposeSettingsListener>,
    project: Project,
) : AbstractExternalSystemSettings<KomposeSettings, KomposeProjectSettings, KomposeSettingsListener>(
    topic,
    project,
) {
    override fun subscribe(
        listener: ExternalSystemSettingsListener<KomposeProjectSettings>,
        parentDisposable: Disposable,
    ) {
        val connection = project.messageBus.connect(parentDisposable)

        connection.subscribe(
            KomposeSettingsListener.TOPIC,
            object : KomposeSettingsListener {
                override fun onBulkChangeStart() =
                    listener.onBulkChangeStart()

                override fun onBulkChangeEnd() =
                    listener.onBulkChangeEnd()

                override fun onProjectRenamed(oldName: String, newName: String) =
                    listener.onProjectRenamed(oldName, newName)

                override fun onProjectsLoaded(settings: Collection<KomposeProjectSettings>) =
                    listener.onProjectsLoaded(settings)

                override fun onProjectsLinked(settings: Collection<KomposeProjectSettings>) =
                    listener.onProjectsLinked(settings)

                override fun onProjectsUnlinked(linkedProjectPaths: Set<String>) =
                    listener.onProjectsUnlinked(linkedProjectPaths)
            },
        )
    }

    override fun copyExtraSettingsFrom(settings: KomposeSettings) {
        // noop
    }

    override fun checkSettings(
        old: KomposeProjectSettings,
        current: KomposeProjectSettings,
    ) {
        // noop
    }
}
