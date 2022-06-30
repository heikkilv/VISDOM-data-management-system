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
import visdom.utils.GeneralUtils.toSeqOption
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class PipelineReportSchema(
    pipeline_id: Int,
    total_time: Double,
    total_count: Int,
    success_count: Int,
    failed_count: Int,
    skipped_count: Int,
    error_count: Int,
    test_suites: Seq[TestSuiteSchema],
    host_name: String
)
extends BaseSchema

object PipelineReportSchema extends BaseSchemaTrait2[PipelineReportSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.PipelineId, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.TotalTime, false, toDoubleOption),
        FieldDataModel(SnakeCaseConstants.TotalCount, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.SuccessCount, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.FailedCount, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.SkippedCount, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.ErrorCount, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.TestSuites, false, (value: Any) => toSeqOption(value, TestSuiteSchema.fromAny)),
        FieldDataModel(SnakeCaseConstants.HostName, false, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[PipelineReportSchema] = {
        TupleUtils.toTuple[Int, Double, Int, Int, Int, Int, Int, Seq[TestSuiteSchema], String](values) match {
            case Some(inputValues) => Some(
                (PipelineReportSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
