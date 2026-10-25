package dev.scriptor.kompose.model

import kotlinx.serialization.Serializable

@Serializable
data class KomposeCompiler(
    val common: KomposeCompilerCommon,
    val data: KomposeCompilerData,
)
