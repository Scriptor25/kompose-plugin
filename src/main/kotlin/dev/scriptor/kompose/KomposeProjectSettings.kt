package dev.scriptor.kompose

import com.intellij.openapi.externalSystem.settings.ExternalProjectSettings

class KomposeProjectSettings : ExternalProjectSettings() {

    override fun clone(): KomposeProjectSettings {
        return KomposeProjectSettings().also {
            it.externalProjectPath = externalProjectPath
            it.isUseQualifiedModuleNames = isUseQualifiedModuleNames
        }
    }
}
