package visdom.adapters.dataset.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class SonarAnalysisSchema(
    project_id: String,
    analysis_key: Option[String],
    date: String,
    revision: String
)
extends BaseSchema

object SonarAnalysisSchema extends BaseSchemaTrait2[SonarAnalysisSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.ProjectId, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.AnalysisKey, true, toStringOption),
        FieldDataModel(SnakeCaseConstants.Date, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Revision, false, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[SonarAnalysisSchema] = {
        TupleUtils.toTuple[String, Option[String], String, String](values) match {
            case Some(inputValues) => Some(
                (SonarAnalysisSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
