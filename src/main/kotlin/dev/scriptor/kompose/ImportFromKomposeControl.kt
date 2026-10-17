package dev.scriptor.kompose

import com.intellij.openapi.externalSystem.service.settings.AbstractImportFromExternalSystemControl

class ImportFromKomposeControl(
    systemSettings: KomposeSettings,
    projectSettings: KomposeProjectSettings,
    showProjectFormatPanel: Boolean = false,
) : AbstractImportFromExternalSystemControl<
        KomposeProjectSettings,
        KomposeSettingsListener,
        KomposeSettings>(
    KomposeSystemId,
    systemSettings,
    projectSettings,
    showProjectFormatPanel,
) {

    override fun onLinkedProjectPathChange(path: String) {
        // TODO
    }

    override fun createProjectSettingsControl(settings: KomposeProjectSettings): KomposeProjectSettingsControl {
        return KomposeProjectSettingsControl(null, settings)
    }

    override fun createSystemSettingsControl(settings: KomposeSettings): KomposeSettingsControl {
        return KomposeSettingsControl(settings)
    }
}
