package dev.scriptor.kompose.model

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.model.project.ContentRootData
import com.intellij.openapi.externalSystem.model.project.LibraryData
import com.intellij.openapi.externalSystem.model.project.ModuleData
import com.intellij.openapi.externalSystem.model.project.ProjectData
import dev.scriptor.kompose.KomposeDataKey
import kotlinx.serialization.Serializable
import kotlin.io.path.Path
import kotlin.io.path.div

@Serializable
data class KomposeProject(
    val name: String,
    val path: String,
    val repositories: KomposeRepositories,
    val modules: Map<String, KomposeModule>,
) {
    companion object {
        const val KEY = "PROJECT"
    }

    fun toNode(
        owner: ProjectSystemId,
        projectPath: String,
    ): DataNode<ProjectData> {
        val projectNode = DataNode(
            ProjectKeys.PROJECT,
            ProjectData(
                owner,
                name,
                projectPath,
                projectPath,
            ),
            null,
        )

        val rootModuleNode = if (modules.none { (_, v) -> v.path == "." }) {
            val moduleFileDirectoryPath = Path(projectPath) / ".idea" / "kompose"
            val externalConfigPath = Path(projectPath) / "project.toml"

            val moduleNode = projectNode.createChild(
                ProjectKeys.MODULE,
                ModuleData(
                    name,
                    owner,
                    "JAVA_MODULE",
                    name,
                    moduleFileDirectoryPath.toString(),
                    externalConfigPath.toString(),
                ).apply {
                    setProperty(KomposeDataKey.KEY, KEY)
                }
            )

            moduleNode.createChild(
                ProjectKeys.CONTENT_ROOT,
                ContentRootData(
                    owner,
                    projectPath,
                ),
            )

            moduleNode
        } else null

        val moduleNodes = modules.values
            .associate {
                it.name to it.toNode(
                    owner,
                    projectPath,
                    rootModuleNode ?: projectNode,
                )
            }

        val libraryNodes = mutableMapOf<String, DataNode<LibraryData>>()

        modules.values
            .forEach {
                it.addDependencies(
                    owner,
                    projectPath,
                    projectNode,
                    moduleNodes,
                    libraryNodes,
                )
            }

        return projectNode
    }
}
