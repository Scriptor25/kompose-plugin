package dev.scriptor.kompose.model

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.model.project.LibraryData
import com.intellij.openapi.externalSystem.model.project.ProjectData
import kotlinx.serialization.Serializable

@Serializable
data class KomposeProject(
    val name: String,
    val path: String,
    val repositories: KomposeRepositories,
    val modules: Map<String, KomposeModule>,
) {
    fun toNode(owner: ProjectSystemId): DataNode<ProjectData> {
        val projectNode = DataNode(
            ProjectKeys.PROJECT,
            ProjectData(
                owner,
                name,
                path,
                path,
            ),
            null,
        )

        val moduleNodes = modules.values
            .associate { it.name to it.toNode(owner, this, projectNode) }

        val libraryNodes = mutableMapOf<String, DataNode<LibraryData>>()

        modules.values
            .forEach {
                it.addDependencies(
                    owner,
                    this,
                    projectNode,
                    moduleNodes,
                    libraryNodes,
                )
            }

        return projectNode
    }
}
