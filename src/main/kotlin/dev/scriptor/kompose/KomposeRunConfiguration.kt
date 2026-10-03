package dev.scriptor.kompose

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import javax.swing.JComponent
import javax.swing.JPanel

class KomposeRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
) : RunConfigurationBase<RunConfigurationOptions>(project, factory, null) {

    override fun getState(
        executor: Executor,
        environment: ExecutionEnvironment,
    ): RunProfileState {
        return object : CommandLineState(environment) {
            override fun startProcess(): ProcessHandler {
                val commandLine = GeneralCommandLine(
                    "kompose", "launch",
                ).withWorkDirectory(project.basePath)

                return OSProcessHandler(commandLine).also {
                    ProcessTerminatedListener.attach(it)
                }
            }
        }
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return object : SettingsEditor<RunConfiguration>() {
            override fun resetEditorFrom(s: RunConfiguration) {
                // noop
            }

            override fun applyEditorTo(s: RunConfiguration) {
                // noop
            }

            override fun createEditor(): JComponent {
                return JPanel()
            }
        }
    }
}