// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.options

import visdom.adapters.options.AttributeFilter
import visdom.adapters.options.AttributeFilterTrait
import visdom.adapters.options.ObjectTypes
import visdom.utils.CommonConstants


final case class MultiOptions(
    pageOptions: OnlyPageInputOptions,
    targetType: String,
    objectType: String,
    query: Option[String],
    dataAttributes: Option[String],
    includedLinks: String
)
extends BaseMultiInputOptions {
    def getObjectTypes(): Map[String, Set[String]] = {
        ObjectTypes.objectTypes
    }

    def getAttributeFilters(): Option[Seq[AttributeFilterTrait]] = {
        MultiOptions.getAttributeFilters(query)
    }
}

object MultiOptions {
    def getAttributeFilters(queryStringOption: Option[String]): Option[Seq[AttributeFilter]] = {
        (
            queryStringOption.map(
                queryString =>
                    queryString
                        .split(CommonConstants.Semicolon)
                        .map(attributeQuery => AttributeFilter.fromString(attributeQuery))
                        .flatten
                        .toSeq
            )
        ) match {
            case Some(filters: Seq[AttributeFilter]) => filters.nonEmpty match {
                case true => Some(filters)
                case false => None
            }
            case None => None
        }
    }
}
