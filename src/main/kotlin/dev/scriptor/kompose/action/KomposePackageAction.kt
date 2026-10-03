package dev.scriptor.kompose.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import dev.scriptor.kompose.KomposeTask
import dev.scriptor.kompose.runKomposeTask

class KomposePackageAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        runKomposeTask(e.project, KomposeTask.PACKAGE)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}
