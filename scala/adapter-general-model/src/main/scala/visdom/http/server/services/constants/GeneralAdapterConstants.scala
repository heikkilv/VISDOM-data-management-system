// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.services.constants


object GeneralAdapterConstants {
    final val All = "all"
    final val Constructs = "constructs"
    final val Data = "data"
    final val Events = "events"
    final val Links = "links"
    final val None = "none"
    final val Page = "page"
    final val PageSize = "pageSize"
    final val PrivateToken = "Private-Token"
    final val Query = "query"
    final val Target = "target"
    final val Type = "type"
    final val Uuid = "uuid"

    final val DefaultLinks = "all"
    final val DefaultPage = "1"
    final val DefaultPageSize = "100"
    final val DefaultTarget = "event"
    final val DefaultType = "commit"
    final val DefaultUuid = "00000000-0000-0000-0000-000000000000"

    final val ValidTargetAuthor = "author"
    final val ValidTargetArtifact = "artifact"
    final val ValidTargetOrigin = "origin"
}
