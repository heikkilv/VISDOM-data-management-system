package visdom.fetchers.gitlab.queries.files

object FileConstants {
    final val FileRootPath = "/files"
    final val FilePath = "files"

    final val FileStatusAcceptedDescription = "The fetching of the file data has been started"

    final val FileEndpointDescription = "Starts a fetching process for files data from a GitLab repository."
    final val FileEndpointSummary = "Fetch files data from a GitLab repository."

    // the example responses for the files endpoint
    final val FileResponseExampleAccepted = """{
        "status": "Accepted",
        "description": "The fetching of the file data has been started",
        "options": {
            "projectName": "group/my-project-name",
            "reference": "master",
            "recursive": "true",
            "includeCommitLinks": "true"
        }
    }"""
}
