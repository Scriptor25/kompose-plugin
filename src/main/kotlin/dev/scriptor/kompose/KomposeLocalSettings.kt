package dev.scriptor.kompose

import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemLocalSettings
import com.intellij.openapi.project.Project

class KomposeLocalSettings(
    externalSystemId: ProjectSystemId,
    project: Project,
    state: KomposeLocalSettingsState,
) : AbstractExternalSystemLocalSettings<KomposeLocalSettingsState>(
    externalSystemId,
    project,
    state,
)
