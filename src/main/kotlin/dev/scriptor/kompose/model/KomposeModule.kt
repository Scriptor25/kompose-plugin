package dev.scriptor.kompose.model

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.model.project.ContentRootData
import com.intellij.openapi.externalSystem.model.project.LibraryData
import com.intellij.openapi.externalSystem.model.project.ModuleData
import com.intellij.openapi.externalSystem.model.project.ProjectData
import dev.scriptor.kompose.KomposeDataKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.io.path.Path
import kotlin.io.path.div

@Serializable
data class KomposeModuleArtifact(
    val group: String,
    val artifact: String,
    val version: String,
)

@Serializable
data class KomposeModule(
    val name: String,
    val path: String,
    val source: String,
    val build: String,
    val artifact: KomposeModuleArtifact,
    @SerialName("source_sets")
    val sourceSets: Map<String, KomposeSourceSet>,
    val data: KomposeModuleData,
) {
    companion object {
        const val KEY = "MODULE"
    }

    fun toNode(
        owner: ProjectSystemId,
        projectPath: String,
        parentNode: DataNode<*>,
    ): Map<String, DataNode<ModuleData>> {
        val modulePath = Path(projectPath) / path

        val moduleFileDirectoryPath = Path(projectPath) / ".idea" / "kompose"
        val externalConfigPath = modulePath / "module.toml"

        val moduleName = when (val data = parentNode.data) {
            is ModuleData -> "${data.moduleName}.$name"
            else -> name
        }

        val moduleNode = parentNode.createChild(
            ProjectKeys.MODULE,
            ModuleData(
                moduleName,
                owner,
                "JAVA_MODULE",
                moduleName,
                moduleFileDirectoryPath.toString(),
                externalConfigPath.toString(),
            ).apply {
                setProperty(KomposeDataKey.KEY, KEY)

                group = artifact.group
                version = artifact.version

                // publication = object : ProjectCoordinate {
                //     override fun getGroupId(): String = module.artifact.group
                //     override fun getArtifactId(): String = module.artifact.artifact
                //     override fun getVersion(): String = module.artifact.version
                // }
            }
        )

        moduleNode.createChild(
            ProjectKeys.CONTENT_ROOT,
            ContentRootData(
                owner,
                modulePath.toString(),
            ),
        )

        return sourceSets.mapValues {
            it.value.toNode(
                owner,
                projectPath,
                this,
                moduleNode,
            )
        }
    }

    fun addDependencies(
        owner: ProjectSystemId,
        projectPath: String,
        projectNode: DataNode<ProjectData>,
        moduleNodes: Map<String, Map<String, DataNode<ModuleData>>>,
        libraryNodes: MutableMap<String, DataNode<LibraryData>>
    ) {
        val sourceSetNodes = moduleNodes[name]
            ?: return

        sourceSets.forEach {
            val moduleNode = sourceSetNodes[it.key]
                ?: error("invalid source set '${it.key}'")

            it.value.addDependencies(
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
