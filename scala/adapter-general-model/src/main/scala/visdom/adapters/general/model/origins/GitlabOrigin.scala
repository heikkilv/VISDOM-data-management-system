package visdom.adapters.general.model.origins

import visdom.adapters.general.model.base.Origin
import visdom.utils.GeneralUtils
import visdom.utils.CommonConstants


class GitlabOrigin(hostName: String, projectGroup: String, projectName: String)
extends Origin {
    def getType: String = GitlabOrigin.GitlabOriginType
    val source: String = hostName
    val context: String = projectName

    val groupName: String = projectGroup
}

object GitlabOrigin {
    final val GitlabOriginType: String = "GitLab"

    def getGitlabOriginFromHost(hostName: String): GitlabOrigin = {
        new GitlabOrigin(hostName, CommonConstants.EmptyString, CommonConstants.EmptyString)
    }
}
