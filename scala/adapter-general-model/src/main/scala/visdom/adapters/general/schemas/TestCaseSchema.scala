// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toDoubleOption
import visdom.utils.GeneralUtils.toIntOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class TestCaseSchema(
    status: String,
    name: String,
    classname: String,
    file: Option[String],
    execution_time: Double,
    system_output: Option[String],
    stack_trace: Option[String],
    recent_failures: Option[String]
)
extends BaseSchema

object TestCaseSchema extends BaseSchemaTrait2[TestCaseSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Status, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Name, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Classname, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.File, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.ExecutionTime, false, toDoubleOption),
        FieldDataModel(SnakeCaseConstants.SystemOutput, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.StackTrace, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.RecentFailures, true, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[TestCaseSchema] = {
        TupleUtils.toTuple[String, String, String, Option[String], Double, Option[String],
                           Option[String], Option[String]](values) match {
            case Some(inputValues) => Some(
                (TestCaseSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
