// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.options

import org.bson.conversions.Bson
import visdom.utils.GeneralUtils


final case class DatasetAttributeFilter(
    attributeName: String,
    filterType: AttributeFilterType,
    targetValue: String
)
extends AttributeFilterTrait {
    override val objectTypesObject: ObjectTypesTrait = DatasetObjectTypes
}

object DatasetAttributeFilter
extends AttributeFilterObject[DatasetAttributeFilter]  {
    def fromString(filterString: String): Option[DatasetAttributeFilter] = {
        AttributeFilter.fromString(filterString)
            .map(
                filter => DatasetAttributeFilter(
                    attributeName = filter.attributeName,
                    filterType = filter.filterType,
                    targetValue = filter.targetValue
                )
            )
    }
}
