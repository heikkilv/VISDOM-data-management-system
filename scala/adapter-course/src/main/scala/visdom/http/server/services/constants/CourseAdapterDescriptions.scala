package visdom.http.server.services.constants


object CourseAdapterDescriptions {
    final val CourseAdapterInfoEndpointDescription = "Returns information about the course data adapter."
    final val CourseAdapterInfoEndpointSummary = "Returns course adapter info."

    final val PointsQueryEndpointSummary = "Returns a points document."
    final val PointsQueryEndpointDescription = "Returns a document related to points achieved by a student."

    final val PointsStatusOkDescription = "The data successfully fetched"

    final val StatusInvalidDescription = "The request contained an invalid parameter"

    final val DefaultErrorDescription = "Some query parameters contained invalid values"
}
