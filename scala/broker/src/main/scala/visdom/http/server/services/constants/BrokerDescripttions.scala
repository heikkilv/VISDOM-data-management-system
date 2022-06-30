// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.services.constants


object BrokerDescriptions {
    final val BrokerInfoEndpointDescription = "Returns information about the data broker."
    final val BrokerInfoEndpointSummary = "Returns data broker info."

    final val AdaptersEndpointDescription = "Returns information about the active data adapters."
    final val AdaptersEndpointSummary = "Returns active data adapter info."

    final val FetchersEndpointDescription = "Returns information about the active data fetchers."
    final val FetchersEndpointSummary = "Returns active data fetcher info."

    final val BrokerQueryLogEntry = "Received query for active components of type: "
}
