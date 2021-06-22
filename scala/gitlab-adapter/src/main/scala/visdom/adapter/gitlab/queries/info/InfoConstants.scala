package visdom.adapter.gitlab.queries.info


object InfoConstants {
    final val InfoRootPath = "/info"
    final val InfoPath = "info"

    final val InfoEndpointDescription = "Returns information about the data adapter."
    final val InfoEndpointSummary = "Returns adapter info."

    final val InfoStatusOkDescription = "The information was fetched successfully"

    // the example response for the info endpoint
    final val InfoResponseExampleName = "Example response"
    final val InfoResponseExample = """{
        "componentName": "GitLab-adapter",
        "componentType": "adapter",
        "adapterType": "GitLab",
        "version": "0.2",
        "startTime": "2021-06-07T09:30:00.000Z",
        "apiAddress": "localhost:9876",
        "swaggerDefinition": "/api-docs/swagger.json"
    }"""
}
