package visdom.fetchers.gitlab.queries.pipelines


object PipelinesConstants {
    final val PipelinesRootPath = "/pipelines"
    final val PipelinesPath = "pipelines"

    final val PipelinesStatusAcceptedDescription = "The fetching of the pipeline data has been started"

    final val PipelinesEndpointDescription = "Starts a fetching process for pipeline data from a GitLab repository."
    final val PipelinesEndpointSummary = "Fetch pipeline data from a GitLab repository."

    // the example responses for the pipeline endpoint
    final val PipelinesResponseExampleAccepted = """{
        "status": "Accepted",
        "description": "The fetching of the pipeline data has been started",
        "options": {
            "projectName": "group/my-project-name"
        }
    }"""
}
