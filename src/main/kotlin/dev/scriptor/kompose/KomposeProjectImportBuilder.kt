package dev.scriptor.kompose

import com.intellij.icons.AllIcons
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.service.project.ProjectDataManager
import com.intellij.openapi.externalSystem.service.project.wizard.AbstractExternalProjectImportBuilder
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import java.io.File
import javax.swing.Icon

class KomposeProjectImportBuilder : AbstractExternalProjectImportBuilder<ImportFromKomposeControl>(
    ProjectDataManager.getInstance(),
    {
        ImportFromKomposeControl(
            systemSettings = KomposeSettings.getInstance(ProjectManager.getInstance().defaultProject),
            projectSettings = KomposeProjectSettings(),
        )
    },
    KomposeSystemId,
) {

    override fun getName(): String = "Kompose"
    override fun getIcon(): Icon = AllIcons.Actions.Compile

    override fun doPrepare(context: WizardContext) {
        // TODO
    }

    override fun beforeCommit(dataNode: DataNode<ProjectData>, project: Project) {
        // TODO
    }

    override fun getExternalProjectConfigToUse(file: File): File {
        // TODO
        return file
    }

    override fun applyExtraSettings(context: WizardContext) {
        // TODO
    }
}
