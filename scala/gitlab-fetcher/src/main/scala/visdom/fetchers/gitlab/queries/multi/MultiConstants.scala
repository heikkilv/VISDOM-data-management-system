// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.fetchers.gitlab.queries.multi


object MultiConstants {
    final val MultiRootPath = "/multi"
    final val MultiPath = "multi"

    final val MultiEndpointDescription =
        """Starts a fetching process for commit and file data from the given GitLab repositories.
        All additional metadata and link data will be included. Only fetches data from the master branches."""
    final val MultiEndpointSummary = "Fetch commit and file data from a GitLab repositories."

    final val MultiStatusAcceptedDescription = "The fetching of the data has started"

    final val ParameterDescriptionStartDate = "the earliest timestamp for the fetched data given in ISO 8601 format with timezone"
    final val ParameterDescriptionEndDate = "the latest timestamp for the fetched data given in ISO 8601 format with timezone"

    final val ParameterDefaultFilePath: Option[String] = None
    final val ParameterDefaultIncludeStatistics: Boolean = true
    final val ParameterDefaultIncludeFileLinks: Boolean = true
    final val ParameterDefaultIncludeReferenceLinks: Boolean = true
    final val ParameterDefaultIncludeCommitLinks: Boolean = true

    // the example responses for the all endpoint
    final val MultiResponseExampleAccepted = """{
        "status": "Accepted",
        "description": "The fetching of the data has been started",
        "options": {
            "projectNames": "group/project-name1,group/project-name2",
            "filePath": "folder",
            "recursive": "true",
            "startDate": "2020-01-01T00:00:00.000Z",
            "endDate": "2021-01-01T00:00:00.000Z",
            "useAnonymization": "false",
            "projects": {
                "allowed": ["group/project-name1"],
                "unauthorized": [],
                "notFound": ["group/project-name2"]
            }
        }
    }"""
}
