package dev.scriptor.kompose

import com.intellij.openapi.components.Service
import com.intellij.openapi.externalSystem.model.ExternalSystemException
import com.intellij.openapi.project.Project
import com.intellij.platform.eel.provider.utils.stderrString
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportRawProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Service(Service.Level.PROJECT)
class KomposeProjectService(
    private val project: Project,
    private val scope: CoroutineScope,
) {
    fun resolve() {
        scope.launch {
            withBackgroundProgress(project, "Resolving Kompose Dependencies") {
                withContext(Dispatchers.IO) {
                    reportRawProgress { reporter ->
                        val result = process(
                            "kompose", ":resolve", "--progress",
                            stdout = { line ->
                                val (fst, snd) = line.split("/", limit = 2).map { it.trim().toInt() }

                                reporter.fraction(fst.toDouble() / snd.toDouble())
                                reporter.text("$fst / $snd")
                            },
                            workingDirectory = project.basePath!!,
                        )

                        if (result.exitCode != 0)
                            throw ExternalSystemException("'kompose :resolve' failed with exit code ${result.exitCode}:\n${result.stderrString}")
                    }
                }
            }
        }
    }
}
