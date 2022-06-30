// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.swagger

import com.github.swagger.akka.model.Info
import visdom.adapters.AdapterValues


class SwaggerAdapterDocService(
    adapterValues: AdapterValues,
    serviceClasses: Set[Class[_]]
)
extends SwaggerDocService {
    override val host: String = adapterValues.apiAddress
    override val info: Info = Info(version = adapterValues.Version)
    override val apiClasses: Set[Class[_]] = serviceClasses
}
