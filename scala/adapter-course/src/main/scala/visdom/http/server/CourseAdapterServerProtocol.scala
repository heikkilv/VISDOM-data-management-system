// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server

import akka.util.Timeout
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
import spray.json.RootJsonFormat


trait CourseAdapterServerProtocol
extends ServerProtocol {
    implicit lazy val courseAdapterInfoResponseFormat: RootJsonFormat[response.CourseAdapterInfoResponse] =
        jsonFormat7(response.CourseAdapterInfoResponse)

    override implicit val timeout: Timeout = Timeout((10 * ServerConstants.DefaultMaxResponseDelaySeconds + 1).seconds)
}
