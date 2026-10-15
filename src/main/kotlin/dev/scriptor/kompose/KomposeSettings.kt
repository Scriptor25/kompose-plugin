package dev.scriptor.kompose

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemSettings
import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener
import com.intellij.openapi.project.Project

@State(
    name = "KomposeSettings",
    storages = [Storage("kompose.xml")],
)
class KomposeSettings(
    project: Project,
) : AbstractExternalSystemSettings<KomposeSettings, KomposeProjectSettings, KomposeSettingsListener>(
    KomposeSettingsListener.TOPIC,
    project,
), PersistentStateComponent<KomposeSettings.State> {

    companion object {
        fun getInstance(project: Project): KomposeSettings =
            project.getService(KomposeSettings::class.java)
    }

    class State : AbstractExternalSystemSettings.State<KomposeProjectSettings> {

        var settings: Set<KomposeProjectSettings>? = null

        override fun getLinkedExternalProjectsSettings(): Set<KomposeProjectSettings>? {
            return settings
        }

        override fun setLinkedExternalProjectsSettings(settings: Set<KomposeProjectSettings>) {
            this.settings = settings
        }
    }

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

    override fun getState(): State {
        return State().also(::fillState)
    }

    override fun loadState(state: State) {
        super.loadState(state)
    }
}
