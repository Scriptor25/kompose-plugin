package dev.scriptor.kompose.model

import kotlinx.serialization.Serializable

@Serializable
data class KomposeDependencies(
    val modules: List<String>,
    val maven: List<KomposeMavenDependency>,
    val tree: List<KomposeMavenDependency>,
)
