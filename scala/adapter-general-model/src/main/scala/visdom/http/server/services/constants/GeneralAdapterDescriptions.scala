package visdom.http.server.services.constants


object GeneralAdapterDescriptions {
    // scalastyle:off line.size.limit
    final val InfoEndpointDescription = "Returns information about the data adapter that converts raw data to the general software data model, i.e. to events and artifacts."
    final val InfoEndpointSummary = "Returns general data model adapter information."
    // scalastyle:on line.size.limit

    final val TestEndpointDescription = "Test endpoint for paging features."
    final val TestEndpointSummary = "Test endpoint for paging features."

    final val DescriptionPage = "The page number used in filtering the results"
    final val DescriptionPageSize = "The page size used in filtering the results"
    final val DescriptionPrivateToken = "If value is given, only commits that contain the given value are considered"

    final val TestStatusOkDescription = "Successfully fetched result data."
}
