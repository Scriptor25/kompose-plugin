package dev.scriptor.kompose

import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener
import com.intellij.util.messages.Topic

interface KomposeSettingsListener : ExternalSystemSettingsListener<KomposeProjectSettings> {

    companion object {
        val TOPIC = Topic(
            "dev.scriptor.kompose.settings",
            KomposeSettingsListener::class.java,
        )
    }
}
