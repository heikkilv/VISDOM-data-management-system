// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.response


final case class GitlabFetcherInfoResponse(
    componentName: String,
    componentType: String,
    fetcherType: String,
    version: String,
    gitlabServer: String,
    mongoDatabase: String,
    startTime: String,
    apiAddress: String,
    swaggerDefinition: String
) extends FetcherInfoResponse
