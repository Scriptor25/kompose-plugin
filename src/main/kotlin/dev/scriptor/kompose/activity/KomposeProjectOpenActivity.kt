package dev.scriptor.kompose.activity

import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import dev.scriptor.kompose.KomposeSettings
import dev.scriptor.kompose.KomposeSystemId

class KomposeProjectOpenActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val settings = KomposeSettings.getInstance(project)

        for (linkedProject in settings.linkedProjectsSettings) {
            ExternalSystemUtil.refreshProject(
                linkedProject.externalProjectPath,
                ImportSpecBuilder(
                    project,
                    KomposeSystemId,
                ),
            )
        }
    }
}
