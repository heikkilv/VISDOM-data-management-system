// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.options

import org.bson.conversions.Bson
import visdom.utils.GeneralUtils


final case class AttributeFilter(
    attributeName: String,
    filterType: AttributeFilterType,
    targetValue: String
)
extends AttributeFilterTrait {
    val objectTypesObject: ObjectTypesTrait = ObjectTypes
}

object AttributeFilter
extends AttributeFilterObject[AttributeFilter] {
    def fromString(filterString: String): Option[AttributeFilter] = {
        AttributeFilterType.getFilterType(filterString) match {
            case Some(filterType: AttributeFilterType) => {
                val stringParts: Seq[String] = filterString.split(filterType.operatorString)
                stringParts.size == 2 && stringParts.forall(stringPart => stringPart.size > 0) match {
                    case true => Some(
                        AttributeFilter(
                            attributeName = stringParts(0),
                            filterType = filterType,
                            targetValue = stringParts(1)
                        )
                    )
                    case false => None
                }
            }
            case None => None
        }
    }
}
