package dev.scriptor.kompose.model

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.model.project.ExternalSystemSourceType
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
        projectPath: String,
        projectNode: DataNode<ProjectData>,
    ): DataNode<ModuleData> {
        val moduleFileDirectoryPath = Path(projectPath) / ".idea" / "kompose"
        val externalConfigPath = Path(projectPath) / path / "module.toml"

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
                isInheritProjectCompileOutputPath = false

                sourceSets.values.forEach {
                    val build = Path(projectPath) / it.build / "classes"

                    when (it.name) {
                        "main" -> setCompileOutputPath(ExternalSystemSourceType.SOURCE, build.toString())
                        "test" -> setCompileOutputPath(ExternalSystemSourceType.TEST, build.toString())
                    }
                }
            },
        )

        sourceSets.values.forEach {
            it.toNode(
                owner,
                projectPath,
                this,
                moduleNode,
            )
        }

        return moduleNode
    }

    fun addDependencies(
        owner: ProjectSystemId,
        projectPath: String,
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
                    projectPath,
                    projectNode,
                    moduleNodes,
                    libraryNodes,
                    moduleNode,
                )
            }
    }
}
