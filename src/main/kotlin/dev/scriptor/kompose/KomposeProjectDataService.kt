package dev.scriptor.kompose

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ExternalSystemException
import com.intellij.openapi.externalSystem.model.Key
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.externalSystem.service.project.manage.AbstractProjectDataService
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.roots.ProjectRootManager

class KomposeProjectDataService : AbstractProjectDataService<ProjectData, Project>() {

    override fun getTargetDataKey(): Key<ProjectData> = ProjectKeys.PROJECT

    override fun importData(
        toImport: Collection<DataNode<ProjectData>>,
        projectData: ProjectData?,
        project: Project,
        modelsProvider: IdeModifiableModelsProvider,
    ) {
        val projectManager = ProjectRootManager.getInstance(project)

        val projectSdk = ReadAction.computeBlocking<Sdk, ExternalSystemException> { projectManager.projectSdk }
        if (projectSdk == null) {
            // JavaSdk.getInstance().createJdk("", "")
            val javaSdk = SdkConfigurationUtil.findOrCreateSdk(
                JavaSdk.getInstance().versionComparator(),
                *JavaSdk.getAllTypeList().toTypedArray(),
            )
            if (javaSdk != null) {
                WriteAction.run<ExternalSystemException> { projectManager.projectSdk = javaSdk }
            }
        }
    }
}
