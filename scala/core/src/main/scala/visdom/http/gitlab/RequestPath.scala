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
