// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server

import spray.json.RootJsonFormat


trait BrokerServerProtocol
extends ServerProtocol {
    implicit lazy val brokerInfoResponseFormat: RootJsonFormat[response.BrokerInfoResponse] =
        jsonFormat6(response.BrokerInfoResponse)
}
