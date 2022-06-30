// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.options

import org.mongodb.scala.bson.BsonDocument
import visdom.http.server.options.LinkTypes
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class MultiQueryOptions(
    targetType: String,
    objectType: Option[String],
    query: Option[Seq[AttributeFilterTrait]],
    dataAttributes: Option[Seq[String]],
    includedLinks: LinkTypes,
    page: Int,
    pageSize: Int
)
extends BaseQueryWithPageOptions {
    def toBsonDocument(): BsonDocument = {
        BsonDocument(
            Map(
                SnakeCaseConstants.TargetType -> JsonUtils.toBsonValue(targetType),
                SnakeCaseConstants.ObjectType -> JsonUtils.toBsonValue(objectType),
                SnakeCaseConstants.DataAttributes -> JsonUtils.toBsonValue(dataAttributes),
                SnakeCaseConstants.IncludedLinks -> JsonUtils.toBsonValue(includedLinks.linkType),
                SnakeCaseConstants.Page -> JsonUtils.toBsonValue(page),
                SnakeCaseConstants.PageSize -> JsonUtils.toBsonValue(pageSize)
            )
        )
    }
}
