package dev.scriptor.kompose

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.externalSystem.service.project.wizard.AbstractExternalProjectImportProvider
import com.intellij.openapi.vfs.VirtualFile

class KomposeProjectImportProvider : AbstractExternalProjectImportProvider(KomposeSystemId) {

    override fun createSteps(context: WizardContext): Array<out ModuleWizardStep> {
        return ModuleWizardStep.EMPTY_ARRAY
    }

    override fun canImportFromFile(file: VirtualFile): Boolean {
        // TODO
        return file.name == "project.toml"
    }
}
