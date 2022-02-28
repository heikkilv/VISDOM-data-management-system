package visdom.adapters.general.model.origins

import visdom.adapters.general.model.base.Origin
import visdom.adapters.general.model.origins.data.GitlabOriginData
import visdom.utils.GeneralUtils
import visdom.utils.CommonConstants


class GitlabOrigin(hostName: String, projectGroup: String, projectName: String, project_id: Option[Int])
extends Origin {
    def getType: String = GitlabOrigin.GitlabOriginType
    val source: String = hostName
    val context: String = projectName

    override val id: String = GitlabOrigin.getId(hostName, projectName)
    val data: GitlabOriginData = GitlabOriginData(project_id, projectGroup)
}

object GitlabOrigin {
    final val GitlabOriginType: String = "GitLab"

    def getGitlabOriginFromHost(hostName: String): GitlabOrigin = {
        new GitlabOrigin(hostName, CommonConstants.EmptyString, CommonConstants.EmptyString, None)
    }

    def getId(hostName: String, projectName: String): String = {
        GeneralUtils.getUuid(GitlabOriginType, hostName, projectName)
    }
}
