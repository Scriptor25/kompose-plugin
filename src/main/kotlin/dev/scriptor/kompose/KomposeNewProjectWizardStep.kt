package dev.scriptor.kompose

import com.intellij.ide.wizard.AbstractNewProjectWizardStep
import com.intellij.ide.wizard.NewProjectWizardBaseData.Companion.baseData
import com.intellij.ide.wizard.NewProjectWizardStep
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindItem
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.name

class KomposeNewProjectWizardStep(parent: NewProjectWizardStep) : AbstractNewProjectWizardStep(parent) {

    enum class ModuleType {
        Application,
        Library,
    }

    companion object {

    }

    private val moduleTypeProperty = propertyGraph.property(ModuleType.Application)

    override fun setupUI(builder: Panel) {
        with(builder) {
            row("Module Type") {
                comboBox(listOf(ModuleType.Application, ModuleType.Library))
                    .bindItem(moduleTypeProperty)
            }
        }
    }

    override fun setupProject(project: Project) {
        val root = Path(requireNotNull(baseData).contentEntryPath)

        val moduleName = when (moduleTypeProperty.get()) {
            ModuleType.Application -> "app"
            ModuleType.Library -> "lib"
        }

        createDirectoryStructure(root, listOf(moduleName))

        generateGitIgnore(root)
        generateProjectToml(root, listOf(moduleName))

        when (moduleTypeProperty.get()) {
            ModuleType.Application -> generateApplicationModule(root, moduleName)
            ModuleType.Library -> generateLibraryModule(root, moduleName)
        }
    }

    private fun generateGitIgnore(root: Path) {
        val content = """
            /build/
            /repository/
        """.trimIndent() + "\n"

        Files.writeString(root / ".gitignore", content)
    }

    private fun createDirectoryStructure(root: Path, modules: Collection<String>) {
        for (module in modules) {
            Files.createDirectories(root / module / "src" / "main" / "kotlin")
            Files.createDirectories(root / module / "src" / "main" / "resources")
        }
    }

    private fun generateProjectToml(root: Path, modules: Collection<String>) {
        val content = """
            name = "${root.name}"

            [artifact]
            group = "dev.example"
            version = "1.0.0"

            [modules]
            include = ${modules.joinToString(", ", "[", "]") { """"$it"""" }}
            
            [dependencies]
            maven = ["org.jetbrains.kotlin:kotlin-stdlib:2.4.20"]
            
            [repositories]
            maven = ["https://repo1.maven.org/maven2/"]
        """.trimIndent() + "\n"

        Files.writeString(root / "project.toml", content)
    }

    private fun generateApplicationModule(root: Path, name: String) {
        val content = """
            type = "application"
            name = "$name"

            [application]
            main = "MainKt"
            include = ["main"]

            [sources.main]
        """.trimIndent() + "\n"

        Files.writeString(root / name / "module.toml", content)
    }

    private fun generateLibraryModule(root: Path, name: String) {
        val content = """
            type = "library"
            name = "$name"

            [library]
            package = "jar"
            sources = true
            include = ["main"]

            [sources.main]
        """.trimIndent() + "\n"

        Files.writeString(root / name / "module.toml", content)
    }
}
