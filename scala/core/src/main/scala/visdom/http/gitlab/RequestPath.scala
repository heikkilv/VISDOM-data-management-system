// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.gitlab


object RequestPath {
    def getProjectPath(baseAddress: String, projectName: String): String = {
        List(
            baseAddress,
            visdom.http.HttpConstants.PathProjects,
            scalaj.http.HttpConstants.urlEncode(projectName, scalaj.http.HttpConstants.utf8)
        ).mkString(visdom.http.HttpConstants.CharacterSlash)
    }
}
