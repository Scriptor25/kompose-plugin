package dev.scriptor.kompose

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.externalSystem.service.project.wizard.AbstractExternalProjectImportProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class KomposeProjectImportProvider : AbstractExternalProjectImportProvider(KomposeSystemId) {

    override fun createSteps(context: WizardContext): Array<out ModuleWizardStep> {
        return ModuleWizardStep.EMPTY_ARRAY
    }

    override fun canImport(fileOrDirectory: VirtualFile, project: Project?): Boolean {
        val root = when {
            fileOrDirectory.isDirectory -> fileOrDirectory
            fileOrDirectory.name == "project.toml" -> fileOrDirectory.parent
            else -> null
        } ?: return false

        return root.findChild("project.toml") != null
    }

    override fun canImportFromFile(file: VirtualFile): Boolean {
        return file.name == "project.toml"
    }
}
