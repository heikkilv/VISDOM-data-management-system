// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils.EnrichedWithToTuple
import visdom.utils.TupleUtils.toOption
import visdom.utils.WartRemoverConstants


final case class SubmissionSchema(
    id: Int,
    submission_time: String,
    grade: Int,
    submission_data: SubmissionDataSchema
)
extends BaseSchema

object SubmissionSchema extends BaseSchemaTrait[SubmissionSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Id, false),
        FieldDataType(SnakeCaseConstants.SubmissionTime, false),
        FieldDataType(SnakeCaseConstants.Grade, false),
        FieldDataType(SnakeCaseConstants.SubmissionData, false)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[SubmissionSchema] = {
        toOption(
            valueOptions.toTuple4,
            (
                (value: Any) => toIntOption(value),
                (value: Any) => toStringOption(value),
                (value: Any) => toIntOption(value),
                (value: Any) => SubmissionDataSchema.fromAny(value)
            )
        ) match {
            case Some((
                id: Int,
                submission_time: String,
                grade: Int,
                submission_data: SubmissionDataSchema
            )) => Some(SubmissionSchema(id, submission_time, grade, submission_data))
            case _ => None
        }
    }
}
