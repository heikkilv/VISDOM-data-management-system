package visdom.http.server.services.constants


object CourseAdapterDescriptions {
    final val CourseAdapterInfoEndpointDescription = "Returns information about the course data adapter."
    final val CourseAdapterInfoEndpointSummary = "Returns course adapter info."

    final val DataQueryEndpointSummary = "Returns a points document."
    final val DataQueryEndpointDescription = "Returns a document related to points achieved by a student."

    final val DataStatusOkDescription = "The data successfully fetched"
    final val UsernameStatusOkDescription = "The usernames successfully fetched"

    final val StatusInvalidDescription = "The request contained an invalid parameter"

    final val DefaultErrorDescription = "Some query parameters contained invalid values"
}
