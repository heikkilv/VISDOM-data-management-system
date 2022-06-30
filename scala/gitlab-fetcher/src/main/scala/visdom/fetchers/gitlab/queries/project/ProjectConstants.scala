// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.fetchers.gitlab.queries.project


object ProjectConstants {
    final val ProjectRootPath = "/project"
    final val ProjectPath = "project"

    final val ProjectStatusAcceptedDescription = "The fetching of the project document has been started"

    final val ProjectEndpointDescription = "Starts a fetching process for project document from a GitLab repository."
    final val ProjectEndpointSummary = "Fetch project document from a GitLab repository."

    // the example response for the project endpoint
    final val ProjectResponseExampleAccepted = """{
        "status": "Accepted",
        "description": "The fetching of the project document has been started",
        "options": {
            "projectId": null,
            "projectName": "group/my-project-name",
            "useAnonymization": "false"
        }
    }"""
}
