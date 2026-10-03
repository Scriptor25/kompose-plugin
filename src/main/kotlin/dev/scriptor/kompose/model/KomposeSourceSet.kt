package dev.scriptor.kompose.model

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ExternalSystemException
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.model.project.*
import com.intellij.openapi.roots.DependencyScope
import kotlinx.serialization.Serializable
import kotlin.io.path.Path
import kotlin.io.path.div

@Serializable
data class KomposeSourceSet(
    val name: String,
    val source: String,
    val build: String,
    val dependencies: KomposeSourceSetDependencies,
) {
    fun toNode(
        owner: ProjectSystemId,
        project: KomposeProject,
        module: KomposeModule,
        moduleNode: DataNode<ModuleData>,
    ): DataNode<ContentRootData> {
        val moduleDirectory = Path(project.path) / module.path
        val directory = Path(project.path) / source
        val sourceDirectory = directory / "kotlin"
        val resourceDirectory = directory / "resources"

        val node = moduleNode.createChild(
            ProjectKeys.CONTENT_ROOT,
            ContentRootData(
                owner,
                moduleDirectory.toString(),
            ).also {
                it.storePath(ExternalSystemSourceType.SOURCE, sourceDirectory.toString())
                it.storePath(ExternalSystemSourceType.RESOURCE, resourceDirectory.toString())
            }
        )

        return node
    }

    fun addLibrary(
        owner: ProjectSystemId,
        project: KomposeProject,
        projectNode: DataNode<ProjectData>,
        libraryNodes: MutableMap<String, DataNode<LibraryData>>,
        mavenDependency: KomposeMavenDependency,
    ): DataNode<LibraryData> {
        val coordinate = "${mavenDependency.group}:${mavenDependency.artifact}:${mavenDependency.version}"

        return libraryNodes.computeIfAbsent(coordinate) { key ->
            projectNode.createChild(
                ProjectKeys.LIBRARY,
                LibraryData(
                    owner,
                    key,
                ).also {
                    it.setGroup(mavenDependency.group)
                    it.artifactId = mavenDependency.artifact
                    it.version = mavenDependency.version

                    val path = Path(project.path) / mavenDependency.path

                    it.addPath(LibraryPathType.BINARY, path.toString())
                }
            )
        }
    }

    fun addDependencies(
        owner: ProjectSystemId,
        project: KomposeProject,
        projectNode: DataNode<ProjectData>,
        moduleNodes: Map<String, DataNode<ModuleData>>,
        libraryNodes: MutableMap<String, DataNode<LibraryData>>,
        ownerNode: DataNode<ModuleData>,
    ) {
        dependencies.compile.modules
            .forEach {
                addDependency(
                    ownerNode,
                    it,
                    moduleNodes[it],
                    DependencyScope.COMPILE,
                )
            }

        dependencies.compile.maven
            .forEach {
                addDependency(
                    ownerNode,
                    addLibrary(
                        owner,
                        project,
                        projectNode,
                        libraryNodes,
                        it,
                    ),
                    DependencyScope.COMPILE,
                )
            }

        dependencies.runtime.modules
            .forEach {
                addDependency(
                    ownerNode,
                    it,
                    moduleNodes[it],
                    DependencyScope.RUNTIME,
                )
            }

        dependencies.compile.maven
            .forEach {
                addDependency(
                    ownerNode,
                    addLibrary(
                        owner,
                        project,
                        projectNode,
                        libraryNodes,
                        it,
                    ),
                    DependencyScope.RUNTIME,
                )
            }
    }

    fun addDependency(
        ownerNode: DataNode<ModuleData>,
        dependencyName: String,
        targetNode: DataNode<ModuleData>?,
        scope: DependencyScope,
    ) {
        if (targetNode == null)
            throw ExternalSystemException("Kompose module '$name' depends on unknown module '$dependencyName'")

        ownerNode.createChild(
            ProjectKeys.MODULE_DEPENDENCY,
            ModuleDependencyData(
                ownerNode.data,
                targetNode.data,
            ).also {
                it.scope = scope
            },
        )
    }

    fun addDependency(
        ownerNode: DataNode<ModuleData>,
        targetNode: DataNode<LibraryData>,
        scope: DependencyScope,
    ) {
        ownerNode.createChild(
            ProjectKeys.LIBRARY_DEPENDENCY,
            LibraryDependencyData(
                ownerNode.data,
                targetNode.data,
                LibraryLevel.MODULE,
            ).also {
                it.scope = scope
            }
        )
    }

}
