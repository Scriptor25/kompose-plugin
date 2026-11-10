package dev.scriptor.kompose.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface KomposeModuleData {

    @Serializable
    @SerialName("application")
    data class Application(
        val main: String,
    ) : KomposeModuleData

    @Serializable
    @SerialName("library")
    data class Library(
        @SerialName("include_sources")
        val includeSources: Boolean = false,
    ) : KomposeModuleData
}
