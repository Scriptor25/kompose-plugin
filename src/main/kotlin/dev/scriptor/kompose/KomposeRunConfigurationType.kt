package dev.scriptor.kompose

import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.SimpleConfigurationType
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue

class KomposeRunConfigurationType : SimpleConfigurationType(
    "Kompose",
    "Kompose",
    "Run Kompose Application",
    NotNullLazyValue.lazy { AllIcons.Actions.Execute },
) {

    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return KomposeRunConfiguration(project, this)
    }
}
