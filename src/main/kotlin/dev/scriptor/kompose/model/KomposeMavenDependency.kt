package dev.scriptor.kompose.model

import kotlinx.serialization.Serializable

@Serializable
data class KomposeMavenDependency(
    val group: String,
    val artifact: String,
    val version: String,
    val classifier: String?,
    val path: String,
    val sources: String,
)
