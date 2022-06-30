// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapter.gitlab.queries.timestamps


object TimestampConstants {
    final val TimestampRootPath = "/timestamps"
    final val TimestampPath = "timestamps"

    final val TimestampEndpointDescription =
        """Return the commit timestamps for the files specified in the query for each project."""
    final val TimestampEndpointSummary = "Return the commit timestamps for the asked files."

    final val TimestampStatusOkDescription = "The data successfully fetched"

    // the example responses for the commits endpoint
    final val ResponseExampleOkName = "Example response"
    final val TimestampResponseExampleOk = """{
        "project-name-1": {
            "file-a": [
                "2021-05-11T12:35:59Z",
                "2021-05-25T13:36:58Z"
            ],
            "file-b": [
                "2020-06-10T10:57:42Z"
            ]
        },
        "project-name-2": {
            "file-c": [
                "2020-10-15T11:43:19Z"
            ]
        }
    }"""
}
