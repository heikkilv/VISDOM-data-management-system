// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.swagger

import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info


abstract trait SwaggerDocService extends SwaggerHttpService {
    override val info: Info = Info(version = SwaggerConstants.SwaggerJsonVersionDefault)
    override val unwantedDefinitions: Seq[String] = SwaggerConstants.SwaggerJsonUnwantedDefinitions
}
