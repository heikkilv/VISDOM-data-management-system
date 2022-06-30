// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.course.schemas

import java.time.Instant
import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.toBooleanOption
import visdom.utils.GeneralUtils.toInstantOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils.EnrichedWithToTuple
import visdom.utils.WartRemoverConstants


final case class MetadataOtherSchema(
    start_date: Option[String],
    end_date: Option[String],
    late_submission_date: Option[String],
    path: Option[String],
    is_folder: Option[Boolean]
)
extends BaseSchema

object MetadataOtherSchema extends BaseSchemaTrait[MetadataOtherSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.StartDate, true),
        FieldDataType(SnakeCaseConstants.EndDate, true),
        FieldDataType(SnakeCaseConstants.LateSubmissionDate, true),
        FieldDataType(SnakeCaseConstants.Path, true),
        FieldDataType(SnakeCaseConstants.IsFolder, true)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[MetadataOtherSchema] = {
        val (
            startDateOption,
            endDateOption,
            lateSubmissionDateOption,
            pathOption,
            isFolderOption
        ) = valueOptions.toTuple5
        Some(
            MetadataOtherSchema(
                toStringOption(startDateOption, true),
                toStringOption(endDateOption, true),
                toStringOption(lateSubmissionDateOption, true),
                toStringOption(pathOption),
                toBooleanOption(isFolderOption)
            )
        )
    }
}
