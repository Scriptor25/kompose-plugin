package dev.scriptor.kompose

import com.intellij.openapi.externalSystem.util.ExternalSystemSettingsControl
import com.intellij.openapi.externalSystem.util.PaintAwarePanel

class KomposeSettingsControl(
    val settings: KomposeSettings,
) : ExternalSystemSettingsControl<KomposeSettings> {

    override fun fillUi(canvas: PaintAwarePanel, indentLevel: Int) {
        // TODO
    }

    override fun reset() {
        // TODO
    }

    override fun isModified(): Boolean {
        // TODO
        return false
    }

    override fun apply(settings: KomposeSettings) {
        // TODO
        this.settings.linkedProjectsSettings = settings.linkedProjectsSettings
    }

    override fun validate(settings: KomposeSettings): Boolean {
        // TODO
        return true
    }

    override fun disposeUIResources() {
        // TODO
    }

    override fun showUi(show: Boolean) {
        // TODO
    }
}
