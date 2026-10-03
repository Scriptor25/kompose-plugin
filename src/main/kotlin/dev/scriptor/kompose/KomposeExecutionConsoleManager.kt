package dev.scriptor.kompose

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.ui.ExecutionConsole
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.externalSystem.execution.ExternalSystemExecutionConsoleManager
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTask
import com.intellij.openapi.util.Key
import com.jetbrains.rd.util.string.print
import com.jetbrains.rd.util.string.printer

class KomposeExecutionConsoleManager : ExternalSystemExecutionConsoleManager<ExecutionConsole, ProcessHandler> {

    override fun getExternalSystemId(): ProjectSystemId {
        return KomposeSystemId
    }

    override fun onOutput(
        executionConsole: ExecutionConsole,
        processHandler: ProcessHandler,
        text: String,
        processOutputType: Key<*>,
    ) {
        executionConsole.print(
            printer {
                print(text)
            }
        )
    }

    override fun isApplicableFor(task: ExternalSystemTask): Boolean {
        return true
    }

    override fun getRestartActions(consoleView: ExecutionConsole): Array<out AnAction> {
        return emptyArray()
    }
}