package dev.scriptor.kompose

import com.intellij.openapi.externalSystem.service.settings.AbstractExternalProjectSettingsControl
import com.intellij.openapi.externalSystem.util.PaintAwarePanel
import com.intellij.openapi.project.Project

class KomposeProjectSettingsControl(
    project: Project?,
    initialSettings: KomposeProjectSettings,
) : AbstractExternalProjectSettingsControl<KomposeProjectSettings>(project, initialSettings) {

    val settings = initialSettings.clone()

    override fun fillExtraControls(content: PaintAwarePanel, indentLevel: Int) {
        // TODO
    }

    override fun isExtraSettingModified(): Boolean {
        // TODO
        return false
    }

    override fun resetExtraSettings(isDefaultModuleCreation: Boolean) {
        // TODO
    }

    override fun applyExtraSettings(settings: KomposeProjectSettings) {
        // TODO
    }

    override fun validate(settings: KomposeProjectSettings): Boolean {
        // TODO
        return true
    }
}
