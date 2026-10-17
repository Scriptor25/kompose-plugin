package dev.scriptor.kompose

import com.intellij.ide.impl.OpenProjectTask
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.projectImport.ProjectOpenProcessor
import java.nio.file.Files

class KomposeProjectOpenProcessor : ProjectOpenProcessor() {

    override val name: String = "Kompose"

    override fun canOpenProject(file: VirtualFile): Boolean {
        val root = when {
            file.isDirectory -> file
            file.name == "project.toml" -> file.parent
            else -> null
        } ?: return false

        return root.findChild("project.toml") != null
    }

    override suspend fun openProjectAsync(virtualFile: VirtualFile, projectOpenOptions: ProjectOpenOptions): Project? {
        val root = when {
            virtualFile.isDirectory -> virtualFile
            virtualFile.name == "project.toml" -> virtualFile.parent
            else -> null
        } ?: return null

        if (root.findChild("project.toml") == null) return null

        val projectPath = root.toNioPath()
        val project = ProjectManagerEx.getInstanceEx().openProjectAsync(
            projectPath,
            OpenProjectTask {
                projectToClose = projectOpenOptions.projectToClose
                forceOpenInNewFrame = projectOpenOptions.forceOpenInNewFrame
                isNewProject = !Files.exists(projectPath.resolve(".idea"))
            },
        ) ?: return null

        val settings = KomposeProjectSettings().apply {
            externalProjectPath = root.path
        }

        ExternalSystemUtil.linkExternalProject(
            settings,
            ImportSpecBuilder(
                project,
                KomposeSystemId,
            ).build(),
        )

        return project
    }
}
