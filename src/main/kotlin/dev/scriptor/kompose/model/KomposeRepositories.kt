package dev.scriptor.kompose.model

import kotlinx.serialization.Serializable

@Serializable
data class KomposeRepositories(
    val maven: List<String>,
)
