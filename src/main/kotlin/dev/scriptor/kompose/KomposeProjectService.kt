package dev.scriptor.kompose

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope

@Service(Service.Level.PROJECT)
class KomposeProjectService(
    private val project: Project,
    private val scope: CoroutineScope,
) {
}
