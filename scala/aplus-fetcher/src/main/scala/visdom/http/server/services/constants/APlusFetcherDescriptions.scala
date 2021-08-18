package visdom.http.server.services.constants


object APlusFetcherDescriptions {
    final val APlusFetcherInfoEndpointDescription = "Returns information about the A+ data fetcher."
    final val APlusFetcherInfoEndpointSummary = "Returns A+ data fetcher info."

    final val APlusFetcherCourseEndpointDescription =
        "Starts a fetching process for course metadata from an A+ instance."
    final val APlusFetcherCourseEndpointSummary = "Fetch course metadata from A+."

    final val APlusFetcherModuleEndpointDescription =
        "Starts a fetching process for module data for a course from an A+ instance."
    final val APlusFetcherModuleEndpointSummary = "Fetch module data for a course from A+."

    final val APlusFetcherExerciseEndpointDescription =
        "Starts a fetching process for exercise data for a module in a course from an A+ instance."
    final val APlusFetcherExerciseEndpointSummary = "Fetch exercise data for a course from A+."

    final val StatusAcceptedDescription = "The fetching of the data has started"

    final val StatusInvalidDescription = "The request contained an invalid parameter"
}
