package dev.scriptor.kompose

import com.intellij.openapi.externalSystem.service.execution.AbstractExternalSystemTaskConfigurationType

class KomposeTaskConfigurationType : AbstractExternalSystemTaskConfigurationType(KomposeSystemId) {

    override fun getConfigurationFactoryId(): String {
        return KomposeSystemId.id
    }
}
