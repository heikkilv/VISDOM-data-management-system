package visdom.fetchers.gitlab.queries.all


object AllDataConstants {
    final val AllDataRootPath = "/all"
    final val AllDataPath = "all"

    final val AllDataEndpointDescription =
        """Starts a fetching process for commit, file and pipeline data from a GitLab repository.
        All additional metadata and link data will be included."""
    final val AllDataEndpointSummary = "Fetch commit, file and pipeline data from a GitLab repository."

    final val AllDataStatusAcceptedDescription = "The fetching of the data has started"

    final val ParameterDefaultFilePath: Option[String] = None
    final val ParameterDefaultIncludeStatistics: Boolean = true
    final val ParameterDefaultIncludeFileLinks: Boolean = true
    final val ParameterDefaultIncludeReferenceLinks: Boolean = true
    final val ParameterDefaultIncludeCommitLinks: Boolean = true
    final val ParameterDefaultIncludeJobs: Boolean = true
    final val ParameterDefaultIncludeJobLogs: Boolean = true
    final val ParameterDefaultRecursive: Boolean = true

    // the example responses for the all endpoint
    final val AllDataResponseExampleAccepted = """{
        "status": "Accepted",
        "description": "The fetching of the data has been started",
        "options": {
            "projectName": "group/my-project-name",
            "reference": "master",
            "startDate": "2020-01-01T00:00:00.000Z",
            "endDate": "2021-01-01T00:00:00.000Z"
        }
    }"""
}
