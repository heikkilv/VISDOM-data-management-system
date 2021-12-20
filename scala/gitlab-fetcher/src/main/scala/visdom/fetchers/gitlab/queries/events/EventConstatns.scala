package visdom.fetchers.gitlab.queries.events


object EventConstants {
    final val EventRootPath = "/events"
    final val EventPath = "events"

    final val EventEndpointDescription = "Starts a fetching process for event data for a GitLab user."
    final val EventEndpointSummary = "Fetch event data for a GitLab user."

    final val EventStatusAcceptedDescription = "The fetching of the event data has been started"

    final val ParameterDescriptionDateAfter = "only events created after the given date, given in ISO 8601 format, are included"
    final val ParameterDescriptionDateBefore = "only events created before the given date, given in ISO 8601 format, are included"

    // the example responses for the events endpoint
    final val EventResponseExampleAccepted = """{
        "status": "Accepted",
        "description": "The fetching of the event data has been started",
        "options": {
            "userId": "username",
            "actionType": "pushed",
            "targetType": null,
            "dateAfter": "2020-01-01",
            "dateBefore": null,
            "useAnonymization": "false"
        }
    }"""
}
