package visdom.http.server.services.constants


object GeneralAdapterExamples {
    final val InfoResponseExample = """{
        "componentName": "adapter-general-model",
        "componentType": "adapter",
        "adapterType": "GeneralModel",
        "version": "0.1",
        "startTime": "2021-11-08T12:00:00.000Z",
        "apiAddress": "localhost:8795",
        "swaggerDefinition": "/api-docs/swagger.json"
    }"""

    final val SingleExampleOkName = "Single response example"
    final val SingleExampleOk = """{
        "id": "00000000-0000-0000-0000-000000000000",
        "type": "commit",
        "data": {}
    }
    """

    final val SingleExampleNotFoundName = "Single response not found example"
    final val SingleExampleNotFound = """{
        "description": "Did not find data for id '00000000-0000-0000-0000-000000000000' and type 'commit'.",
        "status": "NotFound"
    }
    """

    final val TestExampleOkName = "Test response example"
    final val TestExampleOk = """{
        "count": 2,
        "total_count": 2,
        "page": 1,
        "page_size": 100,
        "previous_page": null,
        "next_page": null,
        "results": [
            {
                "id": 1,
                "data": "data1"
            },
            {
                "id": 2,
                "data": "data2"
            }
        ]
    }
    """

    final val UpdateExampleName = "Cache update example"
    final val UpdateExample = """{
        "message": "The cache has been updated."
    }"""
}
