package visdom.http.server.services.constants


object BrokerExamples {
    final val BrokerInfoResponseExample = """{
        "componentName": "VISDOM-broker",
        "componentType": "broker",
        "version": "0.1",
        "startTime": "2021-06-07T09:30:00.000Z",
        "apiAddress": "localhost:9123",
        "swaggerDefinition": "/api-docs/swagger.json"
    }"""

    final val AdaptersResponseExampleName = "Example response about adapters"
    final val AdaptersResponseExample = """[
        {
            "componentName": "gitlab-adapter",
            "adapterType": "GitLab",
            "version": "0.2",
            "apiAddress": "localhost:9701",
            "swaggerDefinition": "/api-docs/swagger.json",
            "information": {
                "database": "gitlab"
            }
        }
    ]"""

    final val FetchersResponseExampleName = "Example response about fetchers"
    final val FetchersResponseExample = """[
        {
            "componentName": "gitlab-fetcher-com",
            "fetcherType": "GitLab",
            "version": "0.2",
            "apiAddress": "localhost:8701",
            "swaggerDefinition": "/api-docs/swagger.json",
            "information": {
                "gitlabServer": "https://gitlab.com",
                "database": "gitlab",
            }
        },
        {
            "componentName": "gitlab-fetcher-tuni",
            "fetcherType": "GitLab",
            "version": "0.2",
            "apiAddress": "localhost:8702",
            "swaggerDefinition": "/api-docs/swagger.json",
            "information": {
                "gitlabServer": "https://gitlab.tuni.fi",
                "database": "gitlab"
            }
        }
    ]"""
}
