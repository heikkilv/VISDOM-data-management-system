package visdom.http.server.services.constants


object GeneralAdapterDescriptions {
    // scalastyle:off line.size.limit
    final val InfoEndpointDescription = "Returns information about the data adapter that converts raw data to the general software data model, i.e. to events and artifacts."
    final val InfoEndpointSummary = "Returns general data model adapter information."
    // scalastyle:on line.size.limit

    final val SingleEndpointDescription = "Returns a single entity based on type and id."
    final val SingleEndpointSummary = "Returns a single entity based on type and id."

    final val OriginsEndpointDescription = "Returns the origin objects."
    final val OriginsEndpointSummary = "Returns the origin objects."
    final val EventsEndpointDescription = "Returns the event objects."
    final val EventsEndpointSummary = "Returns the event objects."
    final val AuthorsEndpointDescription = "Returns the author objects."
    final val AuthorsEndpointSummary = "Returns the author objects."
    final val ArtifactsEndpointDescription = "Returns the artifact objects."
    final val ArtifactsEndpointSummary = "Returns the artifact objects."

    final val TestEndpointDescription = "Test endpoint for paging features."
    final val TestEndpointSummary = "Test endpoint for paging features."

    final val DescriptionData = "A comma-separated list of data attributes included in the response. Default: all attributes included"
    final val DescriptionLinks = "What links are included in the results. Default: all links included"
    final val DescriptionPage = "The page number used in filtering the results"
    final val DescriptionPageSize = "The page size used in filtering the results"
    final val DescriptionPrivateToken = "If value is given, only commits that contain the given value are considered"
    final val DescriptionTarget = "Either 'event' or 'origin'. Default: 'event'"
    final val DescriptionType = "The type of the object"
    final val DescriptionUuid = "The unique id of the object"

    final val SingleStatusOkDescription = "Successfully fetched result data."
    final val SingleNotFoundDescription = "Did not find the object."

    final val TestStatusOkDescription = "Successfully fetched result data."

    final val UpdateEndpointSummary = "Updates the object cache."
    final val UpdateEndpointDescription = "Updates the object cache for all origins, events, artifacts and authors to make queries faster."
    final val UpdateOkDescription = "The cache has been updated."
}
