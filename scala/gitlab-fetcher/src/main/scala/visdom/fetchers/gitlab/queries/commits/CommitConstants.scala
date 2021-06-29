package visdom.fetchers.gitlab.queries.commits


object CommitConstants {
    final val CommitRootPath = "/commits"
    final val CommitPath = "commits"

    final val CommitEndpointDescription = "Starts a fetching process for commit data from a GitLab repository."
    final val CommitEndpointSummary = "Fetch commit data from a GitLab repository."

    final val CommitStatusAcceptedDescription = "The fetching of the commit data has been started"

    final val ParameterDescriptionStartDate = "the earliest timestamp for the fetched commits given in ISO 8601 format with timezone"
    final val ParameterDescriptionEndDate = "the latest timestamp for the fetched commits given in ISO 8601 format with timezone"

    // the example responses for the commits endpoint
    final val CommitResponseExampleAccepted = """{
        "status": "Accepted",
        "description": "The fetching of the commit data has been started",
        "options": {
            "projectName": "group/my-project-name",
            "reference": "master",
            "startDate": "2020-01-01T00:00:00.000Z",
            "endDate": "2021-01-01T00:00:00.000Z",
            "includeStatistics": "true",
            "includeFileLinks": "true",
            "includeReferenceLinks": "false"
        }
    }"""
}
