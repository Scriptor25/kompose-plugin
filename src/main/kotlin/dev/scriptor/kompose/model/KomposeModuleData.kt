package dev.scriptor.kompose.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface KomposeModuleData {

    val include: List<String>

    @Serializable
    @SerialName("application")
    data class Application(
        override val include: List<String>,
        val main: String,
    ) : KomposeModuleData

    @Serializable
    @SerialName("library")
    data class Library(
        override val include: List<String>,
        @SerialName("include_sources")
        val includeSources: Boolean,
    ) : KomposeModuleData
}
