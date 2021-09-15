package visdom.http.server.services.constants


object CourseAdapterExamples {
    final val CourseAdapterInfoResponseExample = """{
        "componentName": "adapter-course",
        "componentType": "adapter",
        "adapterType": "Course",
        "version": "0.1",
        "startTime": "2021-09-01T12:00:00.000Z",
        "apiAddress": "localhost:8790",
        "swaggerDefinition": "/api-docs/swagger.json"
    }"""

    final val ResponseExampleOkName = "example response"
    final val PointsResponseExampleOk = """{
        "name": "problem",
        "commit_count": 1,
        "commit_meta": [
            {
                "hash": "qwertyuiopasdfghjklzxcvbnm",
                "message": "example commit message",
                "commit_date": "2020-01-01T12:00:00.000Z",
                "committer_email": "user@mail"
            }
        ]
    }"""

    final val ResponseExampleInvalidName = "invalid course id"
    final val ResponseExampleInvalid = """{
    }"""
}
