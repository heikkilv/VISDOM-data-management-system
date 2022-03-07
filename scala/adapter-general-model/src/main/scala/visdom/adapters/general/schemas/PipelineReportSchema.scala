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


final case class PipelineReportSchema(
    pipeline_id: Int,
    total_time: Double,
    total_count: Int,
    success_count: Int,
    failed_count: Int,
    skipped_count: Int,
    error_count: Int,
    // test_suites: Seq[TestSuiteSchema],  // Note, leave out for now
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
        FieldDataModel(SnakeCaseConstants.HostName, false, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[PipelineReportSchema] = {

        TupleUtils.toTuple[Int, Double, Int, Int, Int, Int, Int, String](values) match {
            case Some(inputValues) => Some(
                (PipelineReportSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
