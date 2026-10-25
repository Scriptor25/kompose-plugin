package dev.scriptor.kompose.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface KomposeCompilerData {

    val extra: List<String>

    @Serializable
    @SerialName("jvm")
    data class Jvm(
        @SerialName("jdk_home")
        val jdkHome: String? = null,
        val default: String? = null,
        val target: String? = null,
        @SerialName("java_parameters")
        val javaParameters: Boolean = false,
        @SerialName("no_jdk")
        val noJdk: Boolean = false,
        @SerialName("no_reflect")
        val noReflect: Boolean = false,
        @SerialName("no_stdlib")
        val noStdlib: Boolean = false,
        @SerialName("script_templates")
        val scriptTemplates: Set<String> = emptySet(),
        @SerialName("add_modules")
        val addModules: Set<String> = emptySet(),
        @SerialName("expose_boxed")
        val exposeBoxed: Boolean = false,
        @SerialName("nullability_annotations")
        val nullabilityAnnotations: Map<String, String> = emptyMap(),
        @SerialName("enable_preview")
        val enablePreview: Boolean = false,
        override val extra: List<String> = emptyList(),
    ) : KomposeCompilerData

    @Serializable
    @SerialName("js")
    data class Js(
        @SerialName("module_kind")
        val moduleKind: String? = null,
        val target: String? = null,
        val libraries: Set<String> = emptySet(),
        @SerialName("output_prefix")
        val outputPrefix: String? = null,
        @SerialName("output_postfix")
        val outputPostfix: String? = null,
        @SerialName("source_map_embed_sources")
        val sourceMapEmbedSources: String? = null,
        @SerialName("source_map_names_policy")
        val sourceMapNamesPolicy: String? = null,
        @SerialName("source_map_prefix")
        val sourceMapPrefix: String? = null,
        @SerialName("source_map_base_dirs")
        val sourceMapBaseDirs: Set<String> = emptySet(),
        @SerialName("no_stdlib")
        val noStdlib: Boolean = false,
        @SerialName("generate_meta_info")
        val generateMetaInfo: Boolean = false,
        @SerialName("generate_source_map")
        val generateSourceMap: Boolean = false,
        override val extra: List<String> = emptyList(),
    ) : KomposeCompilerData

    @Serializable
    @SerialName("native")
    data class Native(
        val target: String? = null,
        @SerialName("enable_debug")
        val enableDebug: Boolean = false,
        @SerialName("enable_assertions")
        val enableAssertions: Boolean = false,
        @SerialName("enable_optimizations")
        val enableOptimizations: Boolean = false,
        @SerialName("no_stdlib")
        val noStdlib: Boolean = false,
        @SerialName("no_default_libs")
        val noDefaultLibs: Boolean = false,
        @SerialName("generate_test_runner")
        val generateTestRunner: Boolean = false,
        @SerialName("generate_no_exit_test_runner")
        val generateNoExitTestRunner: Boolean = false,
        override val extra: List<String> = emptyList(),
    ) : KomposeCompilerData
}
