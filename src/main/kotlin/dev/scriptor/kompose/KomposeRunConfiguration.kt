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

    // TODO: select module to run

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return object : SettingsEditor<RunConfiguration>() {
            lateinit var configuration: RunConfiguration

            override fun resetEditorFrom(configuration: RunConfiguration) {
                this.configuration = configuration
            }

            override fun applyEditorTo(configuration: RunConfiguration) {
                configuration.name = this.configuration.name
                configuration.beforeRunTasks = this.configuration.beforeRunTasks
                configuration.isAllowRunningInParallel = this.configuration.isAllowRunningInParallel
            }

            override fun createEditor(): JComponent {
                return JPanel()
            }
        }
    }
}