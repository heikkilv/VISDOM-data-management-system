package visdom.http.server.services.constants


object APlusFetcherExamples {

    final val APlusFetcherInfoResponseExample = """{
        "componentName": "aplus-fetcher",
        "componentType": "fetcher",
        "fetcherType": "APlus",
        "version": "0.1",
        "sourceServer": "https://plus.fi",
        "mongoDatabase": "aplus",
        "startTime": "2021-08-16T12:00:00.000Z",
        "apiAddress": "localhost:8751",
        "swaggerDefinition": "/api-docs/swagger.json"
    }"""

    final val CourseDataResponseExampleAccepted = """{
        "status": "Accepted",
        "description": "The fetching of the data has been started",
        "options": {
            "courseId": 123
        }
    }"""

    final val ResponseExampleInvalidName = "Invalid course id"
    final val ResponseExampleInvalid= """{
        "status": "BadRequest",
        "description": "'-5'' is not a valid course id"
    }"""

    final val ModuleDataResponseExampleAccepted = """{
        "status": "Accepted",
        "description": "The fetching of the data has been started",
        "options": {
            "courseId": 123,
            "moduleId": 765,
            "parseNames": "true",
            "includeExercises": "true",
            "gdprExerciseId": "12345",
            "gdprFieldName": "field_0",
            "gdprAcceptedAnswer": "a"
        }
    }"""

    final val ExerciseDataResponseExampleAccepted = """{
        "status": "Accepted",
        "description": "The fetching of the data has been started",
        "options": {
            "courseId": 123,
            "moduleId": 765,
            "exerciseId": 5555,
            "parseNames": "true",
            "gdprExerciseId": "12345",
            "gdprFieldName": "field_0",
            "gdprAcceptedAnswer": "a"
        }
    }"""
}
