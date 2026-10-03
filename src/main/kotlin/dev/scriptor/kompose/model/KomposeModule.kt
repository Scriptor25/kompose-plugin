package dev.scriptor.kompose.model

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.model.project.LibraryData
import com.intellij.openapi.externalSystem.model.project.ModuleData
import com.intellij.openapi.externalSystem.model.project.ProjectData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.io.path.Path
import kotlin.io.path.div

@Serializable
data class KomposeModule(
    val name: String,
    val path: String,
    val source: String,
    val build: String,
    @SerialName("source_sets")
    val sourceSets: Map<String, KomposeSourceSet>,
    val data: KomposeModuleData,
) {
    fun toNode(
        owner: ProjectSystemId,
        project: KomposeProject,
        projectNode: DataNode<ProjectData>,
    ): DataNode<ModuleData> {
        val ideDirectory = Path(project.path) / ".idea" / "kompose"
        val directory = Path(project.path) / path

        val moduleNode = projectNode.createChild(
            ProjectKeys.MODULE,
            ModuleData(
                name,
                owner,
                "JAVA_MODULE",
                name,
                ideDirectory.toString(),
                directory.toString(),
            ),
        )

        sourceSets.values.forEach {
            it.toNode(
                owner,
                project,
                this,
                moduleNode,
            )
        }

        return moduleNode
    }

    fun addDependencies(
        owner: ProjectSystemId,
        project: KomposeProject,
        projectNode: DataNode<ProjectData>,
        moduleNodes: Map<String, DataNode<ModuleData>>,
        libraryNodes: MutableMap<String, DataNode<LibraryData>>
    ) {
        val moduleNode = moduleNodes[name]
            ?: return

        sourceSets.values
            .forEach {
                it.addDependencies(
                    owner,
                    project,
                    projectNode,
                    moduleNodes,
                    libraryNodes,
                    moduleNode,
                )
            }
    }
}
