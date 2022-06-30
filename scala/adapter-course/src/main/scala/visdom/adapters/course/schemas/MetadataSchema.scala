// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class MetadataSchema(
    other: Option[MetadataOtherSchema]
)
extends BaseSchema

object MetadataSchema extends BaseSchemaTrait[MetadataSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Other, true)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[MetadataSchema] = {
        Some(
            MetadataSchema(
                valueOptions.headOption match {
                    case Some(otherOption) => otherOption match {
                        case Some(other) => MetadataOtherSchema.fromAny(other)
                        case None => None
                    }
                    case None => None
                }
            )
        )
    }
}
