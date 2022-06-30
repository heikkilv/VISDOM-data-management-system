// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class SubmissionDataSchema(
    git: Option[SubmissionGitDataSchema],
    field_0: Option[String],
    field_1: Option[String],
    field_2: Option[String],
    field_3: Option[String],
    field_4: Option[String],
    field_5: Option[String],
    field_6: Option[String],
    field_7: Option[String],
    field_8: Option[String],
    field_9: Option[String]
)
extends BaseSchema

object SubmissionDataSchema extends BaseSchemaTrait2[SubmissionDataSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Git, true, SubmissionGitDataSchema.fromAny),
        FieldDataModel(SnakeCaseConstants.Field0, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.Field1, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.Field2, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.Field3, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.Field4, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.Field5, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.Field6, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.Field7, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.Field8, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.Field9, true, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[SubmissionDataSchema] = {
        TupleUtils.toTuple[Option[SubmissionGitDataSchema], Option[String], Option[String], Option[String],
                           Option[String], Option[String], Option[String], Option[String], Option[String],
                           Option[String], Option[String]](values) match {
            case Some(inputValues) => Some(
                (SubmissionDataSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
