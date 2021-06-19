package visdom.http.gitlab

import scalaj.http.Http
import visdom.fetchers.gitlab.GitlabServer


object Utils {
    def getProjectQueryStatusCode(gitlabServer: GitlabServer, projectName: String): Int = {
        visdom.http.HttpUtils.returnRequestStatusCode(
            gitlabServer.modifyRequest(
                Http(
                    RequestPath.getProjectPath(gitlabServer.baseAddress, projectName)
                )
            )
        )
    }
}
