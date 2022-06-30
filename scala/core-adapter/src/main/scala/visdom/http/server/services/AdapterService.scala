// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.services

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import visdom.http.server.AdapterResponseHandler


trait AdapterService
extends Directives
with AdapterResponseHandler {
    val route: Route
}
