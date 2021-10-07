package visdom.http.server.services.constants


object CourseAdapterDescriptions {
    final val CourseAdapterInfoEndpointDescription = "Returns information about the course data adapter."
    final val CourseAdapterInfoEndpointSummary = "Returns course adapter info."

    final val DataQueryEndpointSummary = "Returns a points document."
    final val DataQueryEndpointDescription = "Returns a document related to points achieved by a student."

    final val HistoryQueryEndpointSummary = "Returns a history data."
    final val HistoryQueryEndpointDescription =
        "Returns a document containing aggregate values for student points, " +
        "attempted exercises, submissions and commits for each course week and predicted course grade."

    final val UsernameQueryEndpointSummary = "Returns student usernames for a course."
    final val UsernameQueryEndpointDescription = "Returns a list of student A+ system usernames for a A+ course."

    final val DataStatusOkDescription = "The data successfully fetched"
    final val HistoryStatusOkDescription = "The history data successfully fetched"
    final val UsernameStatusOkDescription = "The usernames successfully fetched"

    final val StatusInvalidDescription = "The request contained an invalid parameter"

    final val DefaultErrorDescription = "Some query parameters contained invalid values"
}
