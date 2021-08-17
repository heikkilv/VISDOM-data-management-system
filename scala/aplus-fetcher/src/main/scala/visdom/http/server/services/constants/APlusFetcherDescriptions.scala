package visdom.http.server.services.constants


object APlusFetcherDescriptions {
    final val APlusFetcherInfoEndpointDescription = "Returns information about the A+ data fetcher."
    final val APlusFetcherInfoEndpointSummary = "Returns A+ data fetcher info."

    final val APlusFetcherCourseEndpointDescription =
        "Starts a fetching process for course metadata from an A+ instance."
    final val APlusFetcherCourseEndpointSummary = "Fetch course metadata from A+."

    final val CourseDataStatusAcceptedDescription = "The fetching of the data has started"

    final val CourseStatusInvalidDescription = "The request contained an invalid parameter"
    final val CourseResponseExampleInvalidName = "Invalid course id"
}
