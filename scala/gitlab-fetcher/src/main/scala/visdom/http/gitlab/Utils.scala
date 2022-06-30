// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

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
