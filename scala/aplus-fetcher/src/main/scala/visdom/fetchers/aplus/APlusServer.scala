// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.fetchers.aplus

import scalaj.http.Http
import scalaj.http.HttpOptions
import scalaj.http.HttpRequest
import visdom.fetchers.HostServer
import visdom.utils.CommonConstants


class APlusServer(hostAddress: String, apiToken: Option[String], allowUnsafeSSL: Option[Boolean])
extends HostServer(hostAddress, apiToken, allowUnsafeSSL) {
    val baseAddress: String = List(hostAddress, APlusConstants.PathBase).mkString(CommonConstants.Slash)

    def modifyRequest(request: HttpRequest): HttpRequest = {
        val requestWithJsonFormat: HttpRequest = request.param(APlusConstants.ParamFormat, CommonConstants.Json)

        val requestWithToken: HttpRequest = apiToken match {
            case Some(token: String) => requestWithJsonFormat.header(
                APlusConstants.HeaderAuthorization,
                List(APlusConstants.Token, token).mkString(CommonConstants.WhiteSpace)
            )
            case None => requestWithJsonFormat
        }

        allowUnsafeSSL match {
            case Some(allowUnsafe: Boolean) if allowUnsafe => {
                requestWithToken.option(HttpOptions.allowUnsafeSSL)
            }
            case _ => requestWithToken
        }
    }
}
