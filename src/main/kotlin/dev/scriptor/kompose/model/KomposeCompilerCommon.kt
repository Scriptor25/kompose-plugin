package dev.scriptor.kompose.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KomposeCompilerCommon(
    @SerialName("kotlin_home")
    val kotlinHome: String? = null,
    @SerialName("api_version")
    val apiVersion: String? = null,
    @SerialName("language_version")
    val languageVersion: String? = null,
    @SerialName("opt_in")
    val optIn: Set<String> = emptySet(),
    val progressive: Boolean = false,
    @SerialName("plugin_options")
    val pluginOptions: Map<String, String> = emptyMap(),
    val extra: List<String> = emptyList(),
)
