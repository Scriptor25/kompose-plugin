package dev.scriptor.kompose;

import com.intellij.icons.AllIcons
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.ide.wizard.*
import com.intellij.ide.wizard.NewProjectWizardChainStep.Companion.nextStep
import javax.swing.Icon

class KomposeNewProjectWizard : GeneratorNewProjectWizard {

    override val id: String = "kompose"
    override val name: String = "Kompose"
    override val icon: Icon = AllIcons.Stub

    override fun createStep(context: WizardContext): NewProjectWizardStep {
        return RootNewProjectWizardStep(context)
            .nextStep(::NewProjectWizardBaseStep)
            .nextStep(::GitNewProjectWizardStep)
            .nextStep(::KomposeNewProjectWizardStep)
    }
}
