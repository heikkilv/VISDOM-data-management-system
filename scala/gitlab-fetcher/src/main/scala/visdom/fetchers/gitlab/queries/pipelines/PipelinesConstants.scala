package visdom.fetchers.gitlab.queries.pipelines


object PipelinesConstants {
    final val PipelinesRootPath = "/pipelines"
    final val PipelinesPath = "pipelines"

    final val PipelinesStatusAcceptedDescription = "The fetching of the pipeline data has been started"

    final val PipelinesEndpointDescription = "Starts a fetching process for pipeline data from a GitLab repository."
    final val PipelinesEndpointSummary = "Fetch pipeline data from a GitLab repository."

    final val ParameterDescriptionStartDate = "the earliest timestamp for the fetched pipelines given in ISO 8601 format with timezone"
    final val ParameterDescriptionEndDate = "the latest timestamp for the fetched pipelines given in ISO 8601 format with timezone"

    // the example responses for the pipeline endpoint
    final val PipelinesResponseExampleAccepted = """{
        "status": "Accepted",
        "description": "The fetching of the pipeline data has been started",
        "options": {
            "projectName": "group/my-project-name",
            "reference": "master",
            "startDate": null,
            "endDate": null,
            "includeReports": "true",
            "includeJobs": "true",
            "includeJobLogs": "false",
            "useAnonymization": "false"
        }
    }"""
}
