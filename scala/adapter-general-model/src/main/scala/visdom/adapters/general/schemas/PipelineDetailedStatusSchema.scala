package visdom.adapters.general.schemas

import visdom.adapters.schemas.BaseSchema
import visdom.adapters.schemas.BaseSchemaTrait2
import visdom.spark.FieldDataModel
import visdom.utils.GeneralUtils.toStringOption
import visdom.utils.SnakeCaseConstants
import visdom.utils.TupleUtils
import visdom.utils.WartRemoverConstants


final case class PipelineDetailedStatusSchema(
    text: String,
    label: String,
    group: String
)
extends BaseSchema

object PipelineDetailedStatusSchema extends BaseSchemaTrait2[PipelineDetailedStatusSchema] {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def fields: Seq[FieldDataModel] = Seq(
        FieldDataModel(SnakeCaseConstants.Text, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Label, false, toStringOption),
        FieldDataModel(SnakeCaseConstants.Group, false, toStringOption)
    )

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def createInstance(values: Seq[Any]): Option[PipelineDetailedStatusSchema] = {
        TupleUtils.toTuple[String, String, String](values) match {
            case Some(inputValues) => Some(
                (PipelineDetailedStatusSchema.apply _).tupled(inputValues)
            )
            case None => None
        }
    }
}
