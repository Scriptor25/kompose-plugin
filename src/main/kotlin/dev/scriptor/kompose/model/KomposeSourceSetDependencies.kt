package dev.scriptor.kompose.model

import kotlinx.serialization.Serializable

@Serializable
data class KomposeSourceSetDependencies(
    val compile: KomposeDependencies,
    val runtime: KomposeDependencies,
)
