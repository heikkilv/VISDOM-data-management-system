// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.course.schemas

import visdom.spark.FieldDataType
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class SubmissionDataSchema(
    git: Option[GitSubmissionSchema]
)
extends BaseSchema

object SubmissionDataSchema extends BaseSchemaTrait[SubmissionDataSchema] {
    def fields: Seq[FieldDataType] = Seq(
        FieldDataType(SnakeCaseConstants.Git, true)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def transformValues(valueOptions: Seq[Option[Any]]): Option[SubmissionDataSchema] = {
        Some(
            SubmissionDataSchema(
                valueOptions.headOption match {
                    case Some(otherOption) => otherOption match {
                        case Some(other) => GitSubmissionSchema.fromAny(other) match {
                            case Some(gitSubmission: GitSubmissionSchema) => Some(gitSubmission)
                            case _ => None
                        }
                        case None => None
                    }
                    case None => None
                }
            )
        )
    }
}
