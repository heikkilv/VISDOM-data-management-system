// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.fetchers

import scalaj.http.HttpRequest


abstract class HostServer(hostAddress: String, apiToken: Option[String], allowUnsafeSSL: Option[Boolean]) {
    val hostName: String = hostAddress
    val baseAddress: String

    def modifyRequest(request: HttpRequest): HttpRequest
}
