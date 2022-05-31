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


final case class TestSuiteSchema(
    name: String,
    total_time: Double,
    total_count: Int,
    success_count: Int,
    failed_count: Int,
    skipped_count: Int,
    error_count: Int,
    suite_error: Option[String],
    test_cases: Seq[TestCaseSchema]
)
extends BaseSchema

object TestSuiteSchema extends BaseSchemaTrait2[TestSuiteSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Name, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.TotalTime, false, toDoubleOption),
        FieldDataModel(SnakeCaseConstants.TotalCount, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.SuccessCount, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.FailedCount, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.SkippedCount, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.ErrorCount, false, toIntOption),
        FieldDataModel(SnakeCaseConstants.SuiteError, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.TestCases, false, (value: Any) => toSeqOption(value, TestCaseSchema.fromAny))
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[TestSuiteSchema] = {
        TupleUtils.toTuple[String, Double, Int, Int, Int, Int, Int,
                           Option[String], Seq[TestCaseSchema]](values) match {
            case Some(inputValues) => Some(
                (TestSuiteSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
