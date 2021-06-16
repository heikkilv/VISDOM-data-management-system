package visdom.fetchers.gitlab.queries.info


object InfoConstants {
    final val InfoRootPath = "/info"
    final val InfoPath = "info"

    final val InfoEndpointDescription = "Returns information about the data fetcher."
    final val InfoEndpointSummary = "Returns fetcher info."

    final val InfoStatusOkDescription = "The information fetched successfully"

    // the example response for the info endpoint
    final val InfoResponseExampleName = "Example response"
    final val InfoResponseExample = """{
        "componentName": "GitLab-fetcher",
        "componentType": "fetcher",
        "fetcherType": "GitLab",
        "version": "0.2",
        "gitlabServer": "https://gitlab.com",
        "mongoDatabase": "gitlab",
        "startTime": "2021-06-07T09:30:00.000Z",
        "apiAddress": "localhost:8701",
        "swaggerDefinition": "/api-docs/swagger.json"
    }"""
}
