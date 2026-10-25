package dev.scriptor.kompose

import com.intellij.facet.FacetManager
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.Key
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.project.ModuleData
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.externalSystem.service.project.manage.AbstractProjectDataService
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import dev.scriptor.kompose.model.KomposeSourceSet
import org.jetbrains.kotlin.config.CompilerSettings
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.idea.facet.KotlinFacet
import org.jetbrains.kotlin.idea.facet.KotlinFacetType
import org.jetbrains.kotlin.idea.serialization.updateCompilerArguments
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.js.JsPlatforms
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.platform.konan.NativePlatforms

class KomposeModuleDataService : AbstractProjectDataService<ModuleData, Module>() {

    override fun getTargetDataKey(): Key<ModuleData> = ProjectKeys.MODULE

    override fun importData(
        toImport: Collection<DataNode<ModuleData>>,
        projectData: ProjectData?,
        project: Project,
        modelsProvider: IdeModifiableModelsProvider,
    ) {
        ExternalSystemApiUtil.executeProjectChangeAction(project) {
            for (node in toImport) {
                if (node.data.getProperty(KomposeDataKey.KEY) != KomposeSourceSet.KEY)
                    continue

                val module = modelsProvider.modifiableModuleModel.findModuleByName(node.data.moduleName)
                    ?: error("module '${node.data.moduleName}' does not exist")

                val facetManager = FacetManager.getInstance(module)
                val facetType = KotlinFacetType.INSTANCE

                val facet = when (val existing = facetManager.getFacetByType(facetType.id)) {
                    null -> facetManager.addFacet(
                        facetType,
                        "Kotlin",
                        null,
                    )

                    else -> existing
                }

                configure(facet, node.data)

                facetManager.facetConfigurationChanged(facet)
            }
        }
    }

    private fun configure(facet: KotlinFacet, data: ModuleData) {
        val targetPlatformString = data.getProperty(KomposeDataKey.TARGET_PLATFORM)
        val languageLevelString = data.getProperty(KomposeDataKey.LANGUAGE_LEVEL)
        val apiLevelString = data.getProperty(KomposeDataKey.API_LEVEL)
        val additionalArgumentsString = data.getProperty(KomposeDataKey.ADDITIONAL_ARGUMENTS)

        val settings = facet.configuration.settings

        settings.useProjectSettings = false

        if (targetPlatformString != null)
            settings.targetPlatform = parseTargetPlatform(targetPlatformString)
        if (languageLevelString != null)
            settings.languageLevel = LanguageVersion.fromVersionString(languageLevelString)
        if (apiLevelString != null)
            settings.apiLevel = LanguageVersion.fromVersionString(apiLevelString)

        settings.compilerSettings = CompilerSettings().apply {
            if (additionalArgumentsString != null)
                additionalArguments = additionalArgumentsString
        }
    }

    private fun parseTargetPlatform(str: String): TargetPlatform {
        val (cls, spec) = str.split(':', limit = 2)

        return when (cls) {
            "jvm" -> {
                val jvmTarget = when (spec) {
                    "default" -> JvmTarget.DEFAULT
                    "1.6" -> JvmTarget.JVM_1_6
                    "1.8" -> JvmTarget.JVM_1_8
                    "9" -> JvmTarget.JVM_9
                    "10" -> JvmTarget.JVM_10
                    "11" -> JvmTarget.JVM_11
                    "12" -> JvmTarget.JVM_12
                    "13" -> JvmTarget.JVM_13
                    "14" -> JvmTarget.JVM_14
                    "15" -> JvmTarget.JVM_15
                    "16" -> JvmTarget.JVM_16
                    "17" -> JvmTarget.JVM_17
                    "18" -> JvmTarget.JVM_18
                    "19" -> JvmTarget.JVM_19
                    "20" -> JvmTarget.JVM_20
                    "21" -> JvmTarget.JVM_21
                    "22" -> JvmTarget.JVM_22
                    "23" -> JvmTarget.JVM_23
                    "24" -> JvmTarget.JVM_24
                    "25" -> JvmTarget.JVM_25
                    "26" -> JvmTarget.JVM_26
                    "27" -> null
                    "28" -> null
                    else -> error("unsupported jvm specifier '$spec'")
                }

                if (jvmTarget == null)
                    JvmPlatforms.unspecifiedJvmPlatform
                else
                    JvmPlatforms.jvmPlatformByTargetVersion(jvmTarget)
            }

            "js" -> {
                if (spec != "default")
                    error("unsupported js specifier '$spec'")

                JsPlatforms.defaultJsPlatform
            }

            "native" -> {
                val names = spec.split('+')

                NativePlatforms.nativePlatformByTargetNames(names)
            }

            else -> error("unsupported platform class '$cls'")
        }
    }
}
