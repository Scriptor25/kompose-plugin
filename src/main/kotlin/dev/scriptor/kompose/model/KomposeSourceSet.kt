package dev.scriptor.kompose.model

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ExternalSystemException
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.model.project.*
import com.intellij.openapi.roots.DependencyScope
import dev.scriptor.kompose.KomposeDataKey
import kotlinx.serialization.Serializable
import kotlin.io.path.Path
import kotlin.io.path.div

@Serializable
data class KomposeSourceSet(
    val name: String,
    val source: String,
    val build: String,
    val dependencies: KomposeSourceSetDependencies,
    val compiler: KomposeCompiler,
) {
    companion object {
        const val KEY = "SOURCE_SET"
    }

    fun toNode(
        owner: ProjectSystemId,
        projectPath: String,
        module: KomposeModule,
        parentNode: DataNode<*>,
    ): DataNode<ModuleData> {
        val modulePath = Path(projectPath) / module.path

        val moduleFileDirectoryPath = Path(projectPath) / ".idea" / "kompose"
        val externalConfigPath = modulePath / "module.toml"

        val source = Path(projectPath) / source
        val build = Path(projectPath) / build

        val kotlin = source / "kotlin"
        val resources = source / "resources"

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
                val extra = buildList {
                    addAll(compiler.common.extra)
                    addAll(compiler.data.extra)

                    if (compiler.common.kotlinHome != null) {
                        add("-kotlin-home")
                        add(compiler.common.kotlinHome)
                    }
                    for (entry in compiler.common.optIn) {
                        add("-opt-in")
                        add(entry)
                    }
                    for ((key, value) in compiler.common.pluginOptions) {
                        add("-P")
                        add("$key=$value")
                    }
                    if (compiler.common.progressive)
                        add("-progressive")

                    when (compiler.data) {
                        is KomposeCompilerData.Jvm -> {
                            if (compiler.data.jdkHome != null) {
                                add("-jdk-home")
                                add(compiler.data.jdkHome)
                            }
                            if (compiler.data.default != null) {
                                add("-jvm-default")
                                add(compiler.data.default)
                            }
                            if (compiler.data.target != null) {
                                add("-jvm-target")
                                add(compiler.data.target)
                            }
                            if (compiler.data.javaParameters) {
                                add("-java-parameters")
                            }
                            if (compiler.data.noJdk) {
                                add("-no-jdk")
                            }
                            if (compiler.data.noReflect) {
                                add("-no-reflect")
                            }
                            if (compiler.data.noStdlib) {
                                add("-no-stdlib")
                            }
                            if (compiler.data.scriptTemplates.isNotEmpty()) {
                                add("-script-templates")

                                var str = ""
                                for (template in compiler.data.scriptTemplates)
                                    str += "$template,"
                                add(str.substring(0, str.length - 1))
                            }
                            if (compiler.data.addModules.isNotEmpty()) {
                                add("-Xadd-modules")

                                var str = ""
                                for (module in compiler.data.addModules)
                                    str += "$module,"
                                add(str.substring(0, str.length - 1))
                            }
                            if (compiler.data.exposeBoxed) {
                                add("-Xjvm-expose-boxed")
                            }
                            for ((key, value) in compiler.data.nullabilityAnnotations) {
                                add("-Xnullability-annotations")
                                add("@$key:$value")
                            }
                            if (compiler.data.enablePreview) {
                                add("-Xjvm-enable-preview")
                            }
                        }

                        is KomposeCompilerData.Js -> TODO()
                        is KomposeCompilerData.Native -> TODO()
                    }
                }

                val targetPlatform = when (compiler.data) {
                    is KomposeCompilerData.Jvm -> "jvm:${compiler.data.target ?: "default"}"
                    is KomposeCompilerData.Js -> TODO()
                    is KomposeCompilerData.Native -> TODO()
                }

                setProperty(KomposeDataKey.KEY, KEY)
                setProperty(KomposeDataKey.TARGET_PLATFORM, targetPlatform)
                setProperty(KomposeDataKey.LANGUAGE_LEVEL, compiler.common.languageVersion)
                setProperty(KomposeDataKey.API_LEVEL, compiler.common.apiVersion)
                setProperty(
                    KomposeDataKey.ADDITIONAL_ARGUMENTS,
                    if (extra.isNotEmpty()) extra.joinToString("\" \"", "\"", "\"") else null,
                )

                val classes = build / "classes"

                isInheritProjectCompileOutputPath = false

                setCompileOutputPath(ExternalSystemSourceType.SOURCE, classes.toString())
            },
        )

        moduleNode.createChild(
            ProjectKeys.CONTENT_ROOT,
            ContentRootData(
                owner,
                source.toString(),
            ).apply {
                storePath(ExternalSystemSourceType.SOURCE, kotlin.toString())
                storePath(ExternalSystemSourceType.RESOURCE, resources.toString())
            },
        )

        return moduleNode
    }

    fun addDependencies(
        owner: ProjectSystemId,
        projectPath: String,
        projectNode: DataNode<ProjectData>,
        moduleNodes: Map<String, Map<String, DataNode<ModuleData>>>,
        libraryNodes: MutableMap<String, DataNode<LibraryData>>,
        ownerNode: DataNode<ModuleData>,
    ) {
        dependencies.compile.modules
            .forEach {
                val nodes = moduleNodes[it]
                    ?: throw ExternalSystemException("Kompose module '$name' depends on unknown module '$it'")

                nodes.forEach { (_, node) ->
                    addDependency(
                        ownerNode,
                        node,
                        DependencyScope.COMPILE,
                    )
                }
            }

        dependencies.compile.maven
            .forEach {
                addDependency(
                    ownerNode,
                    addLibrary(
                        owner,
                        projectPath,
                        projectNode,
                        libraryNodes,
                        it,
                    ),
                    DependencyScope.COMPILE,
                )
            }

        dependencies.runtime.modules
            .forEach {
                val nodes = moduleNodes[it]
                    ?: throw ExternalSystemException("Kompose module '$name' depends on unknown module '$it'")

                nodes.forEach { (_, node) ->
                    addDependency(
                        ownerNode,
                        node,
                        DependencyScope.RUNTIME,
                    )
                }
            }

        dependencies.runtime.maven
            .forEach {
                addDependency(
                    ownerNode,
                    addLibrary(
                        owner,
                        projectPath,
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
        targetNode: DataNode<ModuleData>,
        scope: DependencyScope,
    ) {
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
}

fun addLibrary(
    owner: ProjectSystemId,
    projectPath: String,
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

                val path = Path(projectPath) / mavenDependency.path
                val sources = Path(projectPath) / mavenDependency.sources

                it.addPath(LibraryPathType.BINARY, path.toString())
                it.addPath(LibraryPathType.SOURCE, sources.toString())
            }
        )
    }
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
