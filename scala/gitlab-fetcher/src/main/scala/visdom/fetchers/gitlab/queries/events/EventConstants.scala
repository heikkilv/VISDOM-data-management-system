// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.fetchers.gitlab.queries.events


object EventConstants {
    final val EventRootPath = "/events"
    final val EventPath = "events"

    final val EventEndpointDescription = "Starts a fetching process for event data for a GitLab user."
    final val EventEndpointSummary = "Fetch event data for a GitLab user."

    final val EventStatusAcceptedDescription = "The fetching of the event data has been started"

    final val ParameterDescriptionDateAfter = "only events created after the given date are included (requires ISO 8601 format: YYYY-MM-DD)"
    final val ParameterDescriptionDateBefore = "only events created before the given date are included (requires ISO 8601 format: YYYY-MM-DD)"

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
